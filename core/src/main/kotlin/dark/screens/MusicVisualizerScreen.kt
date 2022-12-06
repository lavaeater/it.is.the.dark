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

    private val kickSampler by lazy { loadSampler("80PD_KitB-Kick01", "drumkit-1.json") }
    private val snareSampler by lazy { loadSampler("80PD_KitB-Snare02", "drumkit-1.json") }
    private val hatSampler by lazy { loadSampler("80PD_KitB-OpHat02", "drumkit-1.json") }
    private val bassSampler by lazy { loadSampler("bass-one-shot-808-mini_C_major", "bass-1.json")}
    private val leadSampler by lazy { loadSampler("80s_OberLead_C2", "leads-1.json") }
    private val signalMetronome =
        SignalMetronome(
            120f,
            3f,
            mutableListOf(
                SignalDrummer("kick", kickSampler, generateBeat(-2..2, 1, 4)),
                SignalDrummer("snare", snareSampler, generateBeat(-2..2, 1, 8, 2)),
                SignalDrummer("hat", hatSampler, generateBeat(-2..2, 1, 8,2)),
                SignalBass("bass", bassSampler),
                ChimeyChimeChime("lead", leadSampler, ArpeggioMode.Up)
            ),
            mutableListOf(
                Chord(0f,
                    listOf(
                        Note(-1, 0.75f),
                        Note(0, 0.65f),
                        Note(1, 0.95f),
                        Note(2, 0.25f),
                        )),
                Chord(1f,
                    listOf(
                        Note(2, 0.65f),
                        Note(1, 0.35f),
                        Note(0, 0.25f),
                    )),
                Chord(2f,
                    listOf(
                        Note(2, .5f),
                        Note(4, 0.05f),
                    )),
                Chord(3f,
                    listOf(
                        Note(0, 1f),
                        Note(1, 0.75f),
                        Note(2, 0.5f),
                        Note(4, 0.25f),
                    )))
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
                        table {
                            for (r in 0..signalMetronome.instruments.size) {
                                if (r == 0) {
                                    (0..16).forEach { c ->
                                        if (c == 0)
                                            label("Instrument")
                                        else
                                            container { label(" $c ") }
                                                .apply {
                                                    background = object : BaseDrawable(), IMusicSignalReceiver {
                                                        val index = c - 1
                                                        var on = false
                                                        val color = Color(0f, 0f, 0f, 0f)
                                                        override val receiverName: String
                                                            get() = "Color changing background"

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

                                                        override fun setChord(chord: Chord) {

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
                                                                    clamp(color.r + 0.01f, 0f, 1f),
                                                                    clamp(color.g - 0.01f, 0f, 1f),
                                                                    0f,
                                                                    clamp(color.a - 0.01f, 0f, 1f)
                                                                )
                                                            }
                                                            shapeDrawer.filledRectangle(x, y, width, height, color)
                                                        }
                                                    }.apply { signalMetronome.instruments.add(this) }
                                                }
                                    }
                                } else {
                                    val instrument = signalMetronome.instruments[r - 1]
                                    (0..16).forEach { c ->
                                        if (c == 0)
                                            label(instrument.receiverName)
                                        else
                                            container { label("  ") }
                                                .apply {
                                                    background = object : BaseDrawable(), IMusicSignalReceiver {
                                                        val index = c - 1
                                                        var on = false
                                                        val color = Color(0f, 0f, 0f, 0f)
                                                        override val receiverName: String
                                                            get() = "Color changing background"

                                                        override fun signal(
                                                            beat: Int,
                                                            sixteenth: Int,
                                                            timeBars: Float,
                                                            hitTime: Float,
                                                            intensity: Float
                                                        ) {
                                                            if (index == sixteenth && instrument is Instrument) {
                                                                on = instrument.willPlay(sixteenth, intensity)
                                                            }
                                                        }

                                                        override fun setChord(chord: Chord) {

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
                                                                    clamp(color.r + 0.01f, 0f, 1f),
                                                                    clamp(color.g - 0.01f, 0f, 1f),
                                                                    0f,
                                                                    clamp(color.a - 0.01f, 0f, 1f)
                                                                )
                                                            }
                                                            shapeDrawer.filledRectangle(
                                                                x,
                                                                y,
                                                                width,
                                                                height,
                                                                color
                                                            )
                                                        }
                                                    }.apply { signalMetronome.instruments.add(this) }
                                                }
                                    }
                                }
                                row()
                            }
                        }
                        row()
                    }.align(Align.center)
                    row()
                    setFillParent(true)
                }
            }
        }
    }

}