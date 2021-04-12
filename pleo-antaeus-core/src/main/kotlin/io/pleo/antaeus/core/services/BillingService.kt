package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import java.time.LocalDate

class BillingService(
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
) {
    private val logger = KotlinLogging.logger("BillingService")

    fun billPendingInvoices(): Map<String, Int> {
        var page = 0;
        logger.info("Running the schedule " + LocalDate.now() + " at " + System.currentTimeMillis())
        var paid = 0;
        var customerRejected = 0;
        var currencyMismatch = 0;
        var customerNotFound = 0;
        var networkIssue = 0
        while (true) {
            logger.info("Fetching data for Page $page")
            val pendingInvoices = invoiceService.fetchAllPendingInvoices(page++)
            if (pendingInvoices.isEmpty()) {
                break
            }
            for (invoice in pendingInvoices) {
                try {
                    val success = paymentProvider.charge(invoice)
                    if (!success) {
                        invoice.status = InvoiceStatus.CUSTOMER_REJECTED
                        customerRejected++
                    } else {
                        invoice.status = InvoiceStatus.PAID
                        paid++
                    }
                } catch (ex: CurrencyMismatchException) {
                    invoice.status = InvoiceStatus.CURRENCY_MISMATCH
                    currencyMismatch++
                    logger.error { ex }
                } catch (ex: CustomerNotFoundException) {
                    invoice.status = InvoiceStatus.CUSTOMER_NOT_FOUNT
                    customerNotFound++
                    logger.error { ex }
                } catch (ex: NetworkException) {
                    invoice.status = InvoiceStatus.FAILURE//TODO retry logic
                    networkIssue++
                    logger.error { ex }
                }catch (ex: Exception) {
                    invoice.status = InvoiceStatus.FAILURE//TODO retry logic
                    logger.error { ex }
                }
                logger.info("Status " + invoice.status + " for customer " + invoice.customerId + " for invoice " + invoice.id)
            }
            invoiceService.updateBatchStatus(pendingInvoices)
        }
        logger.info("Finishing the schedule " + LocalDate.now() + " till " + System.currentTimeMillis())
        return mapOf("paid" to paid,
                "customerRejected" to customerRejected,
                "networkIssue" to networkIssue,
                "currencyMisMatch" to currencyMismatch,
                "customerNotFound" to customerNotFound)
    }
}
