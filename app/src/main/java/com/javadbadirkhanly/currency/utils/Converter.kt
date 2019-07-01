package com.javadbadirkhanly.currency.utils

import java.math.BigDecimal
import java.math.RoundingMode

class Converter {

    companion object {
        fun buyPln(usd: BigDecimal, pln: BigDecimal) : BigDecimal {
            val currency : BigDecimal = pln.div(usd)
            return currency.setScale(3, RoundingMode.HALF_UP)
        }

        fun sellPLN(pln: BigDecimal, usd: BigDecimal) : BigDecimal {
            val currency : BigDecimal = usd.div(pln)
            return currency.setScale(3, RoundingMode.HALF_UP)
        }
    }
}