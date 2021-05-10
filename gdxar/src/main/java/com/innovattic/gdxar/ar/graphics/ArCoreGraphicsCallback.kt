package com.innovattic.gdxar.ar.graphics

import com.google.ar.core.Frame

/**
 * Interfaces between [ArCoreGraphics], which receives surface information from
 * GDX [com.badlogic.gdx.backends.android.AndroidGraphics],
 * and allows that information to be sent to the [com.google.ar.core.Session].
 */
interface ArCoreGraphicsCallback {
    fun setDisplayGeometry(rotation: Int, width: Int, height: Int)
    fun setCameraTextureName(textureId: Int)
    fun update(): Frame?
}
