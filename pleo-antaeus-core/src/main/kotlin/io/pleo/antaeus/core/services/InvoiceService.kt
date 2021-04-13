/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetchAllInvoicesOnStatus(status: InvoiceStatus, page: Int = 0): List<Invoice> {
        return dal.fetchAllInvoicesOnStatus(status, page)
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun updateBatchStatus(updatedInvoices: List<Invoice>) {
        dal.batchUpdateInvoice(updatedInvoices);
    }

    fun updateInvoiceStatus(invoiceId: Int, status: InvoiceStatus): Invoice? {
        return  dal.updateInvoiceStatus(invoiceId, status)?: throw InvoiceNotFoundException(invoiceId)
    }
}
