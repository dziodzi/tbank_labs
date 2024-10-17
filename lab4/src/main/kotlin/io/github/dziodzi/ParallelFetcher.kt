import io.github.dziodzi.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

private val log = LoggerFactory.getLogger("ParallelFetcherLogger")

fun main() = runBlocking {
    val startTime = System.currentTimeMillis()

    val threadPool = Executors.newFixedThreadPool(Config.THREAD_POOL_SIZE).asCoroutineDispatcher()

    val newsList = getNewsParallel(Config.NEWS_COUNT, threadPool)
    log.info("Successfully received news")

    val mostRatedNews = newsList.getMostRatedNews(count = Config.NEWS_COUNT, period = Config.PERIOD)
    log.info("Successfully rated news")

    saveNewsToCsv("news_parallel.csv", mostRatedNews)
    saveNewsToMarkdown("news_parallel.md", mostRatedNews)

    val endTime = System.currentTimeMillis()
    log.info("Total execution time: ${endTime - startTime} ms")

    threadPool.close()
}