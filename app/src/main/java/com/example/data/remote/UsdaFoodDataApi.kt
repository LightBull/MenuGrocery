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
data class UsdaSearchResponse(
    @Json(name = "totalHits") val totalHits: Int? = 0,
    @Json(name = "foods") val foods: List<UsdaFoodItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class UsdaFoodItem(
    @Json(name = "fdcId") val fdcId: Long? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "brandOwner") val brandOwner: String? = null,
    @Json(name = "brandName") val brandName: String? = null,
    @Json(name = "foodNutrients") val foodNutrients: List<UsdaFoodNutrient>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class UsdaFoodNutrient(
    @Json(name = "nutrientName") val nutrientName: String? = null,
    @Json(name = "unitName") val unitName: String? = null,
    @Json(name = "value") val value: Double? = null
)

interface UsdaFoodDataApi {
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = "DEMO_KEY",
        @Query("pageSize") pageSize: Int = 15
    ): UsdaSearchResponse

    companion object {
        private const val BASE_URL = "https://api.nal.usda.gov/fdc/v1/"

        fun create(): UsdaFoodDataApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(UsdaFoodDataApi::class.java)
        }
    }
}
