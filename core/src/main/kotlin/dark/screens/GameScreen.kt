package dark.screens

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.World
import createDarkMonster
import dark.core.DarkGame
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.math.vec2

class GameScreen(
    private val game: DarkGame,
    private val fleksWorkd: World,
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
        fleksWorkd.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        viewPort.update(width, height)
        batch.projectionMatrix = camera.combined
    }

    override fun resume() {
        super.resume()
    }

    override fun show() {
        createDarkMonster(vec2(),10f,20, 5f)
    }


}