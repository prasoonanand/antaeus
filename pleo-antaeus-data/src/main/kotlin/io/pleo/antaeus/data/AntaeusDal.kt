/*
    Implements the data access layer (DAL).
    The data access layer generates and executes requests to the database.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.data.config.DBConfiguration
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.transactions.transaction

class AntaeusDal(private val db: Database, private val config: DBConfiguration) {
    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            // Returns the first invoice with matching id.
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    fun fetchPendingInvoices(page: Int = 0): List<Invoice> {
        require(page >= 0) { "Page size should be greater than or equal to 0" }
        val batchSize = config.dbBatchSize;
        val offset = page * batchSize;

        return transaction(db) {
            InvoiceTable
                    .select(InvoiceTable.status.eq(InvoiceStatus.PENDING.name))
                    .limit(batchSize, offset)
                    .map { it.toInvoice() }
        }
    }

    fun batchUpdateInvoice(invoices: List<Invoice>) {
        transaction(db) {
            BatchUpdateStatement(InvoiceTable).apply {
                invoices.forEach {
                    addBatch(EntityID(it.id, InvoiceTable))
                    this[InvoiceTable.status] = it.status.name
                }
                execute(this@transaction)
            }
        }
    }

    fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus = InvoiceStatus.PENDING): Invoice? {
        val id = transaction(db) {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                } get InvoiceTable.id
        }

        return fetchInvoice(id.value)
    }

    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(currency: Currency): Customer? {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id)
    }

    fun updateInvoiceStatus(invoiceId: Int, status: InvoiceStatus): Invoice? {
        val id = transaction(db) {
            InvoiceTable.update({InvoiceTable.id eq invoiceId}){
                it[this.status] = status.name
            }
        }
        return if(id == 0) null else fetchInvoice(invoiceId)
    }
}
