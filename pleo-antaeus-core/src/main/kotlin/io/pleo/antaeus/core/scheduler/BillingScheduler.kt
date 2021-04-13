package io.pleo.antaeus.core.scheduler

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.models.InvoiceStatus
import org.quartz.Job
import org.quartz.JobExecutionContext

class BillingScheduler: Job {

    override fun execute(context: JobExecutionContext?) {
        val billingService = context?.jobDetail?.jobDataMap?.get("billingService") as BillingService
        val invoiceStatus = context?.jobDetail?.jobDataMap?.get("invoiceStatus") as InvoiceStatus
        billingService.billInvoices(invoiceStatus, false)
    }

}
