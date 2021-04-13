package io.pleo.antaeus.core.scheduler

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.config.CoreConfiguration
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.models.InvoiceStatus
import org.junit.jupiter.api.Test
import java.util.*

internal class SchedulingServiceTest {

    @Test
    fun scheduleMonthlyBilling() {
        val billingService = mockk<BillingService> {
            every { billInvoices(any(),any()) } returns Collections.emptyMap()
        }

        val coreConfig = mockk<CoreConfiguration> {
            every { cronRegex } returns "0/5 * * * * ?"
            every { retriesCount } returns 2
            every { retryIntervalInSec } returns 1
        }

        val billingRetryListener = BillingRetryListener(billingService, coreConfig)

        val billingScheduler = SchedulingService(billingRetryListener, billingService, coreConfig)
        billingScheduler.scheduleMonthlyBilling()
        Thread.sleep(7 * 1000)
        verify(exactly = 1) { billingService.billInvoices(InvoiceStatus.PENDING,false) }
        verify(exactly = 1) { billingService.billInvoices(InvoiceStatus.FAILURE,false) }
        verify(exactly = 1) { billingService.billInvoices(InvoiceStatus.FAILURE,true) }
    }
}
