package com.innovattic.gdxar.ar

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.badlogic.gdx.backends.android.DefaultAndroidInput
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy
import com.innovattic.gdxar.ar.graphics.ArCoreGraphics
import com.innovattic.gdxar.ar.scene.ArScene

/**
 * Android Fragment subclass that handles initializing ARCore and the underlying graphics engine
 * used for drawing 3d and 2d models in the context of the ARCore Frame. This class is based on the
 * libgdx library for Android game development.
 *
 * This Fragment assumes you've already ensured AR is safe to launch beforehand!
 *
 * Subclass this Fragment, and call [createArSceneView] in your implementation of [onViewCreated]
 * to obtain a [View] for your GDX and AR content.
 * Then use this View however you like (for example, place in it the layout).
 */
abstract class ArSceneFragment : AndroidFragmentApplication(), LifecycleOwner {

    private lateinit var sessionDelegate: ArSessionDelegate

    protected open val configuration: AndroidApplicationConfiguration =
        AndroidApplicationConfiguration()

    abstract val scene: ArScene

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionDelegate = ArSessionDelegate(requireContext(), lifecycle)
    }

    override fun initializeForView(
        listener: ApplicationListener,
        config: AndroidApplicationConfiguration
    ): View {
        super.initializeForView(listener, config)
        val resolutionStrategy = config.resolutionStrategy ?: FillResolutionStrategy()
        val graphics = ArCoreGraphics(this, sessionDelegate, config, resolutionStrategy)
        scene.setArCoreGraphics(graphics)
        scene.setSessionProvider(sessionDelegate)
        this.graphics = graphics

        val view = graphics.view
        input = DefaultAndroidInput(this, requireContext(), view, config)
        return view
    }

    protected fun createArSceneView(): View = initializeForView(scene, configuration)
}
