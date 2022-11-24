package dark.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import createBlob
import createLights
import createMap
import createPlayer
import dark.core.DarkGame
import dark.core.GameSettings
import dark.ecs.components.PointType
import dark.ecs.systems.blob.BlobGrouper
import eater.injection.InjectionContext.Companion.context
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen

class GameScreen(
    private val game: DarkGame,
    private val engine: Engine,
    private val viewPort: ExtendViewport,
    private val batch: PolygonSpriteBatch,
    private val camera: OrthographicCamera,
    private val gameSettings: GameSettings
) : KtxScreen, KtxInputAdapter {
    override fun hide() {
        super.hide()
    }

    override fun pause() {
        super.pause()
    }

    override fun render(delta: Float) {
        engine.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        viewPort.update(width, height)
        batch.projectionMatrix = camera.combined
//        inject<HackLightEngine>().update(width, height)
    }

    override fun resume() {
        super.resume()
    }

    override fun show() {
        val map = createMap("two")
        context.bindSingleton(map)
        BlobGrouper.blobPoints = map.points[PointType.BlobStart]!!
//        createFood(map.points[PointType.BlobStart]!!)
//        createHumans(map.points[PointType.HumanStart]!!)
        createLights(map.points[PointType.Lights]!!)

        for (i in 0..gameSettings.MinBlobs)
            createBlob(map.points[PointType.BlobStart]!!.random(), (5..6).random() * 10f, follow = false)

        createPlayer(map.points[PointType.PlayerStart]!!.random(), follow = true)
    }
}