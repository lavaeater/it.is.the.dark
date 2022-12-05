package dark.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.MathUtils.floor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import dark.core.DarkGame
import de.pottgames.tuningfork.Audio
import eater.core.BasicScreen
import eater.extensions.boundLabel
import eater.injection.InjectionContext
import eater.input.CommandMap
import eater.input.command
import eater.messaging.MessageHandler
import eater.music.*
import ktx.actors.stage
import ktx.scene2d.*
import space.earlygrey.shapedrawer.ShapeDrawer

class MusicVisualizerScreen(game: DarkGame) : BasicScreen(game, CommandMap("MyCommands")) {
    val noteMin = 60 //one octave lower
    val noteMax = 84 //one octave higher
    var currentNote = 0 //72 should equal a pitch of around 1f, but I have no idea

    val pitchSpan = (60 - 72)..(84 - 72)
    override val viewport: Viewport = ExtendViewport(400f, 600f)


    private val musicPlayer by lazy { MusicPlayer(InjectionContext.inject()) }
    private val kickdrum by lazy { loadSampler("80PD_KitB-Kick01", "drumkit-1.json") }
     private val signalMetronome = SignalMetronome(120f, mutableListOf(Instrument(kickdrum)))

    private val sampleRate = 44100

    private val audio by lazy { InjectionContext.inject<Audio>() }

    private fun setUpCommands() {
        commandMap.setUp(Input.Keys.P, "Toggle Music") {
            musicPlayer.toggle()
        }
        commandMap.setUp(Input.Keys.SPACE, "Toggle signalplayer") {
            if(signalMetronome.notPlaying)
                signalMetronome.play()
            else
                signalMetronome.stop()
        }
    }

    override fun show() {


        Scene2DSkin.defaultSkin = Skin(Gdx.files.internal("ui/uiskin.json"))
        super.show()
        if (::stage.isInitialized) {
            stage.clear()
            stage.dispose()
        }
        stage = getStage()
        setUpCommands()
    }

    override fun render(delta: Float) {
        super.render(delta)
        stage.act(delta)
        stage.draw()
        musicPlayer.update(delta)
        signalMetronome.update()
    }


    private lateinit var stage: Stage
    private val shapeDrawer by lazy { InjectionContext.inject<ShapeDrawer>() }
    private fun get16th(timeBars: Float) = floor(timeBars * 16f) % 16

    private fun getStage(): Stage {
        return stage(batch, viewport).apply {
            actors {
                table {
                    table {
                        boundLabel({ get16th(musicPlayer.metronome.timeBars).toString() })
                        boundLabel({ MathUtils.floor(musicPlayer.metronome.timeQuarters).toString() })
                        row()
                        boundLabel({ MathUtils.floor(musicPlayer.metronome.timeBars).toString() })
                        row()
                        (0..15).forEach { i ->
                            table {
                                label(" ") {
                                }.cell(width = 10f, pad = 1f).apply {
                                    background = object: BaseDrawable() {
                                        override fun draw(
                                            batch: Batch,
                                            x: Float,
                                            y: Float,
                                            width: Float,
                                            height: Float
                                        ) {
                                            val c = if(get16th(musicPlayer.metronome.timeBars) == i) Color.BROWN else Color.BLUE
                                            shapeDrawer.filledRectangle(x,y,width,height, c)
                                        }
                                    }
                                }
                                row()
                                for (j in 0 until 4) {
                                    label("$j").cell(grow = true)
                                }
                            }
                        }
                        row()
                    }.align(Align.center or Align.top)
                    row()
                    setFillParent(true)
                }
            }
        }
    }

}