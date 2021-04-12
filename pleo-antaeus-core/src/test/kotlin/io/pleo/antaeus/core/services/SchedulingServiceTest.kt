package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.config.CoreConfiguration
import org.junit.jupiter.api.Test
import java.util.*

internal class SchedulingServiceTest {

    @Test
    fun scheduleMonthlyBilling() {
        val billingService = mockk<BillingService> {
            every { billPendingInvoices() } returns Collections.emptyMap()
        }

        val coreConfig = mockk<CoreConfiguration> {
            every { cronRegex } returns "0/3 * * * * ?"
        }

        val billingScheduler = SchedulingService(billingService, coreConfig)
        billingScheduler.scheduleMonthlyBilling()
        Thread.sleep(7 * 1000)
        verify(exactly = 2) { billingService.billPendingInvoices() }

    }
}
