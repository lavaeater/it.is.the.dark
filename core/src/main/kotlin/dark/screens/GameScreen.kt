package dark.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import createBlob
import createFood
import createLight
import dark.core.DarkGame
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.math.vec2

class GameScreen(
    private val game: DarkGame,
    private val engine: Engine,
    private val viewPort: ExtendViewport,
    private val batch: PolygonSpriteBatch,
    private val camera: OrthographicCamera): KtxScreen, KtxInputAdapter {
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
    }

    override fun resume() {
        super.resume()
    }

    override fun show() {
        createFood()
//        createLight()
        createBlob(vec2(), follow = true)
    }


}