package io.pleo.antaeus.core.config

class CoreConfiguration {
    val cronRegex: String? = System.getProperty("CRON_REGEX")?.toString()
    val retriesCount: Int = System.getProperty("RETRIES_COUNT")?.toInt() ?: 5
    val retryIntervalInSec: Int = System.getProperty("RETRY_INTERVAL_IN_SEC")?.toInt() ?: 5
}
