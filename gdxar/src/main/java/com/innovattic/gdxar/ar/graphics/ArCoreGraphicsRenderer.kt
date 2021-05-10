package com.innovattic.gdxar.ar.graphics

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Sets up the OES texture used to render the camera.
 * This is refactored out from the rendering of the background to decouple the background processing,
 * such a rotation, from the actual rendering.
 */
class ArCoreGraphicsRenderer {
    private var quadTexCoord: FloatBuffer? = null
    private var quadTexCoordTransformed: FloatBuffer? = null
    private var mTextureId = -1

    private val mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES

    fun getTextureId(): Int {
        return mTextureId
    }

    fun createOnGlThread() {
        // Generate the background texture.
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mTextureId = textures[0]
        GLES20.glBindTexture(mTextureTarget, mTextureId)
        GLES20.glTexParameteri(mTextureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(mTextureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(mTextureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(mTextureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        val numVertices = 4
        if (numVertices != QUAD_COORDS.size / COORDS_PER_VERTEX) {
            error("Unexpected number of vertices in BackgroundRenderer.")
        }
        val bbVertices = ByteBuffer.allocateDirect(QUAD_COORDS.size * FLOAT_SIZE)
        bbVertices.order(ByteOrder.nativeOrder())
        val quadVertices = bbVertices.asFloatBuffer()
        quadVertices.put(QUAD_COORDS)
        quadVertices.position(0)
        val bbTexCoords = ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE)
        bbTexCoords.order(ByteOrder.nativeOrder())

        val quadTexCoord = bbTexCoords.asFloatBuffer()
        this.quadTexCoord = quadTexCoord
        quadTexCoord.put(QUAD_TEXCOORDS)
        quadTexCoord.position(0)
        val bbTexCoordsTransformed =
            ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE)
        bbTexCoordsTransformed.order(ByteOrder.nativeOrder())
        quadTexCoordTransformed = bbTexCoordsTransformed.asFloatBuffer()
    }

    fun getVertices(frame: Frame?): FloatArray {
        val quadTexCoordTransformed =
            quadTexCoordTransformed ?: error("createOnGlThread must have been run first")

        if (frame != null && frame.hasDisplayGeometryChanged()) {
            frame.transformCoordinates2d(
                Coordinates2d.VIEW_NORMALIZED,
                quadTexCoord,
                Coordinates2d.TEXTURE_NORMALIZED,
                quadTexCoordTransformed
            )
        }
        val ret = FloatArray(QUAD_COORDS.size + QUAD_TEXCOORDS.size)
        for (i in 0..3) {
            ret[i * 5 + 0] = QUAD_COORDS[i * 3]
            ret[i * 5 + 1] = QUAD_COORDS[i * 3 + 1]
            ret[i * 5 + 2] = QUAD_COORDS[i * 3 + 2]
            ret[i * 5 + 3] = quadTexCoordTransformed[i * 2]
            ret[i * 5 + 4] = quadTexCoordTransformed[i * 2 + 1]
        }
        return ret
    }

    companion object {
        private const val COORDS_PER_VERTEX = 3
        private const val TEXCOORDS_PER_VERTEX = 2
        private const val FLOAT_SIZE = 4
        private val QUAD_COORDS: FloatArray = floatArrayOf(
            -1.0f, -1.0f, 0.0f, -1.0f, +1.0f, 0.0f, +1.0f, -1.0f, 0.0f, +1.0f, +1.0f, 0.0f
        )
        private val QUAD_TEXCOORDS: FloatArray = floatArrayOf(
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )
    }
}
