package dark.screens

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.ExtendViewport
import dark.core.DarkGame
import de.pottgames.tuningfork.SoundSource
import eater.core.BasicScreen
import eater.input.CommandMap
import ktx.scene2d.actors
import ktx.scene2d.label
import java.util.TreeMap


class SoundMachineScreen(game: DarkGame): BasicScreen(game, CommandMap("SoundMachine")) {
    val samples = TreeMap<String, SoundSource>()

    private fun getSamples



    private val stage by lazy {
        val aStage = ktx.actors.stage(batch, ExtendViewport(800f, 600f, OrthographicCamera()))
        aStage.actors {
            label(scoreAvgs.joinToString() + "\n" + scoresInterpolated.joinToString()).apply {
                setFontScale(0.5f)
            }
        }
        aStage
    }


}