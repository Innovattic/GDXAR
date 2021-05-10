package com.innovattic.gdxarsample.ui.model.andy

import android.opengl.GLES20
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder

/**
 * Model of Andy the Android. This includes loading 2 OBJ models and combining them into one model.
 * The assets are loaded using the internal file loader, so the path to the assets is relative to
 * src/main/assets in the source project.
 */
class AndyModelLoader(assetManager: AssetManager) {
    private var model: Model? = null

    /**
     * Create a new model. The asset manager is used to begin the asynchronous loading of the model
     * assets. To make sure the assets are loaded, the caller needs to add assetManager.update() to
     * the render() method.
     */
    init {
        assetManager.setLoader(
            Model::class.java, ".obj", ObjLoader(InternalFileHandleResolver())
        )
        val objLoaderParameters = ObjLoader.ObjLoaderParameters()
        objLoaderParameters.flipV = true
        assetManager.load(ANDY_MODEL, Model::class.java, objLoaderParameters)
        assetManager.load(ANDY_SHADOW_MODEL, Model::class.java, objLoaderParameters)
    }

    /**
     * Initializes the model. This needs to be called when the model assets are loaded into memory. If
     * they cannot be found yet, it is assumed that they are still loading.
     */
    fun initialize(assetManager: AssetManager) {
        if (isInitialized()) {
            return
        }
        if (!assetManager.isLoaded(ANDY_MODEL, Model::class.java) ||
            !assetManager.isLoaded(ANDY_SHADOW_MODEL, Model::class.java)
        ) {
            return
        }

        val body = assetManager.get(ANDY_MODEL, Model::class.java)
        val shadow = assetManager.get(ANDY_SHADOW_MODEL, Model::class.java)
        if (body != null && shadow != null) {
            val bodyMaterial = Material(TextureAttribute.createDiffuse(Texture(ANDY_TEXTURE)))
            val shadowMaterial =
                Material(TextureAttribute.createDiffuse(Texture(ANDY_SHADOW_TEXTURE)))
            shadowMaterial.set(
                BlendingAttribute(true, GLES20.GL_ZERO, GLES20.GL_ONE_MINUS_SRC_ALPHA, 1f)
            )
            val builder = ModelBuilder()
            builder.begin()
            for (part in body.meshParts) {
                builder.part(part, bodyMaterial)
            }
            for (part in shadow.meshParts) {
                builder.part(part, shadowMaterial)
            }
            model = builder.end()
        }
    }

    fun createInstance(): ModelInstance {
        return ModelInstance(model)
    }

    fun isInitialized() = model != null

    companion object {
        private const val ANDY_MODEL = "models/andy/andy.obj"
        private const val ANDY_TEXTURE = "models/andy/andy.png"
        private const val ANDY_SHADOW_MODEL = "models/andy/andy_shadow.obj"
        private const val ANDY_SHADOW_TEXTURE = "models/andy/andy_shadow.png"
    }
}
