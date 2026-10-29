package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenFoodFactsResponse(
    @Json(name = "count") val count: Int? = 0,
    @Json(name = "products") val products: List<ProductDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "code") val code: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "brands") val brands: String? = null,
    @Json(name = "image_front_small_url") val imageUrl: String? = null,
    @Json(name = "nutriments") val nutriments: NutrimentsDto? = null
)

@JsonClass(generateAdapter = true)
data class NutrimentsDto(
    @Json(name = "energy-kcal_100g") val energyKcal100g: Double? = null,
    @Json(name = "energy-kcal") val energyKcal: Double? = null,
    @Json(name = "proteins_100g") val proteins100g: Double? = null,
    @Json(name = "carbohydrates_100g") val carbohydrates100g: Double? = null,
    @Json(name = "fat_100g") val fat100g: Double? = null
)

interface OpenFoodFactsApi {
    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") terms: String,
        @Query("search_simple") simple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("fields") fields: String = "code,product_name,brands,nutriments,image_front_small_url"
    ): OpenFoodFactsResponse

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/"

        fun create(): OpenFoodFactsApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OpenFoodFactsApi::class.java)
        }
    }
}
