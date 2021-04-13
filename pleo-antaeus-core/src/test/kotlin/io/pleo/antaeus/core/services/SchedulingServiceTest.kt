package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.config.CoreConfiguration
import io.pleo.antaeus.models.InvoiceStatus
import org.junit.jupiter.api.Test
import java.util.*

internal class SchedulingServiceTest {

    @Test
    fun scheduleMonthlyBilling() {
        val billingService = mockk<BillingService> {
            every { billPendingInvoices(InvoiceStatus.PENDING) } returns Collections.emptyMap()
        }

        val coreConfig = mockk<CoreConfiguration> {
            every { cronRegex } returns "0/5 * * * * ?"
        }

        val billingScheduler = SchedulingService(billingService, coreConfig)
        billingScheduler.scheduleMonthlyBilling()
        Thread.sleep(5 * 1000)
        verify(exactly = 1) { billingService.billPendingInvoices(InvoiceStatus.PENDING) }

    }
}
