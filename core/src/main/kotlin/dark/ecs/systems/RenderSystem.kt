package dark.ecs.systems

import Food
import Map
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import dark.core.GameSettings
import dark.ecs.components.Blob
import dark.ecs.components.PropsAndStuff
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.CameraFollow
import ktx.ashley.allOf
import ktx.assets.toInternalFile
import ktx.graphics.use
import ktx.math.vec2
import ktx.math.vec3
import space.earlygrey.shapedrawer.ShapeDrawer


class RenderSystem(
    private val batch: PolygonSpriteBatch,
    private val shapeDrawer: ShapeDrawer,
    private val camera: OrthographicCamera,
    private val gameSettings: GameSettings
) : EntitySystem() {
    private val shaderProgram by lazy {
        val vertexShader = "shaders/vertex.glsl".toInternalFile().readString()
        val fragmentShader = "shaders/fragment.glsl".toInternalFile().readString()
        ShaderProgram.pedantic = false
        ShaderProgram(vertexShader, fragmentShader)
    }
    private val mapFamily = allOf(Map::class).get()
    private val mapEntity get() = engine.getEntitiesFor(mapFamily).first() //Should always be one
    private val foodFamily = allOf(Food::class, Box2d::class).get()
    private val fbo by lazy {
        FrameBuffer(
            Pixmap.Format.RGBA8888,
            Gdx.graphics.width * 2,
            Gdx.graphics.height * 2,
            true
        )
    }
    private val blobCenter = vec3()
    private val shaderCenter = vec2()
    private val shockParams = vec3(10.0f, 0.2f, 0.1f)
    private var shaderTime = 2f
    override fun update(deltaTime: Float) {
        shaderTime - deltaTime
        if (shaderTime < 0f)
            shaderTime = 2f

        batch.projectionMatrix = camera.combined
        fbo.begin()
        batch.use {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
            Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT)
            //Render the map. i.e. draw its region
            val map = Map.get(mapEntity)
            batch.draw(
                map.mapTextureRegion,
                map.mapOrigin.x,
                map.mapOrigin.y
            )
            shapeDrawer.filledCircle(map.mapOrigin, 15f, Color.YELLOW)

            for (blobList in BlobGrouper.blobGroups) {
                for ((index, blobEntity) in blobList.withIndex()) {
                    var nextIndex = index + 1
                    if (nextIndex > blobList.lastIndex)
                        nextIndex = 0
                    val blobBody = Box2d.get(blobEntity).body
                    val blobPosition = blobBody.position
                    val blob = Blob.get(blobEntity)
                    val health = PropsAndStuff.get(blobEntity).getHealth()
                    shapeDrawer.filledCircle(
                        blobPosition,
                        1f,
                        Color(
                            1f - health.normalizedValue,
                            health.normalizedValue,
                            1f - health.normalizedValue,
                            health.normalizedValue
                        )
                    )
                    shapeDrawer.setColor(Color.GREEN)

                    if (CameraFollow.has(blobEntity))
                        blobCenter.set(blobPosition, 0f)
                    shapeDrawer.line(blobPosition, Box2d.get(blobList[nextIndex]).body.position)
                    shapeDrawer.setColor(Color.WHITE)
                }
            }
            val foodRenderStuff = engine.getEntitiesFor(foodFamily)
                .associate { Box2d.get(it).body.position to MathUtils.norm(0f, 100f, Food.get(it).foodEnergy) }
            for ((position, normalizedEnergy) in foodRenderStuff) {
                shapeDrawer.filledCircle(
                    position,
                    2.5f * normalizedEnergy,
                    Color(1f - normalizedEnergy, normalizedEnergy, 1f - normalizedEnergy, normalizedEnergy)
                )
            }
        }
        fbo.end()
        batch.use {
            batch.shader = shaderProgram
            /**
             * We need all the points
             */

            camera.project(blobCenter)

            shaderCenter.set(blobCenter.x / Gdx.graphics.width, blobCenter.y / Gdx.graphics.height)
            shaderProgram.setUniformf("time", deltaTime)
            shaderProgram.setUniformf("center", shaderCenter)
            shaderProgram.setUniformf("shockParams", shockParams)
            val texture = fbo.colorBufferTexture
            val textureRegion = TextureRegion(texture)
            // and.... FLIP!  V (vertical) only
            // and.... FLIP!  V (vertical) only
            textureRegion.flip(false, true)
            batch.draw(
                textureRegion,
                camera.position.x - camera.viewportWidth / 2f,
                camera.position.y - camera.viewportHeight / 2f,
                camera.viewportWidth,
                camera.viewportHeight
            )
            batch.shader = null
        }
    }
}