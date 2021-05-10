package com.innovattic.gdxar.util.extension

import android.content.Context
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.core.content.getSystemService

fun Context.getRotation(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display?.rotation
    } else {
        @Suppress("DEPRECATION")
        getSystemService<WindowManager>()?.defaultDisplay?.rotation
    } ?: Surface.ROTATION_0
}
