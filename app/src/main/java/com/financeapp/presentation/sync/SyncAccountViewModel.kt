package com.financeapp.presentation.sync

import android.content.Intent
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.financeapp.FinanceApplication
import com.financeapp.data.local.dataStore
import com.financeapp.data.remote.FirebaseSyncService
import com.financeapp.notification.CloudSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class SyncScreenMode { SIGN_IN, CREATE_ACCOUNT, SYNCED }

data class SyncUiState(
    val mode: SyncScreenMode = SyncScreenMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSignedIn: Boolean = false,
    val userEmail: String = "",
    val userDisplayName: String = "",
    val lastSyncTime: String? = null,
    val autoSyncEnabled: Boolean = false,
    val isSyncing: Boolean = false,
    val isRestoring: Boolean = false,
    val showRestoreConfirm: Boolean = false,
    val showSignOutConfirm: Boolean = false,
    val showForgotPassword: Boolean = false,
    val forgotPasswordEmail: String = "",
    val resetSent: Boolean = false
)

class SyncAccountViewModel(private val app: FinanceApplication) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    companion object {
        private val SYNC_ID_TOKEN = stringPreferencesKey("sync_id_token")
        private val SYNC_REFRESH_TOKEN = stringPreferencesKey("sync_refresh_token")
        private val SYNC_UID = stringPreferencesKey("sync_uid")
        private val SYNC_EMAIL = stringPreferencesKey("sync_email")
        private val SYNC_DISPLAY_NAME = stringPreferencesKey("sync_display_name")
        private val SYNC_LAST_TIME = stringPreferencesKey("sync_last_time")
        private val SYNC_AUTO = booleanPreferencesKey("sync_auto_enabled")
        private val SYNC_TOKEN_EXPIRY = stringPreferencesKey("sync_token_expiry")

        fun Factory(app: FinanceApplication) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SyncAccountViewModel(app) as T
        }
    }

    init {
        viewModelScope.launch {
            val prefs = app.dataStore.data.first()
            val token = prefs[SYNC_ID_TOKEN] ?: ""
            val email = prefs[SYNC_EMAIL] ?: ""
            val autoSync = prefs[SYNC_AUTO] ?: false
            val lastSync = prefs[SYNC_LAST_TIME]
            val displayName = prefs[SYNC_DISPLAY_NAME] ?: ""
            if (token.isNotBlank() && email.isNotBlank()) {
                _uiState.update {
                    it.copy(
                        isSignedIn = true,
                        mode = SyncScreenMode.SYNCED,
                        userEmail = email,
                        userDisplayName = displayName,
                        lastSyncTime = lastSync,
                        autoSyncEnabled = autoSync
                    )
                }
            }
        }
    }

    fun setEmail(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun setPassword(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }
    fun setDisplayName(v: String) = _uiState.update { it.copy(displayName = v) }
    fun togglePasswordVisible() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun switchToCreateAccount() = _uiState.update { it.copy(mode = SyncScreenMode.CREATE_ACCOUNT, errorMessage = null) }
    fun switchToSignIn() = _uiState.update { it.copy(mode = SyncScreenMode.SIGN_IN, errorMessage = null) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }
    fun showRestoreConfirm() = _uiState.update { it.copy(showRestoreConfirm = true) }
    fun hideRestoreConfirm() = _uiState.update { it.copy(showRestoreConfirm = false) }
    fun showSignOutConfirm() = _uiState.update { it.copy(showSignOutConfirm = true) }
    fun hideSignOutConfirm() = _uiState.update { it.copy(showSignOutConfirm = false) }
    fun showForgotPassword() = _uiState.update { it.copy(showForgotPassword = true, forgotPasswordEmail = _uiState.value.email) }
    fun hideForgotPassword() = _uiState.update { it.copy(showForgotPassword = false, resetSent = false) }
    fun setForgotEmail(v: String) = _uiState.update { it.copy(forgotPasswordEmail = v) }

    fun signIn() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter email and password") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = FirebaseSyncService.signIn(state.email.trim(), state.password)
            when (result) {
                is FirebaseSyncService.AuthResponse.Success -> {
                    saveAuthState(result.result)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignedIn = true,
                            mode = SyncScreenMode.SYNCED,
                            userEmail = result.result.email,
                            userDisplayName = result.result.displayName.ifBlank { result.result.email.substringBefore("@") },
                            password = ""
                        )
                    }
                }
                is FirebaseSyncService.AuthResponse.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    fun createAccount() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter email and password") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = FirebaseSyncService.signUp(state.email.trim(), state.password, state.displayName)
            when (result) {
                is FirebaseSyncService.AuthResponse.Success -> {
                    saveAuthState(result.result)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignedIn = true,
                            mode = SyncScreenMode.SYNCED,
                            userEmail = result.result.email,
                            userDisplayName = state.displayName.ifBlank { result.result.email.substringBefore("@") },
                            password = ""
                        )
                    }
                }
                is FirebaseSyncService.AuthResponse.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.forgotPasswordEmail
        if (email.isBlank()) return
        viewModelScope.launch {
            FirebaseSyncService.sendPasswordReset(email)
            _uiState.update { it.copy(resetSent = true) }
        }
    }

    fun uploadToCloud() {
        _uiState.update { it.copy(isSyncing = true, errorMessage = null) }
        viewModelScope.launch {
            val prefs = app.dataStore.data.first()
            val token = getValidToken(prefs) ?: run {
                _uiState.update { it.copy(isSyncing = false, errorMessage = "Session expired. Please sign in again.", isSignedIn = false, mode = SyncScreenMode.SIGN_IN) }
                return@launch
            }
            val uid = prefs[SYNC_UID] ?: ""
            val dbFile = app.getDatabasePath("finance_database")
            if (!dbFile.exists()) {
                _uiState.update { it.copy(isSyncing = false, errorMessage = "Database file not found") }
                return@launch
            }
            val success = FirebaseSyncService.uploadBackup(token, uid, dbFile.readBytes())
            if (success) {
                val timestamp = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
                app.dataStore.edit { it[SYNC_LAST_TIME] = timestamp }
                _uiState.update { it.copy(isSyncing = false, lastSyncTime = timestamp, successMessage = "✓ Data synced to cloud") }
            } else {
                _uiState.update { it.copy(isSyncing = false, errorMessage = "Sync failed. Check your internet connection.") }
            }
        }
    }

    fun downloadFromCloud() {
        _uiState.update { it.copy(isRestoring = true, errorMessage = null, showRestoreConfirm = false) }
        viewModelScope.launch {
            val prefs = app.dataStore.data.first()
            val token = getValidToken(prefs) ?: run {
                _uiState.update { it.copy(isRestoring = false, errorMessage = "Session expired. Please sign in again.", isSignedIn = false, mode = SyncScreenMode.SIGN_IN) }
                return@launch
            }
            val uid = prefs[SYNC_UID] ?: ""
            val bytes = FirebaseSyncService.downloadBackup(token, uid)
            if (bytes == null) {
                _uiState.update { it.copy(isRestoring = false, errorMessage = "No backup found in the cloud for this account.") }
                return@launch
            }
            try {
                val dbFile = app.getDatabasePath("finance_database")
                app.database.close()
                dbFile.writeBytes(bytes)
                val intent = app.packageManager.getLaunchIntentForPackage(app.packageName)!!
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                app.startActivity(intent)
                android.os.Process.killProcess(android.os.Process.myPid())
            } catch (e: Exception) {
                _uiState.update { it.copy(isRestoring = false, errorMessage = "Restore failed: ${e.message}") }
            }
        }
    }

    fun toggleAutoSync() {
        val newValue = !_uiState.value.autoSyncEnabled
        viewModelScope.launch {
            app.dataStore.edit { it[SYNC_AUTO] = newValue }
            if (newValue) {
                WorkManager.getInstance(app).enqueueUniquePeriodicWork(
                    "cloud_auto_sync",
                    ExistingPeriodicWorkPolicy.KEEP,
                    PeriodicWorkRequestBuilder<CloudSyncWorker>(1, TimeUnit.HOURS).build()
                )
            } else {
                WorkManager.getInstance(app).cancelUniqueWork("cloud_auto_sync")
            }
            _uiState.update { it.copy(autoSyncEnabled = newValue) }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            app.dataStore.edit { prefs ->
                prefs.remove(SYNC_ID_TOKEN)
                prefs.remove(SYNC_REFRESH_TOKEN)
                prefs.remove(SYNC_UID)
                prefs.remove(SYNC_EMAIL)
                prefs.remove(SYNC_DISPLAY_NAME)
            }
            WorkManager.getInstance(app).cancelUniqueWork("cloud_auto_sync")
            _uiState.update { SyncUiState(mode = SyncScreenMode.SIGN_IN) }
        }
    }

    private suspend fun getValidToken(prefs: androidx.datastore.preferences.core.Preferences): String? {
        val token = prefs[SYNC_ID_TOKEN] ?: return null
        val expiry = prefs[SYNC_TOKEN_EXPIRY]?.toLongOrNull() ?: 0L
        if (System.currentTimeMillis() < expiry - 60_000) return token
        val refreshToken = prefs[SYNC_REFRESH_TOKEN] ?: return token
        val refreshResult = FirebaseSyncService.refreshToken(refreshToken)
        return when (refreshResult) {
            is FirebaseSyncService.AuthResponse.Success -> {
                app.dataStore.edit {
                    it[SYNC_ID_TOKEN] = refreshResult.result.idToken
                    it[SYNC_TOKEN_EXPIRY] = (System.currentTimeMillis() + refreshResult.result.expiresIn * 1000).toString()
                }
                refreshResult.result.idToken
            }
            is FirebaseSyncService.AuthResponse.Error -> token
        }
    }

    private suspend fun saveAuthState(result: FirebaseSyncService.AuthResult) {
        app.dataStore.edit { prefs ->
            prefs[SYNC_ID_TOKEN] = result.idToken
            prefs[SYNC_UID] = result.localId
            prefs[SYNC_EMAIL] = result.email
            prefs[SYNC_DISPLAY_NAME] = result.displayName.ifBlank { result.email.substringBefore("@") }
            prefs[SYNC_TOKEN_EXPIRY] = (System.currentTimeMillis() + result.expiresIn * 1000).toString()
        }
    }
}
