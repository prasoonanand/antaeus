package io.pleo.antaeus.models

enum class InvoiceStatus {
    PENDING,
    PAID,
    CURRENCY_MISMATCH,
    CUSTOMER_NOT_FOUNT,
    CUSTOMER_REJECTED,
    FAILURE,
    ERROR
}
