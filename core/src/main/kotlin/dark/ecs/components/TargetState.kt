package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class TargetState : Component, Pool.Poolable {
    val target = vec2()
    override fun reset() {
        target.setZero()
    }

    companion object {
        val mapper = mapperFor<TargetState>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }

        fun get(entity: Entity): TargetState {
            return mapper.get(entity)
        }
    }
}