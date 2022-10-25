package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ai.getArriveAtFoodSteering
import dark.ecs.components.*
import dark.ecs.components.Target
import eater.ai.ashley.AiComponent
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
                    if(Target.ArriveAtFoodTarget.has(entity)) {
                        val tc = Target.ArriveAtFoodTarget.get(entity)
                        if(tc.target != null && Food.has(tc.target!!)) {
                            val currentTarget = Food.get(tc.target!!)
                            if(currentTarget.foodEnergy < message.energy) {
                                tc.target = message.target
                            }
                        }
                    }
                }
                is BlobMessage.TakeSomeOfMyHealth -> {
                    PropsAndStuff.get(entity).getHealth().current += message.healthToAdd
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