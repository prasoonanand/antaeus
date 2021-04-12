package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import java.util.stream.IntStream
import kotlin.collections.ArrayList

internal class BillingServiceTest {

    @Test
    fun billPendingInvoicesNoInvoices() {
        val invoiceService = mockk<InvoiceService> {
            every { fetchAllPendingInvoices(0) } returns Collections.emptyList()
        }
        val paymentProvider = mockk<PaymentProvider>() {
            every { charge(any()) } returns true
        }

        val billingService = BillingService(paymentProvider, invoiceService)
        val processed = billingService.billPendingInvoices()
        assertResult(0, 0, 0, 0, 0, processed)
        verify (exactly = 0) { invoiceService.updateBatchStatus(any()) }
    }

    @Test
    fun billPendingInvoicesAllPaid() {
        val invoices = generateInvoices(5)
        val invoiceService = mockk<InvoiceService> {
            every { fetchAllPendingInvoices(0) } returns invoices
            every { fetchAllPendingInvoices(1) } returns Collections.emptyList()
            every { updateBatchStatus(any()) } returns Unit
        }
        val paymentProvider = mockk<PaymentProvider>() {
            every { charge(any()) } returns true
        }

        val billingService = BillingService(paymentProvider, invoiceService)
        val processed = billingService.billPendingInvoices()
        assertResult(5, 0, 0, 0, 0, processed)
        verify (exactly = 1) { invoiceService.updateBatchStatus(any()) }
    }

    @Test
    fun billPendingInvoicesAllRejected() {
        val invoices = generateInvoices(5)
        val invoiceService = mockk<InvoiceService> {
            every { fetchAllPendingInvoices(0) } returns invoices
            every { fetchAllPendingInvoices(1) } returns Collections.emptyList()
            every { updateBatchStatus(any()) } returns Unit
        }
        val paymentProvider = mockk<PaymentProvider>() {
            every { charge(any()) } returns false
        }

        val billingService = BillingService(paymentProvider, invoiceService)
        val processed = billingService.billPendingInvoices()
        assertResult(0, 5, 0, 0, 0, processed)
        verify (exactly = 1) { invoiceService.updateBatchStatus(any()) }
    }

    @Test
    fun billPendingInvoicesOneOfAll() {
        val invoices = generateInvoices(5)
        val invoiceService = mockk<InvoiceService> {
            every { fetchAllPendingInvoices(0) } returns invoices
            every { fetchAllPendingInvoices(1) } returns Collections.emptyList()
            every { updateBatchStatus(any()) } returns Unit
        }
        val paymentProvider = mockk<PaymentProvider>() {
            every { charge(invoices[0]) } returns true
            every { charge(invoices[1]) } returns false
            every { charge(invoices[2]) } throws NetworkException()
            every { charge(invoices[3]) } throws CurrencyMismatchException(3, 3)
            every { charge(invoices[4]) } throws CustomerNotFoundException(4)
        }

        val billingService = BillingService(paymentProvider, invoiceService)
        val processed = billingService.billPendingInvoices()
        assertResult(1, 1, 1, 1, 1, processed)
        verify (exactly = 1) { invoiceService.updateBatchStatus(any()) }
    }

    /**
     * Here we can see the the same result is processed twice but this will never happen as
     * uniqueness is being handled at DB end.
     */
    @Test
    fun billPendingInvoicesOneOfAllAndUpdate2Times() {
        val invoices = generateInvoices(5)
        val invoiceService = mockk<InvoiceService> {
            every { fetchAllPendingInvoices(0) } returns invoices
            every { fetchAllPendingInvoices(1) } returns invoices
            every { fetchAllPendingInvoices(2) } returns Collections.emptyList()
            every { updateBatchStatus(any()) } returns Unit
        }
        val paymentProvider = mockk<PaymentProvider>() {
            every { charge(invoices[0]) } returns true
            every { charge(invoices[1]) } returns false
            every { charge(invoices[2]) } throws NetworkException()
            every { charge(invoices[3]) } throws CurrencyMismatchException(3, 3)
            every { charge(invoices[4]) } throws CustomerNotFoundException(4)
        }

        val billingService = BillingService(paymentProvider, invoiceService)
        val processed = billingService.billPendingInvoices()
        assertResult(2, 2, 2, 2, 2, processed)
        verify (exactly = 2) { invoiceService.updateBatchStatus(any()) }
    }

    private fun generateInvoices(i: Int): List<Invoice> {
        val  invoices = ArrayList<Invoice>(i)
        IntStream.range(0, i).forEach {
            invoices.add(Invoice(it,it, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PENDING))
        }
        return invoices
    }


    private fun assertResult(paid: Int, customerRejected: Int, networkIssue: Int, currencyMisMatch: Int, customerNotFound: Int, processed: Map<String, Int>) {
        assert(processed["paid"] == paid)
        assert(processed["customerRejected"] == customerRejected)
        assert(processed["networkIssue"] == networkIssue)
        assert(processed["currencyMisMatch"] == currencyMisMatch)
        assert(processed["customerNotFound"] == customerNotFound)
    }
}
