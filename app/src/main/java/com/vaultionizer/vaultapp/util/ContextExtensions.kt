package com.vaultionizer.vaultapp.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.res.getDrawableOrThrow

// From https://stackoverflow.com/questions/57661168/textinputlayout-with-loading-indicator
fun Context.getProgressBarDrawable(): Drawable {
    val value = TypedValue()
    theme.resolveAttribute(android.R.attr.progressBarStyleSmall, value, false)
    val progressBarStyle = value.data
    val attributes = intArrayOf(android.R.attr.indeterminateDrawable)
    val array = obtainStyledAttributes(progressBarStyle, attributes)
    val drawable = array.getDrawableOrThrow(0)
    array.recycle()
    return drawable
}