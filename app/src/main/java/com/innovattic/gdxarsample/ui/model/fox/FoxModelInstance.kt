package com.innovattic.gdxarsample.ui.model.fox

import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.AnimationController

class FoxModelInstance(val modelInstance: ModelInstance) {
    private val animationController = AnimationController(modelInstance)

    fun setSurveyAnimation() {
        animationController.setAnimation(Animation.SURVEY, -1)
    }

    fun update(delta: Float) {
        animationController.update(delta)
    }

    private object Animation {
        const val SURVEY = "Survey"
    }
}
