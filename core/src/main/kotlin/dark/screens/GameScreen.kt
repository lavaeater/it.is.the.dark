package dark.screens

import com.aliasifkhan.hackLights.HackLightEngine
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import createBlob
import createFood
import createLight
import createMap
import createPlayer
import createSomeHumans
import dark.core.DarkGame
import dark.core.GameSettings
import dark.ecs.systems.BlobGrouper
import eater.injection.InjectionContext.Companion.inject
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.math.vec2

class GameScreen(
    private val game: DarkGame,
    private val engine: Engine,
    private val viewPort: ExtendViewport,
    private val batch: PolygonSpriteBatch,
    private val camera: OrthographicCamera,
private val gameSettings: GameSettings): KtxScreen, KtxInputAdapter {
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
        inject<HackLightEngine>().update(width, height)
    }

    override fun resume() {
        super.resume()
    }

    override fun show() {
        val validPoints = createMap("two")
        BlobGrouper.blobPoints = validPoints
        createFood()
        //createSomeHumans()
        for(i in 0..5)
            createLight()

        for(i in 0..gameSettings.MaxBlobs / 100)
            createBlob(validPoints.random(), (5..16).random() * 10f, follow = i == 0)

        createPlayer()
    }

    override fun keyDown(keycode: Int): Boolean {
        return super.keyDown(keycode)
    }

    override fun keyUp(keycode: Int): Boolean {
        return super.keyUp(keycode)
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return super.mouseMoved(screenX, screenY)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return super.touchDown(screenX, screenY, pointer, button)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return super.touchUp(screenX, screenY, pointer, button)
    }
}