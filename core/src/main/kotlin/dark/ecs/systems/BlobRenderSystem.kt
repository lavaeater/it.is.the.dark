package dark.ecs.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.github.tommyettinger.colorful.Shaders.fragmentShader
import eater.ecs.ashley.components.Box2d
import ktx.assets.toInternalFile
import ktx.graphics.use
import ktx.math.vec2
import space.earlygrey.shapedrawer.ShapeDrawer


class BlobRenderSystem(private val batch: PolygonSpriteBatch, private val shapeDrawer: ShapeDrawer, private val camera: OrthographicCamera) :EntitySystem() {
    private val shaderProgram by lazy {
        val vertexShader = "shaders/vertex.glsl".toInternalFile().readString()
        val fragmentShader = "shaders/fragment.glsl".toInternalFile().readString()
        ShaderProgram(vertexShader, fragmentShader)
    }
    private val fbo by lazy { FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.width, Gdx.graphics.height, true) }
    override fun update(deltaTime: Float) {
        batch.projectionMatrix = camera.combined
        fbo.begin()
        batch.use {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
            for(blobList in BlobGrouper.blobGroups) {
                for ((index, blob) in blobList.withIndex()) {
                    var nextIndex = index + 1
                    if(nextIndex > blobList.lastIndex)
                        nextIndex = 0

                    shapeDrawer.line(Box2d.get(blob).body.position, Box2d.get(blobList[nextIndex]).body.position)
                }
            }
        }
        fbo.end()
        batch.begin()
        batch.shader = shaderProgram
        /**
         * We need all the points
         */
        val v = vec2(shockWavePositionX, shockWavePositionY)
        v.x = v.x / Gdx.graphics.width
        v.y = v.y / Gdx.graphics.height
        shaderProgram.setUniformf("time", deltaTime)
        shaderProgram.setUniformf("center", v)
        val texture = fbo.colorBufferTexture
        val textureRegion = TextureRegion(texture)
        // and.... FLIP!  V (vertical) only
        // and.... FLIP!  V (vertical) only
        textureRegion.flip(false, true)
        batch.draw(textureRegion, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        batch.shader = null
    }
}