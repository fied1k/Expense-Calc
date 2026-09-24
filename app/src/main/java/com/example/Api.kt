package com.example

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class NominatimAddress(
    val house_number: String? = null,
    val road: String? = null,
    val pedestrian: String? = null,
    val amenity: String? = null,
    val tourism: String? = null,
    val building: String? = null,
    val shop: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val municipality: String? = null,
    val hamlet: String? = null,
    val suburb: String? = null,
    val neighbourhood: String? = null,
    val quarter: String? = null,
    val residential: String? = null,
    val city_district: String? = null,
    val county: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null
)

@JsonClass(generateAdapter = true)
data class NominatimResult(
    val display_name: String,
    val lat: String,
    val lon: String,
    val address: NominatimAddress? = null
) {
    val cleanName: String
        get() = formatCleanAddress(display_name, address)
}

fun formatCleanAddress(displayName: String, address: NominatimAddress?): String {
    if (address != null) {
        val street = when {
            address.house_number != null && address.road != null -> "${address.house_number} ${address.road}"
            address.road != null -> address.road
            address.pedestrian != null -> address.pedestrian
            address.amenity != null -> address.amenity
            address.tourism != null -> address.tourism
            address.building != null -> address.building
            address.shop != null -> address.shop
            else -> null
        }
        val city = address.city ?: address.town ?: address.village ?: address.municipality ?: address.hamlet
        val state = address.state
        val postcode = address.postcode

        val stateAndZip = listOfNotNull(state, postcode).joinToString(" ").trim()

        val parts = mutableListOf<String>()
        if (!street.isNullOrBlank()) parts.add(street)
        if (!city.isNullOrBlank() && city != street) parts.add(city)
        if (stateAndZip.isNotBlank()) parts.add(stateAndZip)

        if (parts.isNotEmpty()) {
            return parts.joinToString(", ")
        }
    }

    val forbiddenCountyTerms = listOf("county", "parish", "borough")
    val forbiddenCountries = listOf("united states", "united states of america", "usa", "us")
    val neighborhoods = listOfNotNull(
        address?.suburb,
        address?.neighbourhood,
        address?.quarter,
        address?.residential,
        address?.city_district
    ).map { it.lowercase() }

    val rawParts = displayName.split(",").map { it.trim() }
    val filtered = rawParts.filter { part ->
        val lower = part.lowercase()
        if (forbiddenCountries.contains(lower)) return@filter false
        if (forbiddenCountyTerms.any { lower.contains(it) }) return@filter false
        if (neighborhoods.contains(lower)) return@filter false
        true
    }

    return if (filtered.isNotEmpty()) filtered.joinToString(", ") else displayName
}

@JsonClass(generateAdapter = true)
data class OsrmResponse(
    val code: String,
    val routes: List<OsrmRoute>? = null
)

@JsonClass(generateAdapter = true)
data class OsrmRoute(
    val distance: Double,
    val duration: Double
)

interface NominatimApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Query("addressdetails") addressdetails: Int = 1,
        @Query("countrycodes") countryCodes: String = "us"
    ): List<NominatimResult>
}

interface OsrmApi {
    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String, // format: "lon1,lat1;lon2,lat2"
        @Query("overview") overview: String = "false"
    ): OsrmResponse
}

object NetworkClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "CostCalculatorApp/1.0 (pcprokc@gmail.com)")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val nominatimApi: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }

    val osrmApi: OsrmApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OsrmApi::class.java)
    }
}
