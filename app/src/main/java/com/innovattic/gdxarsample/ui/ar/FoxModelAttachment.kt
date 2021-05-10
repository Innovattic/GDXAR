package com.innovattic.gdxarsample.ui.ar

import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.github.claywilkinson.plane.PlaneAttachment
import com.innovattic.gdxarsample.ui.model.fox.FoxModelInstance

class FoxModelAttachment(plane: Plane, anchor: Anchor, data: FoxModelInstance) :
    PlaneAttachment<FoxModelInstance>(plane, anchor, data) {

    // Not visible by default - this prevents flickering until model is correctly positioned
    var isVisible: Boolean = false
}
