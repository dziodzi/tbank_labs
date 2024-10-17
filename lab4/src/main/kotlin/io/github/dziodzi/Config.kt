package io.github.dziodzi

import java.time.LocalDate

object Config {
    const val BASE_URL = "https://kudago.com/public-api/v1.4/news/"
    const val PAGE_SIZE = 1
    const val NEWS_COUNT = 20
    const val THREAD_POOL_SIZE = 4
    const val MAX_PARALLEL_REQUESTS = 1
    val PERIOD = LocalDate.now().minusDays(360)..LocalDate.now()

    val requestParameters = mapOf(
        "order_by" to "-publication_date",
        "actual_only" to true,
        "location" to "spb",
        "fields" to "id,publication_date,title,description,site_url,favorites_count,comments_count"
    )
}

// PAGE_SIZE = 25, NEWS_COUNT = 500, THREAD_POOL_SIZE = 4, PERIOD = 360 days
// 17:43:52.654 [main] INFO ParallelFetcherLogger -- Total execution time: 7583 ms
// 17:44:39.099 [main] INFO OneThreadFetcherLogger -- Total execution time: 10167 ms
