package io.github.dziodzi

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

class ServerRequestException(message: String) : Exception(message)

private val log = LoggerFactory.getLogger("MainLogger")

val client = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

private val json = Json { ignoreUnknownKeys = true }

private suspend fun fetchNews(pageNumber: Int): List<News> {
    return try {
        val response: HttpResponse = client.get(Config.BASE_URL) {
            parameter("page", pageNumber)
            parameter("page_size", Config.PAGE_SIZE)
            Config.requestParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }

        if (response.status == HttpStatusCode.OK) {
            val responseBody: String = response.body()
            Json { ignoreUnknownKeys = true }.decodeFromString<NewsResponse>(responseBody).results
        } else {
            log.error("Failed to fetch news from page $pageNumber: ${response.status}")
            emptyList()
        }
    } catch (e: Exception) {
        log.error("Error fetching news from page $pageNumber: ${e.message}")
        emptyList()
    }
}

suspend fun getNews(count: Int = Config.NEWS_COUNT): List<News> {
    val newsList = mutableListOf<News>()
    var currentPage = 1

    try {
        while (newsList.size < count) {
            val pageNews = fetchNews(currentPage)

            if (pageNews.isEmpty()) {
                break
            }

            newsList.addAll(pageNews)
            log.info("Fetched ${pageNews.size} news from page $currentPage. Total news: ${newsList.size}")
            currentPage++
        }

        return newsList.take(count)
    } catch (e: ServerRequestException) {
        throw e
    } catch (e: Exception) {
        throw ServerRequestException("Failed to get news: ${e.message}")
    }
}

suspend fun getNewsParallel(targetCount: Int, dispatcher: CoroutineContext): List<News> = coroutineScope {
    val newsList = mutableListOf<News>()
    val jobs = mutableListOf<Deferred<List<News>>>()

    for (pageNumber in 1..Int.MAX_VALUE) {
        if (newsList.size >= targetCount) {
            break
        }

        val job = async(dispatcher) {
            Semaphore(Config.MAX_PARALLEL_REQUESTS).withPermit {
                val pageNews = fetchNews(pageNumber)
                log.info("Worker ${pageNumber % Config.THREAD_POOL_SIZE} fetched ${pageNews.size} articles from page $pageNumber. Total news: ${newsList.size}")
                newsList.addAll(pageNews)
                pageNews
            }
        }
        jobs.add(job)

        if (jobs.size >= Config.MAX_PARALLEL_REQUESTS) {
            jobs.awaitAll()
            jobs.clear()
        }
    }

    jobs.awaitAll()
    return@coroutineScope newsList.take(targetCount)
}