package com.transitshield.app.data.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for the TransitShield backend.
 *
 * Default behavior targets the Android emulator host machine via 10.0.2.2.
 * A runtime host override can be provided from the login/settings flow for
 * connecting a real device to the backend running on the same Wi-Fi network.
 */
object RetrofitClient {

    const val EMULATOR_URL = "http://10.0.2.2:8080/api/"
    private const val DEFAULT_PORT = 8080
    private const val API_PATH = "api/"

    @Volatile
    var customBaseUrl: String? = null
        set(value) {
            field = normalizeBaseUrl(value)
        }

    val currentBaseUrl: String
        get() = customBaseUrl ?: EMULATOR_URL

    fun useEmulator() {
        customBaseUrl = null
    }

    fun setCustomHost(hostOrIp: String, port: Int = DEFAULT_PORT) {
        val trimmed = hostOrIp.trim()
        customBaseUrl = if (trimmed.isBlank()) {
            null
        } else {
            "http://$trimmed:$port/$API_PATH"
        }
    }

    private fun normalizeBaseUrl(url: String?): String? {
        val trimmed = url?.trim().orEmpty()
        if (trimmed.isBlank()) return null

        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }

        val withTrailingSlash = if (withScheme.endsWith("/")) withScheme else "$withScheme/"
        return if (withTrailingSlash.endsWith(API_PATH)) {
            withTrailingSlash
        } else {
            "$withTrailingSlash$API_PATH"
        }
    }

    private val dynamicUrlInterceptor = okhttp3.Interceptor { chain ->
        var request = chain.request()
        val overrideUrl = customBaseUrl?.toHttpUrlOrNull()

        if (overrideUrl != null) {
            val originalUrl = request.url
            val newUrl = originalUrl.newBuilder()
                .scheme(overrideUrl.scheme)
                .host(overrideUrl.host)
                .port(overrideUrl.port)
                .build()

            request = request.newBuilder()
                .url(newUrl)
                .build()
        }

        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Volatile
    var authToken: String? = null

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()

        authToken?.takeIf { it.isNotBlank() }?.let {
            builder.header("Authorization", "Bearer $it")
        }

        chain.proceed(builder.build())
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(dynamicUrlInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(EMULATOR_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
