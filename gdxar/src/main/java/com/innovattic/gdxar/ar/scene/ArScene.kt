package com.innovattic.gdxar.ar.scene

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider
import com.badlogic.gdx.math.Matrix4
import com.google.ar.core.Frame
import com.innovattic.gdxar.ar.graphics.ArCoreGraphics

/**
 * ARCoreScene is the base class for the scene to render. Application specific scenes extend this
 * class to handle input and create and manipulate models which are rendered in a batch at the end
 * of each frame.
 *
 * This class handles the basic boilerplate of rendering the background image, moving the camera
 * based on the ARCore frame pose, and basic batch rendering.
 */
abstract class ArScene : ApplicationListener {

    /** Renderer for the camera image which is the background for the ARCore app. */
    private lateinit var renderer: ArSceneRenderer

    /**
     * Camera controlled by the ARCore pose. This is used to determine where the user is looking.
     */
    private lateinit var camera: PerspectiveCamera

    /** Drawing batch. */
    private lateinit var modelBatch: ModelBatch

    /** Must be externally provided shortly after creation, before usage */
    private lateinit var arCoreGraphics: ArCoreGraphics

    /** Must be externally provided shortly after creation, before usage */
    private lateinit var sessionProvider: ArSessionProvider

    /**
     * Called to render the scene.
     * @param frame - The current ARCore frame.
     */
    protected abstract fun render(frame: Frame, modelBatch: ModelBatch)

    /**
     * Shader provider for creating shaders that are used by custom materials.
     * Can be overridden to inject other shaders.
     */
    protected open fun createShaderProvider(): ShaderProvider {
        return DefaultShaderProvider()
    }

    override fun create() {
        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.position.set(0f, 1.6f, 0f)
        camera.lookAt(0f, 0f, 1f)
        camera.near = .01f
        camera.far = 30f
        camera.update()
        renderer = ArSceneRenderer(arCoreGraphics)
        modelBatch = ModelBatch(createShaderProvider())
    }

    override fun render() {

        // Boiler plate rendering code goes here, the intent is that this sets up the scene object,
        // Application specific rendering should be done from render(Frame).
        val frame = arCoreGraphics.getCurrentFrame() ?: return

        // Frame can be null when initializing or if ARCore is not supported on this device.
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        renderer.render(frame)
        Gdx.gl.glDepthMask(true)
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
        Gdx.gl.glEnable(GL20.GL_CULL_FACE)

        // Move the camera, and then render.
        val vm = FloatArray(16)
        frame.camera.getProjectionMatrix(vm, 0, camera.near, camera.far)
        camera.projection.set(vm)
        frame.camera.getViewMatrix(vm, 0)
        camera.view.set(vm)
        camera.combined.set(camera.projection)
        Matrix4.mul(camera.combined.`val`, camera.view.`val`)

        // Here is the rendering batch.
        modelBatch.begin(camera)
        render(frame, modelBatch)
        modelBatch.end()
    }

    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {}

    /** Must be called before [arCoreGraphics] is used */
    fun setArCoreGraphics(graphics: ArCoreGraphics) {
        arCoreGraphics = graphics
    }

    /** Must be called before [sessionProvider] is used */
    fun setSessionProvider(sessionProvider: ArSessionProvider) {
        this.sessionProvider = sessionProvider
    }

    fun getSession() = sessionProvider.getSession()

    fun requireSession() = getSession() ?: error("Session may not be null")
}
