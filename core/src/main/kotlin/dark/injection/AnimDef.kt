package dark.injection

sealed class AnimDef(val name: String, val rowIndex: Int, val numberOfFrames: Int) {
    object Walk: AnimDef("walk", 3, 4)
    object Interact: AnimDef("interact", 1, 3)

    companion object {
        val animDefs = listOf(Walk, Interact)
    }
}