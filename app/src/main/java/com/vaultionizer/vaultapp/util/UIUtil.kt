package com.vaultionizer.vaultapp.util

import android.view.View

fun boolToVisibility(visible: Boolean, falseDefault: Int = View.INVISIBLE) =
    when (visible) {
        true -> View.VISIBLE
        else -> falseDefault
    }