package dark.ai

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class FleeStateComponent: Component, Pool.Poolable {
    var state: FleeState = FleeState.NeedsSteering
    override fun reset() {
        state = FleeState.NeedsSteering
    }

    companion object {
        val mapper = mapperFor<FleeStateComponent>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): FleeStateComponent {
            return mapper.get(entity)
        }
    }
}

sealed class FleeState {
    object NeedsSteering: FleeState()
    object IsFleeing: FleeState()
}