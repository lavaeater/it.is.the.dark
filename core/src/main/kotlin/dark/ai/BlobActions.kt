package dark.ai

import com.badlogic.ashley.core.Entity
import dark.ecs.components.TargetState
import eater.ai.ashley.AiAction
import eater.ai.ashley.AiActionWithState

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