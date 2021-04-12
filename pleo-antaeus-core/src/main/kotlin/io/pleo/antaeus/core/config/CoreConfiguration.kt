package io.pleo.antaeus.core.config

class CoreConfiguration {
    val cronRegex: String? = System.getProperty("CRON_REGEX")?.toString()
}
