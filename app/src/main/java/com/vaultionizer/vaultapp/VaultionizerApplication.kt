package com.vaultionizer.vaultapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.mikepenz.iconics.Iconics.init
import com.mikepenz.iconics.Iconics.registerFont
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.OutlinedGoogleMaterial
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class VaultionizerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        init(applicationContext)
        registerFont(OutlinedGoogleMaterial)
        registerFont(FontAwesome)

        WorkManager.getInstance(applicationContext).pruneWork()
    }
}