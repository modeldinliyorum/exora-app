package com.exora.rates

/** Currency universe: chip order, rate-list priority and display names. */
object Currencies {

    /** Order the rate list is shown in. USD and EUR lead, so they sit at the top. */
    val ORDER = listOf(
        "USD", "EUR", "TRY", "GBP", "CHF", "JPY", "CAD", "AUD", "CNY", "AED",
        "SAR", "INR", "RUB", "SEK", "NOK", "DKK", "PLN", "MXN", "BRL", "ZAR",
        "KRW", "SGD", "HKD"
    )

    /** Currencies offered in the base selector. TRY first as the default base. */
    val SELECTABLE = listOf(
        "TRY", "USD", "EUR", "GBP", "CHF", "JPY", "CAD", "AUD", "CNY", "AED",
        "SAR", "INR", "RUB", "SEK", "NOK", "DKK", "PLN", "MXN", "BRL", "ZAR",
        "KRW", "SGD", "HKD"
    )

    val NAMES = mapOf(
        "TRY" to "Turkish Lira",
        "USD" to "US Dollar",
        "EUR" to "Euro",
        "GBP" to "British Pound",
        "CHF" to "Swiss Franc",
        "JPY" to "Japanese Yen",
        "CAD" to "Canadian Dollar",
        "AUD" to "Australian Dollar",
        "CNY" to "Chinese Yuan",
        "AED" to "UAE Dirham",
        "SAR" to "Saudi Riyal",
        "INR" to "Indian Rupee",
        "RUB" to "Russian Ruble",
        "SEK" to "Swedish Krona",
        "NOK" to "Norwegian Krone",
        "DKK" to "Danish Krone",
        "PLN" to "Polish Zloty",
        "MXN" to "Mexican Peso",
        "BRL" to "Brazilian Real",
        "ZAR" to "South African Rand",
        "KRW" to "South Korean Won",
        "SGD" to "Singapore Dollar",
        "HKD" to "Hong Kong Dollar"
    )

    fun name(code: String): String = NAMES[code] ?: code
}
