package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.services.BillingService
import mu.KotlinLogging
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory


class BillingScheduler(
        private val billingService: BillingService
): Job {
    private val logger = KotlinLogging.logger("SchedulingService")

    fun scheduleMonthlyBilling() {
        try {
            val schedulerFactory = StdSchedulerFactory()
            val scheduler = schedulerFactory.scheduler
            val trigger = TriggerBuilder.newTrigger()
                    .withIdentity("SchedulingService", "io.pleo.antaeus.core.services")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 12 1 * *"))
                    .forJob("BillingService", "io.pleo.antaeus.core.services")
                    .build()
            val jobBuilder = JobBuilder.newJob(BillingScheduler::class.java).build()
            scheduler.scheduleJob(jobBuilder, trigger)
            scheduler.start()
        } catch (ex: Exception) {
            logger.error { ex }
        }
    }

    override fun execute(context: JobExecutionContext?) {
        billingService.billPendingInvoices()
    }

}
