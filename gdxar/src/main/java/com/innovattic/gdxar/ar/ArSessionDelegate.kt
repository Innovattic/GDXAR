package com.innovattic.gdxar.ar

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.SessionPausedException
import com.innovattic.gdxar.ar.graphics.ArCoreGraphicsCallback
import com.innovattic.gdxar.ar.scene.ArSessionProvider
import com.innovattic.gdxar.util.extension.TAG

/**
 * This class encapsulates ARCore session creation.
 *
 * To use this class create an instance in onCreate().
 */
class ArSessionDelegate(private val context: Context, lifecycle: Lifecycle) :
    LifecycleObserver, ArCoreGraphicsCallback, ArSessionProvider {

    private var session: Session? = null

    private var textureId: Int = -1
    private var rotation: Int = -1
    private var width: Int = -1
    private var height: Int = -1

    init {
        lifecycle.addObserver(this)
    }

    /**
     * Handle setting the display geometry.
     * The values are cached if they are set before the session is available.
     */
    override fun setDisplayGeometry(rotation: Int, width: Int, height: Int) {
        val session = session
        if (session != null) {
            session.setDisplayGeometry(rotation, width, height)
        } else {
            this.rotation = rotation
            this.width = width
            this.height = height
        }
    }

    /**
     * Handle setting the texture ID for the background image.
     * The value is cached if it is called before the session is available.
     */
    override fun setCameraTextureName(textureId: Int) {
        val session = session
        if (session != null) {
            session.setCameraTextureName(textureId)
            this.textureId = -1
        } else {
            this.textureId = textureId
        }
    }

    override fun update(): Frame? {
        val session = session
        if (session != null) {
            try {
                return session.update()
            } catch (e: CameraNotAvailableException) {
                Log.e(TAG, "Exception resuming session", e)
            } catch (e: SessionPausedException) {
                Log.e(TAG, "Exception resuming session", e)
            }
        }
        return null
    }

    /**
     * Gets the ARCore session. It can be null if the
     * permissions were not granted by the user or if the device does not support ARCore.
     */
    override fun getSession() = session

    private fun initializeARCore() {
        session = Session(context)

        // Set the graphics information if it was already passed in.
        if (textureId >= 0) {
            setCameraTextureName(textureId)
        }
        if (width > 0) {
            setDisplayGeometry(rotation, width, height)
        }
        try {
            session?.resume()
        } catch (e: CameraNotAvailableException) {
            Log.e(TAG, "Exception resuming session", e)
            return
        }
    }

    /**
     * Handle the onResume event.
     * This checks the permissions and initializes ARCore.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        val session = session
        if (session == null) {
            initializeARCore()
        } else {
            try {
                session.resume()
            } catch (e: CameraNotAvailableException) {
                Log.e(TAG, "Exception resuming session", e)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        session?.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        session?.close()
        session = null
    }
}
