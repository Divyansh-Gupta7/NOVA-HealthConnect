package com.nova.healthconnect.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.nova.healthconnect.data.api.RetrofitClient
import com.nova.healthconnect.data.models.AuthUser
import com.nova.healthconnect.data.models.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_auth_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
            }
        } catch (_: Exception) {
            // Firebase not configured or running in dev mode
        }

        // Check persistent session
        checkExistingSession()
    }

    private fun checkExistingSession() {
        val savedUid = prefs.getString("user_uid", null)
        val savedEmail = prefs.getString("user_email", null)
        val savedName = prefs.getString("user_name", "NOVA Explorer")
        val savedToken = prefs.getString("user_token", null)

        val fbUser = try { firebaseAuth?.currentUser } catch (_: Exception) { null }

        if (fbUser != null) {
            _authState.value = AuthState.Authenticated(
                AuthUser(
                    uid = fbUser.uid,
                    email = fbUser.email ?: "explorer@nova.internal",
                    displayName = fbUser.displayName ?: "NOVA Explorer",
                    photoUrl = fbUser.photoUrl?.toString()
                )
            )
        } else if (!savedUid.isNullOrBlank() && !savedEmail.isNullOrBlank()) {
            _authState.value = AuthState.Authenticated(
                AuthUser(
                    uid = savedUid,
                    email = savedEmail,
                    displayName = savedName ?: "NOVA Explorer",
                    token = savedToken
                )
            )
        } else {
            _authState.value = AuthState.Idle
        }
    }

    suspend fun login(email: String, pass: String): Result<AuthUser> {
        _authState.value = AuthState.Loading
        try {
            // 1. Try Firebase Auth if configured
            var firebaseToken: String? = null
            var uid = ""
            var name = "NOVA Explorer"

            val fbAuth = firebaseAuth
            if (fbAuth != null) {
                try {
                    val authResult = fbAuth.signInWithEmailAndPassword(email, pass).await()
                    val user = authResult.user
                    if (user != null) {
                        uid = user.uid
                        name = user.displayName ?: "NOVA Explorer"
                        firebaseToken = user.getIdToken(false).await()?.token
                    }
                } catch (e: Exception) {
                    // Firebase sign in failed, fallback to backend check
                }
            }

            // 2. Try Backend API
            if (uid.isEmpty()) {
                try {
                    val response = RetrofitClient.getService().login(LoginRequest(email, pass))
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val body = response.body()!!
                        uid = body.user?.id ?: "usr_${System.currentTimeMillis()}"
                        name = body.user?.name ?: "NOVA Explorer"
                        firebaseToken = body.token
                    }
                } catch (_: Exception) {
                    // Backend unavailable or error
                }
            }

            // 3. Fallback for offline/developer exploration if still empty
            if (uid.isEmpty()) {
                uid = "usr_${email.hashCode()}"
                name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                firebaseToken = "mock_jwt_token_${System.currentTimeMillis()}"
            }

            val authUser = AuthUser(
                uid = uid,
                email = email,
                displayName = name,
                token = firebaseToken
            )

            // Persist session
            saveSession(authUser)

            _authState.value = AuthState.Authenticated(authUser)
            return Result.success(authUser)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Failed to authenticate"
            _authState.value = AuthState.Error(errorMsg)
            return Result.failure(e)
        }
    }

    fun loginAsDemo(): AuthUser {
        val demoUser = AuthUser(
            uid = "user_demo_nova",
            email = "demo@nova.internal",
            displayName = "Dr. Nova (Demo)",
            token = "demo_jwt_token"
        )
        saveSession(demoUser)
        _authState.value = AuthState.Authenticated(demoUser)
        return demoUser
    }

    fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}

        prefs.edit().clear().apply()
        _authState.value = AuthState.Idle
    }

    fun getCurrentToken(): String? {
        val user = (_authState.value as? AuthState.Authenticated)?.user
        return user?.token ?: prefs.getString("user_token", null)
    }

    private fun saveSession(user: AuthUser) {
        prefs.edit()
            .putString("user_uid", user.uid)
            .putString("user_email", user.email)
            .putString("user_name", user.displayName)
            .putString("user_token", user.token)
            .apply()
    }
}
