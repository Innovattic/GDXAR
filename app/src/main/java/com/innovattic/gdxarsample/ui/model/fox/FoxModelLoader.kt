package com.innovattic.gdxarsample.ui.model.fox

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import net.mgsx.gltf.loaders.glb.GLBAssetLoader
import net.mgsx.gltf.scene3d.scene.SceneAsset

class FoxModelLoader(assetManager: AssetManager) {
    private var model: Model? = null

    /**
     * Create a new model. The asset manager is used to begin the asynchronous loading of the model
     * assets. To make sure the assets are loaded, the caller needs to add assetManager.update() to
     * the render() method.
     */
    init {
        assetManager.setLoader(
            SceneAsset::class.java, ".glb", GLBAssetLoader()
        )

        assetManager.load(MODEL, SceneAsset::class.java)
    }

    fun initialize(assetManager: AssetManager) {
        if (isInitialized()) {
            return
        }
        if (!assetManager.isLoaded(MODEL, SceneAsset::class.java)) {
            return
        }

        val sceneAsset: SceneAsset = assetManager.get(MODEL)

        val model = sceneAsset.scene.model
        this.model = model
    }

    fun createInstance(): FoxModelInstance {
        val model = model ?: error("initialize must succeed first")
        val modelInstance = ModelInstance(model)
        val foxInstance = FoxModelInstance(modelInstance)
        foxInstance.setSurveyAnimation()
        return foxInstance
    }

    fun isInitialized() = model != null

    companion object {
        private const val MODEL = "models/fox/fox.glb"
    }
}
