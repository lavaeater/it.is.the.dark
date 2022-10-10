import kotlin.experimental.or

object Categories {
    const val none: Short = 0
    const val blob: Short = 1
    const val food: Short = 2

    val whatBlobsCollideWith = blob or food
    val whatFoodCollidesWith = blob

}