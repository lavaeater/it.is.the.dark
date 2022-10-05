package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

sealed class TargetState {
    object NeedsTarget:TargetState()
    object HasTarget:TargetState()
    object IsDoneWithTarget:TargetState()

}

class Target : Component, Pool.Poolable {
    var target: Entity? = null
    var state: TargetState = TargetState.NeedsTarget
    override fun reset() {
        target = null
        state = TargetState.NeedsTarget
    }

    companion object {
        val mapper = mapperFor<Target>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }

        fun get(entity: Entity): Target {
            return mapper.get(entity)
        }
    }
}