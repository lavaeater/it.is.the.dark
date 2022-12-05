package dark.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils.clamp
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
import eater.extensions.boundProgressBar
import eater.injection.InjectionContext
import eater.input.CommandMap
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

    private val kick by lazy { loadSampler("80PD_KitB-Kick01", "drumkit-1.json") }
    private val snare by lazy { loadSampler("80PD_KitB-Snare02", "drumkit-1.json") }
    private val hat by lazy { loadSampler("80PD_KitB-OpHat02", "drumkit-1.json") }
    private val signalMetronome =
        SignalMetronome(
            120f, mutableListOf(
                Instrument("kick", kick, generateBeat(-2..0, 4, 4)),
                Instrument("snare", snare, generateBeat(-4..0, 4, 16)),
                Instrument("hat",hat, generateBeat(0..4, 1, 6)),
            )
        )

    private val sampleRate = 44100

    private val audio by lazy { InjectionContext.inject<Audio>() }
    private val timePiece by lazy { GdxAI.getTimepiece() }

    private fun setUpCommands() {
        commandMap.setUp(Input.Keys.SPACE, "Toggle signalplayer") {
            if (signalMetronome.notPlaying)
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
        timePiece.update(delta)
        stage.act(delta)
        stage.draw()
        signalMetronome.update()
        playSounds()
    }

    private fun playSounds() {
        val soundsToPlayRightNowIGuess = ToPlay.soundsToPlay.filter { it.targetTime < timePiece.time }
        ToPlay.soundsToPlay.removeAll(soundsToPlayRightNowIGuess)
        for (sound in soundsToPlayRightNowIGuess) {
            audio.play(sound.soundBuffer, 1f, sound.pitch)
        }
    }


    private lateinit var stage: Stage
    private val shapeDrawer by lazy { InjectionContext.inject<ShapeDrawer>() }
    private fun get16th(timeBars: Float) = floor(timeBars * 16f) % 16

    private fun getStage(): Stage {
        return stage(batch, viewport).apply {
            actors {
                table {
                    table {
                        boundProgressBar({ signalMetronome.intensity }).cell(pad = 5f)
                        row()
                        boundLabel({ "Bar: ${signalMetronome.thisBar}: ${signalMetronome.lastBar}" })
                        row()
                        boundLabel({ "TimeSeconds: ${signalMetronome.timeSeconds.toInt()}: ${signalMetronome.timeQuarters.toInt()}" })
                        row()
                        boundLabel({ "TimeBars: ${signalMetronome.timeBars.toInt()}: ${signalMetronome.lastTimeBars.toInt()}" })
                        row()
                        table {
                            (0..16).forEach { i ->
                                if (i == 0) {
                                    table {
                                        for(instrument in signalMetronome.receivers) {
                                            label(instrument.toString())
                                        }
                                    }
                                } else {
                                    container { label(" $i ") }
                                        .apply {
                                            background = object : BaseDrawable(), IMusicSignalReceiver {
                                                val index = i
                                                var on = false
                                                val color = Color(0f, 0f, 0f, 0f)

                                                override fun signal(
                                                    beat: Int,
                                                    sixteenth: Int,
                                                    timeBars: Float,
                                                    hitTime: Float,
                                                    intensity: Float
                                                ) {
                                                    if (sixteenth == index) {
                                                        on = true
                                                    }
                                                }

                                                override fun draw(
                                                    batch: Batch,
                                                    x: Float,
                                                    y: Float,
                                                    width: Float,
                                                    height: Float
                                                ) {
                                                    if (on) {
                                                        color.set(Color.GREEN)
                                                        on = false
                                                    } else {
                                                        color.set(
                                                            clamp(color.r + 0.2f, 0f, 1f),
                                                            clamp(color.g - 0.2f, 0f, 1f),
                                                            0f,
                                                            clamp(color.a - 0.3f, 0f, 1f)
                                                        )
                                                    }
                                                    shapeDrawer.filledRectangle(x, y, width, height, color)
                                                }
                                            }.apply { signalMetronome.receivers.add(this) }
                                        }
                                }
                            }
                        }
                        row()
                        boundLabel({ "This 16th: ${signalMetronome.this16th}" }).cell(pad = 1f)
                        row()
                        boundLabel({ "Playing: ${signalMetronome.playing}" })
//                        boundLabel({ get16th(musicPlayer.metronome.timeBars).toString() })
//                        boundLabel({ MathUtils.floor(musicPlayer.metronome.timeQuarters).toString() })
//                        row()
//                        boundLabel({ MathUtils.floor(musicPlayer.metronome.timeBars).toString() })
//                        row()
//                        (0..15).forEach { i ->
//                            table {
//                                label(" ") {
//                                }.cell(width = 10f, pad = 1f).apply {
//                                    background = object: BaseDrawable() {
//                                        override fun draw(
//                                            batch: Batch,
//                                            x: Float,
//                                            y: Float,
//                                            width: Float,
//                                            height: Float
//                                        ) {
//                                            val c = if(get16th(musicPlayer.metronome.timeBars) == i) Color.BROWN else Color.BLUE
//                                            shapeDrawer.filledRectangle(x,y,width,height, c)
//                                        }
//                                    }
//                                }
//                                row()
//                                for (j in 0 until 4) {
//                                    label("$j").cell(grow = true)
//                                }
//                            }
//                        }
                        row()
                    }.align(Align.center)
                    row()
                    setFillParent(true)
                }
            }
        }
    }

}