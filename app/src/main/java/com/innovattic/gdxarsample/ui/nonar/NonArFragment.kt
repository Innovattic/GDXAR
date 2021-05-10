package com.innovattic.gdxarsample.ui.nonar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.badlogic.gdx.backends.android.AndroidFragmentApplication

class NonArFragment : AndroidFragmentApplication() {
    private val scene = NonArScene()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = initializeForView(scene)
}
