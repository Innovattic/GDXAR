package com.innovattic.gdxarsample.ui.ar

import android.util.Log
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.NotTrackingException
import com.innovattic.gdxar.ar.scene.ArScene
import com.innovattic.gdxarsample.extension.TAG
import com.github.claywilkinson.plane.PlaneMaterial
import com.github.claywilkinson.plane.PlaneModel
import com.innovattic.gdxarsample.ui.model.fox.FoxModelLoader
import java.util.HashMap

/**
 * This scene has 2 major components:
 * 1. Detects planes and renders them using a custom shader and material.
 * 2. When a plane is tapped, an Android model is drawn.
 * Demonstrating loading multiple models and then combining them into a single model for ease of
 * use.
 */
class SampleArScene(
    private val loadingChangeListener: (isLoading: Boolean) -> Unit
) : ArScene() {
    private val assetManager = AssetManager()
    private lateinit var modelLoader: FoxModelLoader

    private var isLoading: Boolean = false
        set(value) {
            field = value
            loadingChangeListener(value)
        }

    // Keep the objects in the scene mapped by the anchor id.
    private val instances = HashMap<Anchor, FoxModelAttachment>()

    override fun create() {
        super.create()
        modelLoader = FoxModelLoader(assetManager)
    }

    /** Create a new shader provider that is aware of the Plane material custom shader.  */
    override fun createShaderProvider(): ShaderProvider {
        val config = DefaultShader.Config().apply {
            numBones = 50
        }
        return object : DefaultShaderProvider(config) {
            override fun createShader(renderable: Renderable): Shader {
                return if (renderable.material.id.startsWith(PlaneMaterial.MATERIAL_ID_PREFIX)) {
                    PlaneMaterial.getShader(renderable)
                } else {
                    super.createShader(renderable)
                }
            }
        }
    }

    /**
     * This is the main render method. It is called on each frame. This is where all scene operations
     * need to be. This includes interacting with the ARCore frame for hit tests, plane detection
     * updates and anchor updates.
     *
     * It also is where application specific objects are created and ultimately rendered.
     */
    override fun render(frame: Frame, modelBatch: ModelBatch) {
        // Let the asset manager work asynchronously.
        assetManager.update()
        modelLoader.initialize(assetManager)

        // If we're still loading/detecting planes, just return.
        if (!checkIsLoaded(frame)) {
            return
        }

        // Draw all the planes detected
        drawPlanes(modelBatch)

        // Handle taps to create androids.
        handleInput(frame)
        for (anchor in frame.updatedAnchors) {
            val item = instances[anchor]
            if (item != null) {
                val model = item.data
                val pose = item.pose
                val pos = Vector3(pose.tx(), pose.ty(), pose.tz())
                val rot = Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw())
                model.modelInstance.transform[pos] = rot
                item.isVisible = true
            }
        }

        // Finally, render all the object instances.
        val attachments = instances.values.filter { it.isVisible }
        val foxModelInstances = attachments.map { it.data }
        foxModelInstances.forEach {
            it.update(Gdx.graphics.deltaTime)
        }
        val models = foxModelInstances.map { it.modelInstance }
        modelBatch.render(models)
    }

    /**
     * Handles the touch input. This gets the screen X,Y position of the touch and then performs a
     * Hittest vs. the planes detected. If the hit is within a plane, an instance of the Andy model is
     * created.
     */
    private fun handleInput(frame: Frame) {
        if (Gdx.input.justTouched() && modelLoader.isInitialized()) {
            val x = Gdx.input.x
            val y = Gdx.input.y
            for (hit in frame.hitTest(x.toFloat(), y.toFloat())) {
                // Check if any plane was hit, and if it was hit inside the plane polygon.
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    // Cap the number of objects created. This avoids overloading both the
                    // rendering system and ARCore.
                    if (instances.size >= 16) {
                        val anchor = instances.keys.first()
                        instances.remove(anchor)
                        anchor.detach()
                    }
                    // Adding an Anchor tells ARCore that it should track this position in
                    // space. This anchor will be used in PlaneAttachment to place the 3d model
                    // in the correct position relative both to the world and to the plane.
                    try {
                        val foxModelInstance = modelLoader.createInstance()
                        val modelAttachment = FoxModelAttachment(
                            trackable,
                            requireSession().createAnchor(hit.hitPose),
                            foxModelInstance
                        )
                        val modelInstance = foxModelInstance.modelInstance
                        val p = modelAttachment.pose
                        // position and rotate
                        val dir = Quaternion(p.qx(), p.qy(), p.qz(), p.qw())
                        val pos = Vector3(p.tx(), p.ty(), p.tz())
                        modelInstance.transform.translate(pos)
                        modelInstance.transform.set(dir)

                        instances[modelAttachment.anchor] = modelAttachment
                    } catch (e: NotTrackingException) {
                        Log.w(TAG, "not tracking", e)
                    }

                    // Hits are sorted by depth. Consider only closest hit on a plane.
                    break
                }
            }
        }
    }

    /** Draws the planes detected.  */
    private fun drawPlanes(modelBatch: ModelBatch) {
        val planeInstances = Array<ModelInstance>()
        var index = 0
        for (plane in requireSession().getAllTrackables(Plane::class.java)) {

            // check for planes that are no longer valid
            if (plane.subsumedBy != null || plane.trackingState == TrackingState.STOPPED || plane.polygon.capacity() == 0) {
                continue
            }
            // New plane
            val planeModel = PlaneModel.createPlane(plane, index++) ?: continue
            val instance = ModelInstance(planeModel)
            instance.transform.setToTranslation(
                plane.centerPose.tx(), plane.centerPose.ty(), plane.centerPose.tz()
            )
            planeInstances.add(instance)
        }
        modelBatch.render(planeInstances)
    }

    /**
     * Handles showing the loading message, then hiding it once a plane is detected.
     *
     * @param frame - the ARCore frame.
     * @return true once a plane is loaded.
     */
    private fun checkIsLoaded(frame: Frame): Boolean {
        // If not tracking, don't draw 3d objects.
        if (frame.camera.trackingState != TrackingState.TRACKING) {
            isLoading = true
            return false
        }
        // Check if we detected at least one plane. If so, hide the loading message.
        if (isLoading) {
            for (plane in requireSession().getAllTrackables(Plane::class.java)) {
                if (plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                    && plane.trackingState == TrackingState.TRACKING
                ) {
                    isLoading = false
                }
            }
        }
        return true
    }
}
