package com.javadbadirkhanly.currency.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.javadbadirkhanly.currency.R
import com.javadbadirkhanly.currency.network.ApiInterface
import com.javadbadirkhanly.currency.network.data.Rates
import com.javadbadirkhanly.currency.utils.Converter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), TextWatcher {

    private val apiInterface by lazy { ApiInterface.create() }

    private var disposables: MutableList<Disposable> = mutableListOf()

    private var usdToPln: BigDecimal? = null
    private var plnToUsd: BigDecimal? = null

    private var focusedView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        startCurrencyInterval()

        etUSD.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusedView = v
                Timber.d("focused view is USD")
            }
        }
        etPLN.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusedView = v
                Timber.d("focused view is PLN")
            }
        }

        etUSD.addTextChangedListener(this)
        etPLN.addTextChangedListener(this)
    }

    private fun startCurrencyInterval() {
        disposables.add(Observable.interval(0, 60, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::getCurrencies, this::handleError)
        )
    }

    private fun getCurrencies(aLong: Long) {
        disposables.add(
            apiInterface.currencies().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Timber.d("rates: %s", result.toString())
                    updateRates(result.rates)

                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    private fun handleError(throwable: Throwable) {
        Timber.e(throwable)
    }

    private fun updateRates(rates: Rates) {
        usdToPln = Converter.buyPln(rates.USD, rates.PLN)
        plnToUsd = Converter.sellPLN(rates.PLN, rates.USD)

        Timber.d("usdToPln: %s", usdToPln)
        Timber.d("plnToUsd: %s", plnToUsd)


        tvUSDInfo.text = getString(R.string.usd_pln, usdToPln)
        tvPLNInfo.text = getString(R.string.pln_usd, plnToUsd)
    }

    private fun showErrorDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.alert)
        builder.setMessage(R.string.error)
        builder.setCancelable(true)
        builder.setPositiveButton(android.R.string.ok) {
                dialog, _ -> dialog.dismiss()
        }
        builder.show()
    }

    override fun afterTextChanged(s: Editable?) {
        if (usdToPln == null || plnToUsd == null) {
            showErrorDialog()
            return
        }
        etUSD.removeTextChangedListener(this)
        etPLN.removeTextChangedListener(this)

        if (focusedView == etUSD) {
            if (s.toString().isNotEmpty()) {
                val usd = s.toString().toBigDecimal()

                val result = usd.multiply(usdToPln)

                etPLN.setText(result.stripTrailingZeros().toPlainString())
            } else {
                etPLN.text.clear()
            }
        } else if (focusedView == etPLN) {
            if (s.toString().isNotEmpty()) {
                val pln = s.toString().toBigDecimal()

                val result = pln.multiply(plnToUsd)

                etUSD.setText(result.stripTrailingZeros().toPlainString())
            } else {
                etUSD.text.clear()
            }
        }

        etUSD.addTextChangedListener(this)
        etPLN.addTextChangedListener(this)

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        var i = 0

        disposables.forEach {
            it.dispose()
            Timber.d("disposed: %d", i)
            i++
        }
    }
}
