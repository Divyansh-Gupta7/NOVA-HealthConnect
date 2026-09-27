package com.nova.healthconnect

import android.app.Application
import com.nova.healthconnect.auth.AuthManager
import com.nova.healthconnect.data.api.RetrofitClient
import com.nova.healthconnect.data.repository.NovaRepository
import com.nova.healthconnect.health.HealthConnectManager
import com.nova.healthconnect.health.HealthSyncManager

class NovaApplication : Application() {

    lateinit var authManager: AuthManager
        private set

    lateinit var healthConnectManager: HealthConnectManager
        private set

    lateinit var healthSyncManager: HealthSyncManager
        private set

    lateinit var novaRepository: NovaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        authManager = AuthManager(this)

        RetrofitClient.initialize(this) {
            authManager.getCurrentToken()
        }

        healthConnectManager = HealthConnectManager(this)
        healthSyncManager = HealthSyncManager(this, healthConnectManager)
        novaRepository = NovaRepository(this)

        // Schedule periodic background sync
        healthSyncManager.schedulePeriodicSync()
    }

    companion object {
        lateinit var instance: NovaApplication
            private set
    }
}