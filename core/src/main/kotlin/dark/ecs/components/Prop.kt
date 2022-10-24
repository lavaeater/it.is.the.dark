package dark.ecs.components

import com.badlogic.gdx.math.MathUtils

sealed class Prop(val name: String) {

    sealed class FloatProp(name: String, var current: Float = 100f, val min: Float = 0f, val max: Float = 100f):Prop(name) {
        val normalizedValue: Float
            get() = MathUtils.norm(min, max, MathUtils.clamp(current, 0f, 1f))
        class Health(current: Float = 100f,min: Float = 0f, max: Float = 100f) : FloatProp("Health", current, min, max)

    }
}