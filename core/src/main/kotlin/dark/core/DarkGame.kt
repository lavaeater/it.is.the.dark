package dark.core

import dark.injection.Context
import dark.screens.GameScreen
import dark.screens.MenuScreen
import eater.screens.MusicVisualizerScreen
import eater.screens.SampleExplorerScreen
import eater.core.MainGame
import eater.injection.InjectionContext.Companion.inject
import ktx.async.KtxAsync


class DarkGame : MainGame() {
    override fun create() {
        KtxAsync.initiate()
        Context.initialize(this)

        addScreen(MenuScreen(this))
        addScreen(MusicVisualizerScreen(this))
        addScreen(SampleExplorerScreen(this))
        addScreen(inject<GameScreen>())
        setScreen<MusicVisualizerScreen>()
    }

    fun gameOver() {
        setScreen<MenuScreen>()
    }
}

