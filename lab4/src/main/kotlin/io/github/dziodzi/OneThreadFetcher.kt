package io.github.dziodzi

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("OneThreadFetcherLogger")

fun main() {
    runBlocking {
        val startTime = System.currentTimeMillis()

        val newsList = getNews(count = Config.NEWS_COUNT)
        log.info("Successfully received news")

        val mostRatedNews = newsList.getMostRatedNews(count = Config.NEWS_COUNT, period = Config.PERIOD)
        log.info("Successfully rated news")

        saveNewsToCsv("news_one_thread.csv", mostRatedNews)
        saveNewsToMarkdown("news_one_thread.md", mostRatedNews)

        val endTime = System.currentTimeMillis()
        log.info("Total execution time: ${endTime - startTime} ms")
    }
}
