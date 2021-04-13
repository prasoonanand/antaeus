package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.config.CoreConfiguration
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobKey
import org.quartz.JobListener

class BillingRetryListener(
        private val billingService: BillingService,
        private val coreConfig: CoreConfiguration
) : JobListener {
    private val logger = KotlinLogging.logger("BillingRetryListener")

    override fun getName(): String {
        return "BillingRetryListener";
    }

    override fun jobToBeExecuted(context: JobExecutionContext?) {
        logger.info("Start " + context?.jobDetail?.key)
    }

    override fun jobExecutionVetoed(context: JobExecutionContext?) {
        TODO("Not yet implemented")
    }

    override fun jobWasExecuted(context: JobExecutionContext?, jobException: JobExecutionException?) {
        logger.info("End " + context?.jobDetail?.key)
        if (JobKey.jobKey("BillingServiceJob") == context?.jobDetail?.key) {
            retryFailedBillings();
        }
    }

    private fun retryFailedBillings() {
        var loop = coreConfig.retriesCount;
        while (loop > 1) {
            billingService.billInvoices(InvoiceStatus.FAILURE, false)
            Thread.sleep((coreConfig.retryIntervalInSec * 1000).toLong())
            loop--
        }
        billingService.billInvoices(InvoiceStatus.FAILURE, true)
    }
}
