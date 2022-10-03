package dark.core

import dark.screens.GameScreen
import dark.screens.MenuScreen
import eater.core.MainGame
import ktx.async.KtxAsync

class DarkGame : MainGame() {
    override fun create() {
        KtxAsync.initiate()

        addScreen(MenuScreen(this))
        addScreen(GameScreen(this))
        setScreen<GameScreen>()
    }
}

