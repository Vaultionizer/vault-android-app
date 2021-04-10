package com.vaultionizer.vaultapp

import android.app.Application
import androidx.work.Configuration
import com.vaultionizer.vaultapp.util.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VaultionizerApplication : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setMaxSchedulerLimit(Constants.WORK_MANAGER_SCHEDULER_LIMIT)
            .build()
}