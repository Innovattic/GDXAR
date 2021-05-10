package com.innovattic.gdxarsample.ui

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.innovattic.gdxarsample.R
import com.innovattic.gdxarsample.extension.TAG

class MainActivity : AppCompatActivity(R.layout.activity_main),
    AndroidFragmentApplication.Callbacks {

    override fun exit() {
        Log.v(TAG, "Goodbye")
    }
}
