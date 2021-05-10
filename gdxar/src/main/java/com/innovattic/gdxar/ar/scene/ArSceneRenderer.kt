package com.innovattic.gdxar.ar.scene

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.google.ar.core.Frame
import com.innovattic.gdxar.ar.graphics.ArCoreGraphics
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Background rendering for ARCore.
 * This renders the camera texture in the background of the scene.
 */
internal class ArSceneRenderer(private val arCoreGraphics: ArCoreGraphics) {
    private val shader = ShaderProgram(Shader.vertex, Shader.fragment)
    private val mesh = Mesh(true, 4, 0, VertexAttribute.Position(), VertexAttribute.TexCoords(0))

    fun render(frame: Frame) {
        if (mesh.numVertices == 0 || frame.hasDisplayGeometryChanged()) {
            mesh.setVertices(arCoreGraphics.getBackgroundVertices(frame))
        }

        // Save the state of the glContext before drawing.
        val gl = Gdx.gl
        val saveFlags = IntArray(3)
        val intbuf = ByteBuffer.allocateDirect(16 * Integer.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
        gl.glGetIntegerv(GL20.GL_DEPTH_TEST, intbuf)
        saveFlags[0] = intbuf[0]
        gl.glGetIntegerv(GL20.GL_DEPTH_WRITEMASK, intbuf)
        saveFlags[1] = intbuf[0]
        gl.glGetIntegerv(GL20.GL_DEPTH_FUNC, intbuf)
        saveFlags[2] = intbuf[0]

        // Disable depth, bind the texture and render it on the mesh.
        gl.glDisable(GLES20.GL_DEPTH_TEST)
        gl.glDepthMask(false)
        GLES20.glBindTexture(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, arCoreGraphics.backgroundTexture
        )
        shader.bind()
        mesh.render(shader, GL20.GL_TRIANGLE_STRIP)

        // Restore the state of the context.
        if (saveFlags[0] == GL20.GL_TRUE) {
            gl.glEnable(GL20.GL_DEPTH_TEST)
        }
        gl.glDepthMask(saveFlags[1] == GL20.GL_TRUE)
        gl.glDepthFunc(saveFlags[2])
    }

    private object Shader {
        // The Shader class in GDX is aware of some common uniform and attribute names.
        // These are used to make setting the values when drawing "automatic".
        // This shader simply draws the OES texture on the provided coordinates.
        // a_position == ShaderProgram.POSITION_ATTRIBUTE
        const val vertex = ("attribute vec4 a_position;\n"
            +  // a_texCoord0 == ShaderProgram.TEXCOORD_ATTRIBUTE + "0"
            "attribute vec2 a_texCoord0;\n"
            + "varying vec2 v_TexCoord;\n"
            + "void main() {\n"
            + "gl_Position = a_position;\n"
            + " v_TexCoord = a_texCoord0;\n"
            + "}")
        const val fragment = ("#extension GL_OES_EGL_image_external : require\n"
            + "\n"
            + "precision mediump float;\n"
            + "varying vec2 v_TexCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "\n"
            + "\n"
            + "void main() {\n"
            + "    gl_FragColor = texture2D(sTexture, v_TexCoord);\n"
            + "}")
    }
}
