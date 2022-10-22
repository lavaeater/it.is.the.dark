package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ai.getArriveAtFoodSteering
import dark.ecs.components.*
import eater.ai.steering.box2d.Box2dSteering
import ktx.ashley.allOf


class BlobMessageHandlingSystem : IteratingSystem(allOf(Blob::class, StackAiComponent::class, Box2dSteering::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val blob = Blob.get(entity)
        if (blob.messageQueue.any()) {
            /**
             * All blobs are separate... kinda?
             *
             * So, a Blob finds something, experiences something, it sends a message
             * to all other blobs.
             *
             * Depending on their internal state (health etc), they shall make different decisions
             * depending on this information
             *
             * The information might be outdated... of course...
             *
             * Let's go with groups, still.
             *
             */
            when (val message = blob.messageQueue.removeFirst()) {
                is BlobMessage.FoundAFoodTarget -> {
                    /*
                    If the top task is "go for food", it will take
                    a while for the task "check for food" to suddenly show up.

                    These things can be controlled by something, a system or something, who knows?
                    We simply ALWAYS add a go-for-food-stack-task when we get this message.
                     */
                    val aiStack = StackAiComponent.get(entity)
                    aiStack.actionStack.addFirst(moveTowardsFoodAction(entity, Box2dSteering.get(entity), message.target))
                }
                is BlobMessage.TakeSomeOfMyHealth -> {

                }
            }
        }
    }

    private fun moveTowardsFoodAction(entity: Entity, owner: Box2dSteering, target: Entity): StackedAiAction<StackFoodState> {
        return object : StackedAiAction<StackFoodState>("Go towards food", StackFoodState.NeedsSteering) {
            var previousDistance = 0f
            override fun actFunction(entity: Entity, state: StackFoodState, deltaTime: Float): Boolean {
                return when(state) {
                    StackFoodState.Done -> {
                        true
                    }
                    StackFoodState.Eating -> {
                        false
                    }
                    StackFoodState.InTransit -> {
                        false
                    }
                    StackFoodState.NeedsSteering -> {
                        owner.steeringBehavior = getArriveAtFoodSteering(entity, owner, target)
                        false
                    }

                    StackFoodState.Paused -> {
                        false
                    }
                }
            }

            override fun pauseFunction() : StackFoodState {
                return StackFoodState.Paused
            }

            override fun abortFunction(entity: Entity) {

            }

        }
    }
}

sealed class StackFoodState {
    object NeedsSteering: StackFoodState()
    object InTransit: StackFoodState()
    object Eating: StackFoodState()
    object Done: StackFoodState()
    object Paused: StackFoodState()
}