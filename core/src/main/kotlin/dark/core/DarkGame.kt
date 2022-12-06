package dark.core

import com.badlogic.gdx.audio.Sound
import dark.injection.Context
import dark.screens.GameScreen
import dark.screens.MenuScreen
import dark.screens.MusicVisualizerScreen
import dark.screens.SoundMachineScreen
import eater.core.MainGame
import eater.injection.InjectionContext.Companion.inject
import ktx.async.KtxAsync


class DarkGame : MainGame() {
    override fun create() {
        KtxAsync.initiate()
        Context.initialize(this)

        addScreen(MenuScreen(this))
        addScreen(MusicVisualizerScreen(this))
        addScreen(SoundMachineScreen(this))
        addScreen(inject<GameScreen>())
        setScreen<SoundMachineScreen>()
    }

    fun gameOver() {
        setScreen<MenuScreen>()
    }
}

