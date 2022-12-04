package dark.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils.*
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import dark.core.DarkGame
import de.pottgames.tuningfork.*
import eater.core.BasicScreen
import eater.extensions.boundLabel
import eater.injection.InjectionContext.Companion.inject
import eater.input.CommandMap
import eater.music.ListItem
import eater.music.MusicPlayer
import eater.music.toPitch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.actors.stage
import ktx.collections.lastIndex
import ktx.collections.toGdxArray
import ktx.scene2d.*
import space.earlygrey.shapedrawer.ShapeDrawer
import java.lang.Exception


class SoundMachineScreen(game: DarkGame) : BasicScreen(game, CommandMap("MyCommands")) {
    private fun getSamples(): ListItem.Directory {
        val root = ListItem.Directory("music-samples", "music-samples", null)
        getDirInfo(root)
        return root
    }

    val noteMin = 60 //one octave lower
    val noteMax = 84 //one octave higher
    var currentNote = 0 //72 should equal a pitch of around 1f, but I have no idea

    val pitchSpan = (60-72)..(84-72)

    /*
    I have an idea.

    The min pitch is 0.5, the max 2.0. This equals... it's so low, like the root from 2
     */

    override val viewport: Viewport = ExtendViewport(400f, 600f)

    private val sampleStore by lazy { getSamples() }
    private var currentDirectory = sampleStore

    private val musicPlayer by lazy { MusicPlayer(inject()) }

    private val sampleRate = 44100
    private val createdSound = PcmSoundSource(sampleRate, PcmFormat.MONO_8_BIT)

    private val soundLengthSeconds = 5f
    private val soundSamples = floor(sampleRate * soundLengthSeconds) //Number of values / samples

    private var randomToneVal = (0..255).random()
    private var frequency = 20f //per second

    private val aRandomSound = Array(soundSamples) {
//        if(it % 10 == 0)
//            randomToneVal = (0..255).random()
//        randomToneVal.toByte()

        //This is fucking retarded
        /*
        if frequency is 20 and sound length is 1 second, we are going to need
        length * freq fluctuations
         */
        val fluxes = frequency * soundLengthSeconds //20 in this base case, lets do 40 instead
        /*
        This means our array should be divided into fluxes number of iterations, right?
        Just divide it to begin with?
         */
        val normalized = norm(0f, soundSamples.toFloat() / fluxes, it.toFloat() / fluxes)
        (norm(-1f, 1f, sin(normalized * PI2)) * 255f).toInt().toByte()
    }

    fun playAGeneratedSound() {
        createdSound.queueSamples(aRandomSound.toByteArray(), 0, soundSamples)
        createdSound.play()
    }

    private fun getDirInfo(directory: ListItem.Directory) {
        val dirContents = Gdx.files.external(directory.path).list()
        val dirs = dirContents.filter { it.isDirectory }
        for (dir in dirs) {
            val d = ListItem.Directory(dir.name(), dir.path(), directory)
            directory.childDirs.add(d)
            getDirInfo(d)
        }
        val files = dirContents.filter { !it.isDirectory && it.extension() == "wav" }
        for (file in files) {
            directory.files.add(ListItem.SoundFile(file.nameWithoutExtension(), file.path()))
        }
    }


    var currentListIsDirList = true
    val currentListIsFileList get() = !currentListIsDirList

    private fun switchList() {
        currentList = if (currentListIsDirList) fileList else dirList
        currentListIsDirList = !currentListIsDirList
    }

    private fun goUpInCurrentList() {
        if (currentList.selectedIndex == 0)
            currentList.selectedIndex = 0
        else if (currentList.selectedIndex >= 1)
            currentList.selectedIndex--
    }

    private fun goDownInCurrentList() {
        if (currentList.selectedIndex == currentList.items.lastIndex)
            currentList.selectedIndex = currentList.items.lastIndex
        else if (currentList.selectedIndex < currentList.items.lastIndex)
            currentList.selectedIndex++
    }

    private val sounds = mutableMapOf<ListItem.SoundFile, SoundBuffer>()

    private val audio by lazy { inject<Audio>() }

    private fun selectCurrentItemInList() {
        when (currentList.selected) {
            is ListItem.Directory -> {
                currentDirectory = currentList.selected as ListItem.Directory
                updateFilesAndFoldersLists()
            }

            is ListItem.SoundFile -> {
                /*
                Play this fudging sound, mate!
                 */
                tryToPlay(currentList.selected as ListItem.SoundFile)

            }
        }
    }

    private fun tryToPlay(soundFile: ListItem.SoundFile) {
        var soundWorks = true

        if (!sounds.containsKey(soundFile)) {
            try {
                sounds[soundFile] = WaveLoader.load(Gdx.files.external(currentList.selected.path))
            } catch (e: Exception) {
                soundWorks = false
            }
        }

        if (soundWorks) {
            val sound = sounds[soundFile]!!
            audio.play(sound, 1f, currentNote.toPitch())
        } else {
            currentList.items.removeValue(currentList.selected, true)
        }
    }

    private val selectedSamples = mutableSetOf<ListItem.SoundFile>()

    private fun setUpCommands() {
        commandMap.setUp(Keys.TAB, "Switch lists") {
            switchList()
        }
        commandMap.setUp(Keys.LEFT, "Go Down a Note") {
            currentNote--
            if (currentNote < noteMin) {
                currentNote = noteMin
            }
        }
        commandMap.setUp(Keys.RIGHT, "Go Up a Note") {
            currentNote++
            if (currentNote > noteMax) {
                currentNote = noteMax
            }
        }

        commandMap.setUp(Keys.UP, "Go up in dirlist") {
            goUpInCurrentList()
        }
        commandMap.setUp(Keys.DOWN, "Go up in dirlist") {
            goDownInCurrentList()
        }
        commandMap.setUp(Keys.ENTER, "Select ITem in dirlist") {
            selectCurrentItemInList()
        }
        commandMap.setUp(Keys.BACKSPACE, "Go back one directory or switch back to previous list") {
            if (currentListIsFileList) {
                switchList()
            }
            if (currentDirectory.parent != null) {
                currentDirectory = currentDirectory.parent!!
                updateFilesAndFoldersLists()
            }
        }
        commandMap.setUp(Keys.SPACE, "Select this sample") {
            if (currentListIsFileList) {
                selectedSamples.add(currentList.selected as ListItem.SoundFile)
                currentSamples.setItems(selectedSamples.toGdxArray())
                currentSamples.refreshItems()
            }
        }

        commandMap.setUp(Keys.S, "Save Instrument") {
            if (selectedSamples.any()) {
                saveInstrument()
            }
        }

        commandMap.setUp(Keys.O, "Play Random Sound") {
            playAGeneratedSound()
        }
        commandMap.setUp(Keys.P, "Toggle Music") {
            musicPlayer.toggle()
        }
    }

    var instrumentCount = 0
    private fun saveInstrument() {
        val json = Json.encodeToString(selectedSamples)

        instrumentCount++
        Gdx.files.local("instrument-$instrumentCount.json").writeString(json, true)
        selectedSamples.clear()
    }

    fun updateFilesAndFoldersLists() {
        dirList.setItems(currentDirectory.childDirs.toGdxArray())
        fileList.setItems(currentDirectory.files.toGdxArray())
        if (currentDirectory.childDirs.isEmpty() && currentListIsDirList)
            switchList()
        dirList.refreshItems()
        fileList.refreshItems()
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
    }


    private lateinit var stage: Stage
    private lateinit var dirList: KListWidget<ListItem>
    private lateinit var fileList: KListWidget<ListItem>
    private lateinit var currentList: KListWidget<ListItem>
    private lateinit var currentSamples: KListWidget<ListItem>
    private val shapeDrawer by lazy {inject<ShapeDrawer>()}
    fun get16th(timeBars:Float) = floor(timeBars * 16f) % 16

    private fun getStage(): Stage {
        return stage(batch, viewport).apply {
            actors {
                table {
                    table {
                        boundLabel({musicPlayer.metronome.timeBars.toString()})
                        row()
                        for(i in 0..15) {
                            boundLabel({
                                if(get16th(musicPlayer.metronome.timeBars) == i)
                                    if(i == 15) "${get16th(musicPlayer.metronome.timeBars)}:$i|" else " :$i|"
                                else
                                    " :$i|"
                            })
                         }
                    }
                    row()
                    table {
                        label("Sample Discovery") {
                            setFontScale(4f)
                            setAlignment(Align.center)
                        }.cell(expandX = true, fillX = true, align = Align.center, colspan = 4, padTop = 25f)
                        row()
                        boundLabel({ currentNote.toString() })
                        row()
                        dirList = listWidgetOf(currentDirectory.childDirs.toGdxArray())
                        fileList = listWidgetOf(currentDirectory.files.toGdxArray())
                        row()
                        currentSamples = listWidgetOf(selectedSamples.toGdxArray())
                    }.align(Align.top)
                    currentList = dirList
                    setFillParent(true)
                }
                }
        }
    }


}