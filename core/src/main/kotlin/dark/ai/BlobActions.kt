package dark.ai

import RandomRanges
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import dark.ecs.components.BodyControl
import dark.ecs.components.PropsAndStuff
import dark.ecs.components.TargetState
import eater.ai.ashley.AiAction
import eater.ai.ashley.AiActionWithState
import eater.ecs.ashley.components.Box2d
import ktx.math.minus

object BlobActions {
    val goTowardsFood = object: AiActionWithState<TargetState>("Towards Some Place", TargetState::class) {
        override fun scoreFunction(entity: Entity): Float {
            return 0.5f
        }

        override fun initState(state: TargetState) {
            state.target.set(RandomRanges.getRandomPosition())
        }

        override fun abortFunction(entity: Entity) {
            /**
             * State is automatically removed
             */
        }

        override fun actFunction(entity: Entity, state: TargetState, deltaTime: Float) {
            /**
             * So, we should get the box2d and the state and move towards ta
             *
             * We shall not act directly on bodies etc, rather we shall act upon control
             * components of the entity, which is nicer, perhaps?
             */
            val body = Box2d.get(entity).body
            val distanceToFood = body.position.dst(state.target)
            val bodyControl = BodyControl.get(entity)
            if(distanceToFood > 10f) {
                bodyControl.direction.set((state.target - body.position).nor())
            } else {
                bodyControl.direction.set(Vector2.Zero)
                val health = PropsAndStuff.get(entity).getHealth()
                health.current += deltaTime * 10f
                if(health.current > health.max) {
                    //Split in two
                }
            }
        }
    }
    val allActions = listOf<AiAction>()
}