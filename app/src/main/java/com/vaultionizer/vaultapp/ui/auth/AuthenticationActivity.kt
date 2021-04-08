package com.vaultionizer.vaultapp.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vaultionizer.vaultapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        //supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))
    }
}