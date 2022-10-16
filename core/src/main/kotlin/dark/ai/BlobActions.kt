package dark.ai

import Food
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.ai.utils.Location
import com.badlogic.gdx.math.Vector2
import createBlob
import dark.ecs.components.BodyControl
import dark.ecs.components.PropsAndStuff
import dark.ecs.components.Target
import dark.ecs.components.TargetState
import dark.ecs.systems.BlobGrouper
import eater.ai.ashley.AiActionWithState
import eater.ai.ashley.AlsoGenericAction
import eater.core.engine
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.Remove
import eater.physics.addComponent
import ktx.ashley.allOf
import ktx.log.info
import ktx.math.minus
import ktx.math.plus
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object BlobActions {
    val splitInTwo = object : AlsoGenericAction("Split") {
        override fun scoreFunction(entity: Entity): Float {
            val props = PropsAndStuff.get(entity)
            val health = props.getHealth()
            return if(BlobGrouper.canSplit && health.current > (health.max * 1.5f))
                1f
            else 0f
        }

        override fun abort(entity: Entity) {
        }

        override fun act(entity: Entity, deltaTime: Float) {
            info { "We are splitting UP" }
            val props = PropsAndStuff.get(entity)
            val health = props.getHealth()
            val remainingHealthForNewBlog = health.current / 2f
            health.current = remainingHealthForNewBlog
            val direction = Vector2.X.cpy().rotateDeg((0..359).random().toFloat())
            val at = Box2d.get(entity).body.position + direction.scl(5f)
            createBlob(at, remainingHealthForNewBlog)
        }

    }
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
//            when (state.state) {
//                TargetState.HasTarget -> {
//                    val random = (1..1000).random()
//                    if(Box2d.has(state.target!!)) {
//                        val body = Box2d.get(entity).body
//                        val targetPosition = Box2d.get(state.target!!).body.position
//                        val distanceToFood = body.position.dst(targetPosition)
//                        val bodyControl = BodyControl.get(entity)
//                        if (distanceToFood > 5f) {
//                            bodyControl.direction.set((targetPosition - body.position).nor())
//                        } else {
//                            bodyControl.direction.set(Vector2.Zero)
//                            val health = PropsAndStuff.get(entity).getHealth()
//                            val toAdd = deltaTime * 25f
//                            health.current += toAdd
//                            val food = Food.get(state.target!!)
//                            food.foodEnergy -= toAdd
//                            if (food.foodEnergy < 0f)
//                                state.apply {
//                                    target!!.addComponent<Remove>()
//                                    this.state = TargetState.IsDoneWithTarget
//                                }
//                        }
//                    } else {
//                        state.state = TargetState.IsDoneWithTarget
//                    }
//                }
//
//                TargetState.IsDoneWithTarget -> {
//                    state.target = null
//                    state.state = TargetState.NeedsTarget
//                }
//                TargetState.NeedsTarget -> {
//                    val body = Box2d.get(entity).body
//                    val potentialTarget = engine().getEntitiesFor(foodFamily)
////                        .filter { Food.get(it).foodEnergy > 50f }
//                        .filter { Box2d.get(it).body.position.dst(body.position) < 30f && Food.get(it).foodEnergy > 10f}
//                        .randomOrNull()
////                        .minByOrNull { Box2d.get(entity).body.position.dst(body.position) }
//                    if(potentialTarget != null) {
//                        state.state = TargetState.HasTarget
//                        state.target = potentialTarget
//                    }
//                }
//            }


        }
    }
    val allActions = listOf(splitInTwo, goTowardsFood)
}