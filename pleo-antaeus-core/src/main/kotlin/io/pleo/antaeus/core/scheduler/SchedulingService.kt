package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.config.CoreConfiguration
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.KeyMatcher


class SchedulingService(
        private val billingRetryListener: BillingRetryListener,
        private val billingService: BillingService,
        private val coreConfig: CoreConfiguration
) {
    private val logger = KotlinLogging.logger("SchedulingService")

    fun scheduleMonthlyBilling() {
        try {
            val schedulerFactory = StdSchedulerFactory()
            val scheduler = schedulerFactory.scheduler
            val jobBuilder = JobBuilder
                    .newJob(BillingScheduler::class.java)
                    .withIdentity("BillingServiceJob")
                    .setJobData(JobDataMap(mapOf("billingService" to billingService, "invoiceStatus" to InvoiceStatus.PENDING)))
                    .build()
            val trigger = TriggerBuilder.newTrigger()
                    .withIdentity("SchedulingServiceTrigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule(coreConfig.cronRegex))
                    .build()
            scheduler.scheduleJob(jobBuilder, trigger)
            scheduler.listenerManager
                    .addJobListener(billingRetryListener, KeyMatcher.keyEquals(JobKey.jobKey("BillingServiceJob")))
            scheduler.start()
        } catch (ex: Exception) {
            logger.error { ex }
        }
    }
}
