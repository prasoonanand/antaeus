package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.config.CoreConfiguration
import mu.KotlinLogging
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory


class SchedulingService(
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
                    .setJobData(JobDataMap(mapOf("billingService" to billingService)))
                    .build()
            val trigger = TriggerBuilder.newTrigger()
                    .withIdentity("SchedulingServiceTrigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule(coreConfig.cronRegex))
                    .build()
            scheduler.scheduleJob(jobBuilder, trigger)
            scheduler.start()
        } catch (ex: Exception) {
            logger.error { ex }
        }
    }
}

class BillingScheduler : Job {

    override fun execute(context: JobExecutionContext?) {
        val billingService = context?.jobDetail?.jobDataMap?.get("billingService") as BillingService
        billingService.billPendingInvoices()
    }

}
