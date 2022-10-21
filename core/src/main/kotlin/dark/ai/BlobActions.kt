package dark.ai

import Food
import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.ai.steer.SteeringBehavior
import com.badlogic.gdx.ai.steer.behaviors.*
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import createBlob
import dark.core.GameSettings
import dark.ecs.components.*
import dark.ecs.components.Target
import dark.ecs.systems.BlobGrouper
import eater.ai.ashley.AiActionWithStateComponent
import eater.ai.ashley.AlsoGenericAction
import eater.ai.steering.box2d.Box2dLocation
import eater.ai.steering.box2d.Box2dSteering
import eater.core.engine
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.Remove
import eater.injection.InjectionContext.Companion.inject
import eater.physics.addComponent
import ktx.ashley.allOf
import ktx.log.info
import ktx.math.plus

sealed class WanderState {
    object NotStarted: WanderState()
    object Running: WanderState()
}

class WanderStateComponent: Component, Pool.Poolable {
    var state: WanderState = WanderState.NotStarted
    override fun reset() {
        state = WanderState.NotStarted
    }
}

fun getWanderSteering(entity: Entity, owner: Steerable<Vector2>) : SteeringBehavior<Vector2> {
    val blobGroupProximity = BlobGroupProximity(entity)
    return PrioritySteering(owner).apply {
        add(BlendedSteering(owner).apply {
            add(Wander(owner).apply {
                wanderRate = .1f
                wanderOffset = 10f
                wanderRadius = 250f
                isFaceEnabled = false
            }, 0.4f)
            add(Separation(owner, blobGroupProximity).apply {

            }, 0.25f)
            add(Cohesion(owner, blobGroupProximity).apply {

            }, 0.25f)
            add(Alignment(owner, blobGroupProximity).apply {

            }, 0.1f)
        })
        add(RaycastObstacleAvoidance(owner).apply {
            rayConfiguration = CentralRayWithWhiskersConfiguration(owner, 5f, 2.5f, 15f)
        })
    }
}

fun getArriveAtFoodSteering(entity: Entity, owner: Steerable<Vector2>, target: Entity): SteeringBehavior<Vector2> {
    val blobGroupProximity = BlobGroupProximity(entity)
    return BlendedSteering(owner).apply {
        add(Arrive(owner, Box2dLocation(Box2d.get(target).body.position)).apply {
            arrivalTolerance = 2.5f
        }, 0.4f)
        add(Separation(owner, blobGroupProximity).apply {

        }, 0.3f)
        add(Cohesion(owner, blobGroupProximity).apply {

        }, 0.2f)
        add(Alignment(owner, blobGroupProximity).apply {

        }, 0.1f)
    }
}

object BlobActions {

    private val aMessageForYouSir = object : AlsoGenericAction("There is a message for you, sir!") {
        override fun scoreFunction(entity: Entity): Float {
            val blob = Blob.get(entity)
            return if(blob.messageQueue.isEmpty) 0f else 1f
        }

        override fun abort(entity: Entity) {
            /*Hmm?*/
        }

        override fun act(entity: Entity, deltaTime: Float) {
            val blob = Blob.get(entity)
            val message = blob.messageQueue.removeFirst()
            when(message) {
                is BlobMessage.FoundAFoodTarget -> {
                    /**
                     * We can simply add the move-towards-food-state here:
                     */
                    entity.addComponent<Target.ArriveAtFoodTarget> {
                        target = message.target
                        state = TargetState.NeedsSteering
                    }
                }
            }
        }

    }

    private val wander = object : AiActionWithStateComponent<WanderStateComponent>("Wander with Steering", WanderStateComponent::class) {
        override fun scoreFunction(entity: Entity): Float {
            return 0.3f
        }

        override fun abortFunction(entity: Entity) {
            Box2dSteering.get(entity).steeringBehavior = null
        }

        override fun actFunction(entity: Entity, stateComponent: WanderStateComponent, deltaTime: Float) {
            when(stateComponent.state) {
                WanderState.NotStarted -> {
                    /** Here we add the wander state steering stuff
                     * to this entities steeringthingie
                     */
                    if(Box2dSteering.has(entity)) {
                        val steerable = Box2dSteering.get(entity)
                        steerable.steeringBehavior = getWanderSteering(entity, steerable)
                        stateComponent.state = WanderState.Running
                    }

                }
                WanderState.Running -> {
                    //The steering handles this one.
                }
            }
        }
    }
    
    private val splitInTwo = object : AlsoGenericAction("Split") {
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
    private val arriveAtFood = object : AiActionWithStateComponent<Target.ArriveAtFoodTarget>("Towards Some Place", Target.ArriveAtFoodTarget::class) {
        override fun scoreFunction(entity: Entity): Float {
            val position = Box2d.get(entity).body.position
            return if(Target.ArriveAtFoodTarget.has(entity) && Target.ArriveAtFoodTarget.get(entity).target != null) 1.0f else if(engine().getEntitiesFor(foodFamily).any { Box2d.get(it).body.position.dst(position) < gameSettings.BlobDetectionRadius})
                0.5f else 0.0f
        }

        val foodFamily = allOf(Food::class, Box2d::class).get()
        val gameSettings by lazy { inject<GameSettings>() }

        override fun abortFunction(entity: Entity) {
            /**
             * State is automatically removed
             */
        }

        override fun actFunction(entity: Entity, stateComponent: Target.ArriveAtFoodTarget, deltaTime: Float) {
            /**
             * So, we should get the box2d and the state and move towards ta
             *
             * We shall not act directly on bodies etc, rather we shall act upon control
             * components of the entity, which is nicer, perhaps?
             */
            when (stateComponent.state) {
                TargetState.NeedsSteering -> {
                    if(Box2d.has(stateComponent.target!!)) {
                        val steerable = Box2dSteering.get(entity)
                        steerable.steeringBehavior = getArriveAtFoodSteering(entity, steerable, stateComponent.target!!)
                        stateComponent.state = TargetState.IsSteering
                    } else {
                        stateComponent.state = TargetState.IsDoneWithTarget
                    }
                }

                TargetState.IsDoneWithTarget -> {
                    stateComponent.target = null
                    stateComponent.state = TargetState.NeedsTarget
                }
                TargetState.NeedsTarget -> {
                    val body = Box2d.get(entity).body
                    val potentialTarget = engine().getEntitiesFor(foodFamily)
                        .filter { Box2d.get(it).body.position.dst(body.position) < 30f && Food.get(it).foodEnergy > 10f}
                        .randomOrNull()
                    if(potentialTarget != null) {
                        stateComponent.state = TargetState.NeedsSteering
                        stateComponent.target = potentialTarget
                        val blob = Blob.get(entity)
                        BlobGrouper.sendMessageToGroup(blob.blobGroup, BlobMessage.FoundAFoodTarget(potentialTarget, entity))
                    }
                }

                TargetState.IsSteering -> {
                    val steerable = Box2dSteering.get(entity)
                    val behavior = steerable.steeringBehavior as Arrive
                    if(behavior.calculateSteering(steerable.steeringOutput).isZero) {
                        /* we have arrived */
                        stateComponent.state = TargetState.ArrivedAtTarget
                        steerable.steeringBehavior = null
                    }
                }

                TargetState.ArrivedAtTarget -> {
                    val health = PropsAndStuff.get(entity).getHealth()
                    val toAdd = deltaTime * 25f
                    health.current += toAdd
                    val food = Food.get(stateComponent.target!!)
                    food.foodEnergy -= toAdd
                    if (food.foodEnergy < 0f)
                        stateComponent.apply {
                            target!!.addComponent<Remove>()
                            this.state = TargetState.IsDoneWithTarget
                        }
                }
            }


        }
    }
    val allActions = listOf(splitInTwo, wander, arriveAtFood, aMessageForYouSir)
}