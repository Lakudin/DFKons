package ru.lakuda.dfkons.data.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface CurrencyApi {
    @GET("daily_json.js")
    suspend fun getRates(): ExchangeRateResponse
}

data class ExchangeRateResponse(
    @SerializedName("Date")
    val date: String? = null,
    @SerializedName("Valute")
    val valute: Map<String, ValuteInfo> = emptyMap()
)

data class ValuteInfo(
    @SerializedName("ID") val id: String,
    @SerializedName("NumCode") val numCode: String,
    @SerializedName("CharCode") val charCode: String,
    @SerializedName("Nominal") val nominal: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("Value") val value: Double,
    @SerializedName("Previous") val previous: Double
)

object RetrofitInstance {
    private const val BASE_URL = "https://www.cbr-xml-daily.ru/"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val api: CurrencyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyApi::class.java)
    }
}