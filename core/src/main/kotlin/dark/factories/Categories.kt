import kotlin.experimental.or

object Categories {
    const val none: Short = 0
    const val blob: Short = 1
    const val food: Short = 2
    const val human: Short = 4
    const val walls: Short = 8
    const val lights: Short = 16

    val whatBlobsCollideWith = blob or food or walls or human or lights
    val whatFoodCollidesWith = blob
    val whatHumansCollideWith = blob or human or walls or lights
    val whatWallsCollideWith = blob or human
    val whatLightsCollideWith = blob or human or walls

}