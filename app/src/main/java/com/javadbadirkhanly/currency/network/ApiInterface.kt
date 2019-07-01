package com.javadbadirkhanly.currency.network

import com.javadbadirkhanly.currency.network.data.Currency
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiInterface {

    @GET("latest")
    fun currencies(): Observable<Currency>

    companion object {
        fun create(): ApiInterface {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.exchangeratesapi.io")
                .build()

            return retrofit.create(ApiInterface::class.java)
        }
    }
}