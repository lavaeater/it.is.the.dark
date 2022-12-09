package dark.ecs.systems

import box2dLight.RayHandler
import com.aliasifkhan.hackLights.HackLightEngine
import dark.ecs.components.Food
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import dark.core.GameSettings
import eater.ecs.ashley.components.Map
import dark.ecs.components.blobcomponents.Blob
import dark.ecs.components.blobcomponents.Target
import dark.injection.Assets
import eater.ecs.ashley.components.*
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.ashley.exclude
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
    private val foodFamily = allOf(Food::class, TransformComponent::class).get()
    private val allBlobs = allOf(Blob::class, TransformComponent::class).exclude(Remove::class).get()
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
    private val shockParams = vec3(1.0f, 0.2f, 0.1f)
    private var shaderTime = 2f

    private val humanFamily = allOf(Human::class, TransformComponent::class).exclude(Remove::class).get()
    private val allHumans get() = engine.getEntitiesFor(humanFamily)
    private val assets by lazy { inject<Assets>() }

    private val lightsFamily = allOf(LightComponent::class, TransformComponent::class).exclude(Remove::class).get()
    private val lights get() = engine.getEntitiesFor(lightsFamily)
    private val lightColor = Color(1f, 1f, 0f, 0.5f)
    private val lightsEngine by lazy { inject<HackLightEngine>() }
    private val rayHandler by lazy { inject<RayHandler>() }


    override fun update(deltaTime: Float) {

        batch.projectionMatrix = camera.combined
        //fbo.begin()
        batch.use {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
            Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT)
            //Render the map. i.e. draw its region
            renderMap()
            renderBlobs()
            renderFood()
            renderHumans()
        }

        //lightsEngine.draw(camera.combined)

        renderShader(deltaTime)
        batch.use {
            renderTopLayerMap()
        }
        rayHandler.setCombinedMatrix(camera)
        rayHandler.updateAndRender()
    }

    private fun renderHumans() {
        val t = assets.buddy.values.first().keyFrames.first()
        for (human in allHumans) {
            val position = TransformComponent.get(human).position
            batch.draw(t, position.x - t.regionWidth / 2f, position.y - t.regionHeight / 2f)
//            if (BodyControl.has(human)) {
//                val bc = BodyControl.get(human)
//                shapeDrawer.filledCircle(position + (bc.aimDirection * 10f), 2f, Color.RED)
//            }
        }
    }

    private fun renderBlobs() {
        for (lonelyBlob in engine.getEntitiesFor(allBlobs)) {
            val health = PropsAndStuff.get(lonelyBlob).getHealth()
            val radius = Blob.get(lonelyBlob).radius
            val blobPosition = TransformComponent.get(lonelyBlob).position
            shapeDrawer.filledCircle(
                blobPosition,
                radius,
                Color(0f, health.normalizedValue, 0.5f, 1f)
            )
            val ropes = Blob.get(lonelyBlob).ropes
            Blob.get(lonelyBlob).ropes.removeAll(ropes.filter { it.destroyed })
            for(rope in ropes) {
                val nodePositions = rope.nodes.values.map { TransformComponent.get(it).position }
                for((index, position) in nodePositions.withIndex()) {
                    if(index == 0) {
                        shapeDrawer.line(rope.from!!.position, position)
                    }else if(index < nodePositions.lastIndex) {
                        shapeDrawer.line(position, nodePositions[index + 1])
                    } else {
                        shapeDrawer.line(position, rope.to!!.position)
                    }
                }
            }

            if (gameSettings.Debug && LogComponent.has(lonelyBlob)) {
                shapeDrawer.setColor(Color.GREEN)
                shapeDrawer.circle(blobPosition.x, blobPosition.y, gameSettings.BlobDetectionRadius)
                shapeDrawer.setColor(Color.RED)
                shapeDrawer.circle(blobPosition.x, blobPosition.y, PropsAndStuff.get(lonelyBlob).getDetectionRadius().current)
                shapeDrawer.setColor(Color.BLUE)
                shapeDrawer.circle(blobPosition.x, blobPosition.y, 2.5f)

                if(Target.MoveTowardsFoodTarget.has(lonelyBlob)) {
                    val t = Target.MoveTowardsFoodTarget.get(lonelyBlob).target
                    if(t != null) {
                        val position = TransformComponent.get(t).position
                        shapeDrawer.filledCircle(position, 2f, Color.RED)
                    }
                }
            }
        }
    }

    private fun renderMap() {
        val map = Map.get(mapEntity)
        batch.draw(
            map.mapTextureRegion,
            map.mapOrigin.x,
            map.mapOrigin.y
        )
    }

    private fun renderTopLayerMap() {
        val map = Map.get(mapEntity)
        batch.draw(
            map.mapTopLayerRegion,
            map.mapOrigin.x,
            map.mapOrigin.y
        )

    }

    private fun renderFood() {
        val foodRenderStuff = engine.getEntitiesFor(foodFamily)
            .associate { TransformComponent.get(it).position to MathUtils.norm(100f, 200f, Food.get(it).foodEnergy) }
        for ((position, normalizedEnergy) in foodRenderStuff) {
            shapeDrawer.filledCircle(
                position,
                2.5f * normalizedEnergy,
                Color(1f - normalizedEnergy, normalizedEnergy, 1f - normalizedEnergy, 0.1f)
            )
        }
    }

    private fun renderShader(deltaTime: Float) {
        shaderTime - deltaTime
        if (shaderTime < 0f)
            shaderTime = 2f
        //        fbo.end()
//        batch.use {
//            batch.shader = shaderProgram
//            /**
//             * We need all the points
//             */
//
//            camera.project(blobCenter)
//
//            shaderCenter.set(blobCenter.x / Gdx.graphics.width, blobCenter.y / Gdx.graphics.height)
//            shaderProgram.setUniformf("time", deltaTime)
//            shaderProgram.setUniformf("center", shaderCenter)
//            shaderProgram.setUniformf("shockParams", shockParams)
//            val texture = fbo.colorBufferTexture
//            val textureRegion = TextureRegion(texture)
//            // and.... FLIP!  V (vertical) only
//            // and.... FLIP!  V (vertical) only
//            textureRegion.flip(false, true)
//            batch.draw(
//                textureRegion,
//                camera.position.x - camera.viewportWidth / 2f,
//                camera.position.y - camera.viewportHeight / 2f,
//                camera.viewportWidth,
//                camera.viewportHeight
//            )
//            batch.shader = null
//        }
    }
}