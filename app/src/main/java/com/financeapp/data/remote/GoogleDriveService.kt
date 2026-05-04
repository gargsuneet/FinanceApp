package com.financeapp.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Google Drive REST API helper.
 * Uses an OAuth2 access token obtained via Google Sign-In (play-services-auth).
 * No google-services.json / Google Services Gradle plugin needed.
 *
 * TODO: Replace GOOGLE_WEB_CLIENT_ID with your actual Web Client ID from Google Cloud Console.
 * See: https://console.cloud.google.com → APIs & Services → Credentials
 */
object GoogleDriveService {

    // TODO: Replace with your actual Web Client ID from Google Cloud Console
    // Format: "123456789-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com"
    const val GOOGLE_WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

    private const val DRIVE_FOLDER_NAME = "FinanceApp_Backup"
    private const val BACKUP_FILE_NAME = "finance_backup.db"
    private const val DRIVE_API_BASE = "https://www.googleapis.com/drive/v3"
    private const val DRIVE_UPLOAD_BASE = "https://www.googleapis.com/upload/drive/v3"

    /** Finds or creates the FinanceApp_Backup folder. Returns the folder ID, or null on failure. */
    suspend fun getOrCreateFolder(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val searchUrl = URL(
                "$DRIVE_API_BASE/files?q=name%3D%27$DRIVE_FOLDER_NAME%27+and+" +
                "mimeType%3D%27application%2Fvnd.google-apps.folder%27+and+trashed%3Dfalse" +
                "&fields=files(id%2Cname)"
            )
            val conn = (searchUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $accessToken")
                connectTimeout = 15000
                readTimeout = 15000
            }
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val files = json.getJSONArray("files")
            if (files.length() > 0) return@withContext files.getJSONObject(0).getString("id")

            // Create folder
            val createConn = (URL("$DRIVE_API_BASE/files").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }
            val body = """{"name":"$DRIVE_FOLDER_NAME","mimeType":"application/vnd.google-apps.folder"}"""
            createConn.outputStream.use { it.write(body.toByteArray()) }
            JSONObject(createConn.inputStream.bufferedReader().readText()).getString("id")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Uploads (or updates) the backup DB file to Drive. Returns true on success. */
    suspend fun uploadBackup(accessToken: String, dbBytes: ByteArray, folderId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val existingId = findBackupFile(accessToken, folderId)
                val uploadUrl: URL
                val method: String
                if (existingId != null) {
                    uploadUrl = URL("$DRIVE_UPLOAD_BASE/files/$existingId?uploadType=multipart")
                    method = "PATCH"
                } else {
                    uploadUrl = URL("$DRIVE_UPLOAD_BASE/files?uploadType=multipart")
                    method = "POST"
                }

                val boundary = "boundary_financeapp_${System.currentTimeMillis()}"
                val conn = (uploadUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = method
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
                    doOutput = true
                    connectTimeout = 30000
                    readTimeout = 30000
                }

                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val metadataJson = if (existingId != null) {
                    """{"name":"$BACKUP_FILE_NAME","description":"FinanceApp backup - $timestamp"}"""
                } else {
                    """{"name":"$BACKUP_FILE_NAME","description":"FinanceApp backup - $timestamp","parents":["$folderId"]}"""
                }

                conn.outputStream.use { out ->
                    out.write("--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n$metadataJson\r\n".toByteArray())
                    out.write("--$boundary\r\nContent-Type: application/octet-stream\r\n\r\n".toByteArray())
                    out.write(dbBytes)
                    out.write("\r\n--$boundary--".toByteArray())
                }
                conn.responseCode in 200..299
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    /** Downloads the latest backup from Drive. Returns the file bytes, or null on failure. */
    suspend fun downloadBackup(accessToken: String, folderId: String): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                val fileId = findBackupFile(accessToken, folderId) ?: return@withContext null
                val conn = (URL("$DRIVE_API_BASE/files/$fileId?alt=media").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    connectTimeout = 30000
                    readTimeout = 30000
                }
                conn.inputStream.readBytes()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    /** Lists backup files in the FinanceApp folder. Returns list of Pair(fileId, modifiedTime). */
    suspend fun listBackups(accessToken: String, folderId: String): List<Pair<String, String>> =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(
                    "$DRIVE_API_BASE/files?q=%27$folderId%27+in+parents+and+trashed%3Dfalse" +
                    "&fields=files(id%2Cname%2CmodifiedTime%2Csize)&orderBy=modifiedTime+desc"
                )
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                val files = JSONObject(conn.inputStream.bufferedReader().readText()).getJSONArray("files")
                (0 until files.length()).map { i ->
                    val f = files.getJSONObject(i)
                    Pair(f.getString("id"), f.optString("modifiedTime", "Unknown"))
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    private suspend fun findBackupFile(accessToken: String, folderId: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(
                    "$DRIVE_API_BASE/files?q=%27$folderId%27+in+parents+and+" +
                    "name%3D%27$BACKUP_FILE_NAME%27+and+trashed%3Dfalse&fields=files(id)"
                )
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                val files = JSONObject(conn.inputStream.bufferedReader().readText()).getJSONArray("files")
                if (files.length() > 0) files.getJSONObject(0).getString("id") else null
            } catch (e: Exception) {
                null
            }
        }
}
