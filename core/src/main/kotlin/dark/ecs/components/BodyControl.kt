package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class BodyControl: Component, Pool.Poolable {
    val direction = vec2()
    var maxForce = 1000f
    override fun reset() {
        direction.set(Vector2.Zero)
        maxForce = 1000f
    }

    companion object {
        val mapper = mapperFor<BodyControl>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): BodyControl {
            return mapper.get(entity)
        }
    }
}