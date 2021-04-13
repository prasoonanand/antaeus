package io.pleo.antaeus.data.config

class DBConfiguration {

    val dbBatchSize: Int = System.getProperty("DB_BATCH_SIZE")?.toInt() ?: 100
}
