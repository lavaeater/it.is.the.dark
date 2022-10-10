import com.badlogic.gdx.math.Vector2
import ktx.math.vec2

object RandomRanges {
    val positionRange = -20..20
    fun getRandomPosition(): Vector2 {
        return vec2(positionRange.random() * 5f, positionRange.random() * 5f)
    }
}