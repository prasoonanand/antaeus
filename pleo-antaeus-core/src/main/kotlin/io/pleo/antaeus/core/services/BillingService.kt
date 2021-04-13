package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

class BillingService(
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
) {
    private val logger = KotlinLogging.logger("BillingService")

    fun billInvoices(status: InvoiceStatus, isFinal: Boolean): Map<String, Int> {
        var page = 0;
        logger.info("Running the schedule " + LocalDate.now() + " at " + System.currentTimeMillis())
        var paid = AtomicInteger(0)
        var customerRejected = AtomicInteger(0)
        var currencyMismatch = AtomicInteger(0)
        var customerNotFound = AtomicInteger(0)
        var networkIssue = AtomicInteger(0)
        while (true) {
            logger.info("Fetching data for Page $page")
            val invoices = invoiceService.fetchAllInvoicesOnStatus(status, page++)
            if (invoices.isEmpty()) {
                break
            }
            val billedInvoices = invoices.parallelStream().peek {
                val invoice = it
                try {
                    val success = paymentProvider.charge(invoice)
                    if (!success) {
                        invoice.status = InvoiceStatus.CUSTOMER_REJECTED
                        customerRejected.incrementAndGet()
                    } else {
                        invoice.status = InvoiceStatus.PAID
                        paid.incrementAndGet()
                    }
                } catch (ex: CurrencyMismatchException) {
                    invoice.status = InvoiceStatus.CURRENCY_MISMATCH
                    currencyMismatch.incrementAndGet()
                    logger.error { ex }
                } catch (ex: CustomerNotFoundException) {
                    invoice.status = InvoiceStatus.CUSTOMER_NOT_FOUNT
                    customerNotFound.incrementAndGet()
                    logger.error { ex }
                } catch (ex: NetworkException) {
                    invoice.status = if (isFinal) InvoiceStatus.ERROR else InvoiceStatus.FAILURE
                    networkIssue.incrementAndGet()
                    logger.error { ex }
                } catch (ex: Exception) {
                    invoice.status = if (isFinal) InvoiceStatus.ERROR else InvoiceStatus.FAILURE
                    logger.error { ex }
                }
                logger.info("Status " + invoice.status + " for customer " + invoice.customerId + " for invoice " + invoice.id)
            }.collect(Collectors.toList())
            invoiceService.updateBatchStatus(billedInvoices)
        }
        logger.info("Finishing the schedule " + LocalDate.now() + " till " + System.currentTimeMillis())
        return mapOf("paid" to paid.get(),
                "customerRejected" to customerRejected.get(),
                "networkIssue" to networkIssue.get(),
                "currencyMisMatch" to currencyMismatch.get(),
                "customerNotFound" to customerNotFound.get())
    }
}
