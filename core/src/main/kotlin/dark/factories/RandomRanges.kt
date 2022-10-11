import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ktx.math.random
import ktx.math.vec2

object RandomRanges {
    val positionRange = -20..20
    fun getRandomPosition(): Vector2 {
        return vec2(positionRange.random() * 5f, positionRange.random() * 5f)
    }

    fun getRandomPositionInBounds(bounds: Rectangle): Vector2 {
        val minX = bounds.x / 10
        val minY = bounds.y / 10
        val maxX = minX + bounds.width / 10
        val maxY = minY + bounds.height / 10

        val returnX = (minX..maxX).random() * 10f
        val returnY = (minY..maxY).random() * 10f
        return vec2(returnX, returnY)
    }
}