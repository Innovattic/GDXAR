package com.innovattic.gdxar.ar.graphics

import com.badlogic.gdx.backends.android.AndroidApplicationBase
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidGraphics
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy
import com.google.ar.core.Frame
import com.innovattic.gdxar.util.extension.getRotation
import java.util.concurrent.atomic.AtomicReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Extended AndroidGraphics that is ARCore aware.
 * This handles creating an OES Texture and passing it to the ARCore session.
 */
class ArCoreGraphics(
    private val application: AndroidApplicationBase,
    private val callback: ArCoreGraphicsCallback,
    config: AndroidApplicationConfiguration?,
    resolutionStrategy: ResolutionStrategy?
) : AndroidGraphics(application, config, resolutionStrategy) {

    private val renderer = ArCoreGraphicsRenderer()
    private val currentFrame: AtomicReference<Frame?> = AtomicReference(null)

    val backgroundTexture: Int
        get() = renderer.getTextureId()

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        val rotation = application.context.getRotation()
        callback.setDisplayGeometry(rotation, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig) {
        super.onSurfaceCreated(gl, config)
        renderer.createOnGlThread()
        callback.setCameraTextureName(renderer.getTextureId())
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        currentFrame.set(null)
    }

    fun getBackgroundVertices(frame: Frame?): FloatArray {
        return renderer.getVertices(frame)
    }

    /**
     * @return the current ARCore frame. This is reset at the end of the render loop.
     */
    fun getCurrentFrame(): Frame? {
        if (currentFrame.get() == null) {
            val frame = callback.update()
            currentFrame.compareAndSet(null, frame)
        }
        return currentFrame.get()
    }
}
