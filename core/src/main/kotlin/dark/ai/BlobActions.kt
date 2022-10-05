package dark.ai

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import eater.ai.ashley.AiAction
import eater.ai.ashley.AiActionWithState
import eater.ai.ashley.GenericAction
import eater.ai.ashley.GenericActionWithState
import ktx.ashley.mapperFor
import ktx.math.vec2

class TargetState: Component, Pool.Poolable {
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

object BlobActions {
    val goTowardsFood = object: AiActionWithState<TargetState>("Towards Some Place", TargetState::class) {
        override fun scoreFunction(entity: Entity): Float {
            return 0.5f
        }

        override fun abortFunction(entity: Entity) {
            /**
             * State is automatically removed
             */
        }

        override fun actFunction(entity: Entity, state: TargetState, deltaTime: Float) {
            /**
             * So, we should get the box2d and the state and move towards ta
             */
        }

    }
    val allActions = listOf<AiAction>()
}