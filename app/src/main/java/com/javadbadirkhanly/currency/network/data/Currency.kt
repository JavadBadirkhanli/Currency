package com.javadbadirkhanly.currency.network.data

import java.math.BigDecimal

data class Rates(

    val PLN: BigDecimal,
    val USD: BigDecimal
)

data class Currency(val rates: Rates)
