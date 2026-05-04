package com.financeapp.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Firebase Auth + Storage REST API client.
 * No Firebase SDK or google-services.json required.
 *
 * SETUP (one-time, free):
 * 1. Go to https://console.firebase.google.com → New Project
 * 2. Authentication → Sign-in method → Enable "Email/Password"
 * 3. Storage → Get started → choose region → Start in test mode
 * 4. Project Settings (gear) → General → Web API Key → copy it
 * 5. Storage → bucket URL (e.g. "your-project.appspot.com")
 * 6. Replace the two constants below with your values.
 */
object FirebaseSyncService {

    // TODO: Replace with your Firebase project's Web API Key
    // Found at: Firebase Console → Project Settings → General → Web API Key
    const val FIREBASE_API_KEY = "YOUR_FIREBASE_WEB_API_KEY"

    // TODO: Replace with your Firebase Storage bucket name
    // Found at: Firebase Console → Storage → bucket URL (without gs://)
    // Example: "my-finance-app-12345.appspot.com"
    const val FIREBASE_STORAGE_BUCKET = "YOUR_PROJECT_ID.appspot.com"

    private const val AUTH_BASE = "https://identitytoolkit.googleapis.com/v1/accounts"
    private const val STORAGE_BASE = "https://firebasestorage.googleapis.com/v0/b"

    data class AuthResult(
        val idToken: String,
        val localId: String,
        val email: String,
        val displayName: String = "",
        val expiresIn: Long = 3600
    )

    data class SyncError(val code: String, val message: String)

    sealed class AuthResponse {
        data class Success(val result: AuthResult) : AuthResponse()
        data class Error(val error: SyncError) : AuthResponse()
    }

    suspend fun signIn(email: String, password: String): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$AUTH_BASE:signInWithPassword?key=$FIREBASE_API_KEY")
            val body = """{"email":"$email","password":"$password","returnSecureToken":true}"""
            val response = postJson(url, body)
            parseAuthResponse(response)
        } catch (e: Exception) {
            AuthResponse.Error(SyncError("NETWORK_ERROR", e.message ?: "Network error"))
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String = ""): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val signUpUrl = URL("$AUTH_BASE:signUp?key=$FIREBASE_API_KEY")
            val signUpBody = """{"email":"$email","password":"$password","returnSecureToken":true}"""
            val signUpResponse = postJson(signUpUrl, signUpBody)
            val authResult = parseAuthResponse(signUpResponse)
            if (authResult is AuthResponse.Error) return@withContext authResult

            if (displayName.isNotBlank()) {
                val nameUrl = URL("$AUTH_BASE:update?key=$FIREBASE_API_KEY")
                val token = (authResult as AuthResponse.Success).result.idToken
                val nameBody = """{"idToken":"$token","displayName":"$displayName","returnSecureToken":true}"""
                postJson(nameUrl, nameBody)
            }

            authResult
        } catch (e: Exception) {
            AuthResponse.Error(SyncError("NETWORK_ERROR", e.message ?: "Network error"))
        }
    }

    suspend fun refreshToken(refreshToken: String): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://securetoken.googleapis.com/v1/token?key=$FIREBASE_API_KEY")
            val body = "grant_type=refresh_token&refresh_token=$refreshToken"
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.outputStream.write(body.toByteArray())
            val resp = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(resp)
            AuthResponse.Success(AuthResult(
                idToken = json.getString("id_token"),
                localId = json.getString("user_id"),
                email = "",
                expiresIn = json.optLong("expires_in", 3600)
            ))
        } catch (e: Exception) {
            AuthResponse.Error(SyncError("REFRESH_ERROR", e.message ?: "Token refresh failed"))
        }
    }

    suspend fun uploadBackup(idToken: String, uid: String, dbBytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$STORAGE_BASE/$FIREBASE_STORAGE_BUCKET/o?uploadType=media&name=users/$uid/finance_backup.db")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $idToken")
            conn.setRequestProperty("Content-Type", "application/octet-stream")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.outputStream.write(dbBytes)
            conn.responseCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun downloadBackup(idToken: String, uid: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val path = "users%2F$uid%2Ffinance_backup.db"
            val url = URL("$STORAGE_BASE/$FIREBASE_STORAGE_BUCKET/o/$path?alt=media")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $idToken")
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            if (conn.responseCode == 404) return@withContext null
            conn.inputStream.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getBackupInfo(idToken: String, uid: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val path = "users%2F$uid%2Ffinance_backup.db"
            val url = URL("$STORAGE_BASE/$FIREBASE_STORAGE_BUCKET/o/$path")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $idToken")
            conn.connectTimeout = 15000
            if (conn.responseCode == 404) return@withContext null
            JSONObject(conn.inputStream.bufferedReader().readText())
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendPasswordReset(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$AUTH_BASE:sendOobCode?key=$FIREBASE_API_KEY")
            val body = """{"requestType":"PASSWORD_RESET","email":"$email"}"""
            postJson(url, body)
            true
        } catch (e: Exception) { false }
    }

    private fun postJson(url: URL, body: String): String {
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.outputStream.write(body.toByteArray())
        return try {
            conn.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            conn.errorStream?.bufferedReader()?.readText() ?: "{}"
        }
    }

    private fun parseAuthResponse(json: String): AuthResponse {
        return try {
            val obj = JSONObject(json)
            if (obj.has("error")) {
                val err = obj.getJSONObject("error")
                val msg = err.optString("message", "Unknown error")
                AuthResponse.Error(SyncError(msg, friendlyErrorMessage(msg)))
            } else {
                AuthResponse.Success(AuthResult(
                    idToken = obj.getString("idToken"),
                    localId = obj.getString("localId"),
                    email = obj.optString("email", ""),
                    displayName = obj.optString("displayName", ""),
                    expiresIn = obj.optLong("expiresIn", 3600)
                ))
            }
        } catch (e: Exception) {
            AuthResponse.Error(SyncError("PARSE_ERROR", "Failed to parse response"))
        }
    }

    private fun friendlyErrorMessage(code: String): String = when {
        code.contains("EMAIL_EXISTS") -> "This email is already registered. Try signing in instead."
        code.contains("INVALID_PASSWORD") -> "Incorrect password. Please try again."
        code.contains("EMAIL_NOT_FOUND") || code.contains("INVALID_LOGIN_CREDENTIALS") -> "No account found with this email."
        code.contains("USER_DISABLED") -> "This account has been disabled."
        code.contains("TOO_MANY_ATTEMPTS") -> "Too many failed attempts. Try again later."
        code.contains("WEAK_PASSWORD") -> "Password must be at least 6 characters."
        code.contains("INVALID_EMAIL") -> "Please enter a valid email address."
        else -> code
    }
}
