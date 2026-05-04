package com.financeapp.data.remote

object CurrencyApiService {
    suspend fun fetchRates(baseCurrency: String = "USD"): Map<String, Double>? {
        return try {
            val url = java.net.URL("https://open.er-api.com/v6/latest/$baseCurrency")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            val response = conn.inputStream.bufferedReader().readText()
            val json = org.json.JSONObject(response)
            val rates = json.getJSONObject("rates")
            val map = mutableMapOf<String, Double>()
            rates.keys().forEach { key -> map[key] = rates.getDouble(key) }
            map
        } catch (e: Exception) { null }
    }
}
