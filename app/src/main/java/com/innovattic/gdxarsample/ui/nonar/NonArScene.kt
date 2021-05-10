package com.innovattic.gdxarsample.ui.nonar

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.Vector3
import com.innovattic.gdxarsample.ui.model.andy.AndyModelLoader

/**
 * Simple 3D scene to show in non-AR mode
 */
class NonArScene : ApplicationListener {
    private lateinit var camera: PerspectiveCamera
    private lateinit var modelBatch: ModelBatch
    private lateinit var modelLoader: AndyModelLoader

    private var item: ModelInstance? = null

    private val assetManager = AssetManager()

    override fun create() {
        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.position.set(0f, 1.6f, 0f)
        camera.lookAt(0f, 0f, -1f)
        camera.near = .01f
        camera.far = 30f
        camera.update()
        modelBatch = ModelBatch()

        // Start loading the andy model.
        modelLoader = AndyModelLoader(assetManager)
        Gdx.gl.glClearColor(.25f, .25f, .25f, 1f)
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        Gdx.gl.glDepthMask(true)
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
        Gdx.gl.glEnable(GL20.GL_CULL_FACE)
        if (item == null) {
            handleInput()
        }
        camera.update()
        modelBatch.begin(camera)

        // Let the asset manager work asynchronously.
        assetManager.update()
        modelLoader.initialize(assetManager)
        if (item != null) {
            modelBatch.render(item)
        }
        modelBatch.end()
    }

    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {}

    private fun handleInput() {
        if (Gdx.input.justTouched() && modelLoader.isInitialized()) {
            val x = Gdx.input.x
            val y = Gdx.input.y
            val pos = Vector3(x.toFloat(), y.toFloat(), .9f)

            val item = modelLoader.createInstance()
            this.item = item
            camera.unproject(pos)
            pos.z = .5f
            item.transform.translate(pos)
            item.transform.rotate(0f, 1f, 0f, 180f)
            camera.lookAt(pos)
        }
    }
}
