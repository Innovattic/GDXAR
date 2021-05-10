/*
Copyright 2017 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.github.claywilkinson.plane

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.Attribute
import com.badlogic.gdx.graphics.g3d.Attributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader

/**
 * Material for rendering the detected planes. This is an example of a material using a custom
 * shader.
 */
class PlaneMaterial(index: Int) : Material() {
    /**
     * Attributes and uniform values used by the Plane shader. This class is used to register them
     * with the GDX renderer. The values for the attributes are set on the material of the renderable,
     * or in the rendercontext used when rendering. Each attribute also has a setter method that is
     * used to retrieve the attribute value and set it in the shader object at the correct location.
     */
    private class PlaneShaderAttributes(
        type: Long, // This attribute type has only one value, a color.
        val color: Color
    ) : Attribute(type) {
        override fun copy(): Attribute {
            return PlaneShaderAttributes(type, color)
        }

        override fun compareTo(other: Attribute): Int {
            return if (type != other.type) {
                (type - other.type).toInt()
            } else (other as ColorAttribute).color.toIntBits() - color.toIntBits()
        }

        companion object {
            const val DotColorAlias = "u_dotColor"
            private val DotColorType = register(DotColorAlias)
            const val LineColorAlias = "u_lineColor"
            private val LineColorType = register(LineColorAlias)
            const val GridControlAlias = "u_gridControl"
            private val GridControlType = register(GridControlAlias)
            const val PlaneUvMatrixAlias = "u_PlaneUvMatrix"
            private val PlaneUvMatrixType = register(PlaneUvMatrixAlias)
            const val IndexAlias = "u_index;"
            private val IndexType = register(IndexAlias)
            var DotUniformSetter: BaseShader.Setter = object : BaseShader.Setter {
                override fun isGlobal(shader: BaseShader, inputID: Int): Boolean {
                    return false
                }

                override fun set(
                    shader: BaseShader,
                    inputID: Int,
                    renderable: Renderable,
                    combinedAttributes: Attributes
                ) {
                    val vec = FloatArray(4)
                    val c = (combinedAttributes[DotColorType] as PlaneShaderAttributes).color
                    vec[0] = c.r
                    vec[1] = c.g
                    vec[2] = c.b
                    vec[3] = c.a
                    shader.program.setUniform4fv(shader.loc(inputID), vec, 0, 4)
                }
            }
            var LineUniformSetter: BaseShader.Setter = object : BaseShader.Setter {
                override fun isGlobal(shader: BaseShader, inputID: Int): Boolean {
                    return false
                }

                override fun set(
                    shader: BaseShader,
                    inputID: Int,
                    renderable: Renderable,
                    combinedAttributes: Attributes
                ) {
                    val vec = FloatArray(4)
                    val c = (combinedAttributes[LineColorType] as PlaneShaderAttributes).color
                    vec[0] = c.r
                    vec[1] = c.g
                    vec[2] = c.b
                    vec[3] = c.a
                    shader.program.setUniform4fv(shader.loc(inputID), vec, 0, 4)
                }
            }
            var GridControlUniformSetter: BaseShader.Setter = object : BaseShader.Setter {
                override fun isGlobal(shader: BaseShader, inputID: Int): Boolean {
                    return false
                }

                override fun set(
                    shader: BaseShader,
                    inputID: Int,
                    renderable: Renderable,
                    combinedAttributes: Attributes
                ) {
                    val vec = FloatArray(4)
                    val c = (combinedAttributes[GridControlType] as PlaneShaderAttributes).color
                    vec[0] = c.r
                    vec[1] = c.g
                    vec[2] = c.b
                    vec[3] = c.a
                    shader.program.setUniform4fv(shader.loc(inputID), vec, 0, 4)
                }
            }
            var PlaneUvMatrixUniformSetter: BaseShader.Setter = object : BaseShader.Setter {
                override fun isGlobal(shader: BaseShader, inputID: Int): Boolean {
                    return false
                }

                override fun set(
                    shader: BaseShader,
                    inputID: Int,
                    renderable: Renderable,
                    combinedAttributes: Attributes
                ) {
                    val vec = FloatArray(4)
                    val uScale = DOTS_PER_METER
                    val vScale = DOTS_PER_METER * EQUILATERAL_TRIANGLE_SCALE
                    val index =
                        (combinedAttributes[IndexType] as PlaneShaderAttributes).color.r.toInt()
                    val angleRadians = index * 0.144f
                    vec[0] = (+Math.cos(angleRadians.toDouble())).toFloat() * uScale
                    vec[1] = (-Math.sin(angleRadians.toDouble())).toFloat() * uScale
                    vec[2] = (+Math.sin(angleRadians.toDouble())).toFloat() * vScale
                    vec[3] = (+Math.cos(angleRadians.toDouble())).toFloat() * vScale
                    Gdx.gl.glUniformMatrix2fv(shader.loc(inputID), 1, false, vec, 0)
                }
            }

            fun createDotColor(color: Color): PlaneShaderAttributes {
                return PlaneShaderAttributes(DotColorType, color)
            }

            fun createGridControl(color: Color): PlaneShaderAttributes {
                return PlaneShaderAttributes(GridControlType, color)
            }

            fun createLineColor(color: Color): PlaneShaderAttributes {
                return PlaneShaderAttributes(LineColorType, color)
            }

            // Store the index in the red component of the color..
            fun createIndexAttribute(index: Int): PlaneShaderAttributes {
                return PlaneShaderAttributes(IndexType, Color(index.toFloat(), 0f, 0f, 0f))
            }
        }
    }

    companion object {
        // Id prefix used to detect this is a Plane material and it should be rendered using the Plane
        // shader.
        const val MATERIAL_ID_PREFIX = "planeMat"
        private const val VERTEX_SHADER_CODE = ("uniform mat4 u_worldTrans;\n"
            +  // aka u_Model
            "uniform mat4 u_projViewTrans;\n"
            +  // aka  u_ModelViewProjection
            "uniform mat2 u_PlaneUvMatrix;\n"
            + "\n"
            + "attribute vec3 a_position;\n"
            + "\n"
            + "varying vec3 v_TexCoordAlpha;\n"
            + "\n"
            + "void main() {\n"
            + "   vec4 position = vec4(a_position.x, 0.0, a_position.y,  1.0);\n"
            + " vec4 pos = u_worldTrans * position ;\n"
            + "   v_TexCoordAlpha = vec3(u_PlaneUvMatrix * pos.xz, a_position.z);\n"
            + "  gl_Position = u_projViewTrans * pos;\n"
            + "}")
        private const val FRAGMENT_SHADER_CODE = ("precision highp float;\n"
            + "uniform sampler2D u_diffuseTexture;\n"
            + "uniform vec4 u_dotColor;\n"
            + "uniform vec4 u_lineColor;\n"
            + "// dotThreshold, lineThreshold, lineFadeShrink, occlusionShrink\n"
            + "uniform vec4 u_gridControl;\n"
            + "varying vec3 v_TexCoordAlpha;\n"
            + "\n"
            + "void main() {\n"
            + "  vec4 control = texture2D(u_diffuseTexture, v_TexCoordAlpha.xy);\n"
            + "  float dotScale = v_TexCoordAlpha.z;\n"
            + "  float lineFade =\n"
            + "    max(0.0, u_gridControl.z * v_TexCoordAlpha.z - (u_gridControl.z - 1.0));\n"
            + "  vec3 color = (control.r * dotScale > u_gridControl.x) ? u_dotColor.rgb\n"
            + "             : (control.g > u_gridControl.y) ? u_lineColor.rgb * lineFade\n"
            + "                                             : (u_lineColor.rgb * 0.25 * lineFade) ;\n"
            + "  gl_FragColor = vec4(color, v_TexCoordAlpha.z * u_gridControl.w);\n"
            + "}\n")
        private const val DOTS_PER_METER = 10.0f
        private val EQUILATERAL_TRIANGLE_SCALE = (1 / Math.sqrt(3.0)).toFloat()
        private val COLORS = arrayOf(
            Color.BLACK,
            Color.CHARTREUSE,
            Color.CORAL,
            Color.CYAN,
            Color.BLUE,
            Color.FIREBRICK,
            Color.MAROON,
            Color.BROWN,
            Color.GOLDENROD,
            Color.PURPLE
        )
        private var gridTexture: Texture? = null
        fun getShader(renderable: Renderable?): Shader {
            val planeShader: DefaultShader
            val config = DefaultShader.Config(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE)

            // Register the custom uniform attributes.  These are set up by the renderer at the right time.
            planeShader = DefaultShader(renderable, config)
            planeShader.register(
                PlaneShaderAttributes.DotColorAlias, PlaneShaderAttributes.DotUniformSetter
            )
            planeShader.register(
                PlaneShaderAttributes.LineColorAlias, PlaneShaderAttributes.LineUniformSetter
            )
            planeShader.register(
                PlaneShaderAttributes.GridControlAlias,
                PlaneShaderAttributes.GridControlUniformSetter
            )
            planeShader.register(
                PlaneShaderAttributes.PlaneUvMatrixAlias,
                PlaneShaderAttributes.PlaneUvMatrixUniformSetter
            )
            planeShader.register(
                PlaneShaderAttributes.IndexAlias, PlaneShaderAttributes.PlaneUvMatrixUniformSetter
            )
            return planeShader
        }
    }

    init {
        if (gridTexture == null) {
            gridTexture = Texture("textures/trigrid.png")
            gridTexture!!.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        }
        set(TextureAttribute.createDiffuse(gridTexture))
        id = MATERIAL_ID_PREFIX + index
        set(BlendingAttribute(true, GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f))
        // Custom shader uniform values.
        set(PlaneShaderAttributes.createDotColor(COLORS[index % COLORS.size]))
        set(PlaneShaderAttributes.createLineColor(COLORS[(index + 1) % COLORS.size]))
        set(PlaneShaderAttributes.createIndexAttribute(index))

        // Not really a color, but controls how to draw/fade the grid.
        val gridControl = Color(0.2f, 0.4f, 2.0f, 1.5f)
        set(PlaneShaderAttributes.createGridControl(gridControl))
    }
}
