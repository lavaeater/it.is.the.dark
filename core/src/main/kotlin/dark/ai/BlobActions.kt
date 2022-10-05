package dark.ai

import Food
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import dark.ecs.components.BodyControl
import dark.ecs.components.PropsAndStuff
import dark.ecs.components.Target
import dark.ecs.components.TargetState
import eater.ai.ashley.AiAction
import eater.ai.ashley.AiActionWithState
import eater.core.engine
import eater.ecs.ashley.components.Box2d
import ktx.ashley.allOf
import ktx.math.minus

object BlobActions {
    val goTowardsFood = object : AiActionWithState<Target>("Towards Some Place", Target::class) {
        override fun scoreFunction(entity: Entity): Float {
            return 0.5f
        }

        val foodFamily = allOf(Food::class, Box2d::class).get()

        override fun abortFunction(entity: Entity) {
            /**
             * State is automatically removed
             */
        }

        override fun actFunction(entity: Entity, state: Target, deltaTime: Float) {
            /**
             * So, we should get the box2d and the state and move towards ta
             *
             * We shall not act directly on bodies etc, rather we shall act upon control
             * components of the entity, which is nicer, perhaps?
             */
            when (state.state) {
                TargetState.HasTarget -> {
                    val body = Box2d.get(entity).body
                    val targetPosition = Box2d.get(state.target!!).body.position
                    val distanceToFood = body.position.dst(targetPosition)
                    val bodyControl = BodyControl.get(entity)
                    if (distanceToFood > 10f) {
                        bodyControl.direction.set((targetPosition - body.position).nor())
                    } else {
                        bodyControl.direction.set(Vector2.Zero)
                        val health = PropsAndStuff.get(entity).getHealth()
                        val toAdd = deltaTime * 10f
                        health.current += toAdd
                        val food = Food.get(state.target!!)
                        food.foodEnergy -= toAdd
                        if (health.current > health.max) {
                            //Split in two
                        }
                    }
                }

                TargetState.IsDoneWithTarget -> {}
                TargetState.NeedsTarget -> {
                    val body = Box2d.get(entity).body
                    val potentialTarget = engine().getEntitiesFor(foodFamily)
                        .minByOrNull { Box2d.get(entity).body.position.dst(body.position) }
                    if(potentialTarget != null) {
                        state.state = TargetState.HasTarget
                        state.target = potentialTarget
                    }
                }
            }


        }
    }
    val allActions = listOf<AiAction>(goTowardsFood)
}