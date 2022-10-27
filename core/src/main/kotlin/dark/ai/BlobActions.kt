package dark.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.ai.steer.SteeringBehavior
import com.badlogic.gdx.ai.steer.behaviors.*
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration
import com.badlogic.gdx.math.Vector2
import createBlob
import dark.core.GameSettings
import dark.ecs.components.*
import dark.ecs.components.Food
import dark.ecs.components.Target
import dark.ecs.systems.BlobGrouper
import dark.ecs.systems.log
import dark.ecs.systems.sendMessageTo
import eater.ai.ashley.AiActionWithStateComponent
import eater.ai.ashley.AlsoGenericAction
import eater.ai.steering.box2d.Box2dLocation
import eater.ai.steering.box2d.Box2dRadiusProximity
import eater.ai.steering.box2d.Box2dRaycastCollisionDetector
import eater.ai.steering.box2d.Box2dSteering
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.Remove
import eater.injection.InjectionContext.Companion.inject
import eater.physics.addComponent
import ktx.ashley.allOf
import ktx.log.info
import ktx.math.plus

fun acceptOnlyBlobs(steerable: Steerable<Vector2>): Boolean {
    return steerable is Box2dSteering && steerable.body.userData != null && steerable.body.userData is Entity && Blob.has(
        steerable.body.userData as Entity
    )
}

fun getWanderSteering(entity: Entity, owner: Steerable<Vector2>): SteeringBehavior<Vector2> {
    val box2dProximity =
        Box2dRadiusProximity(owner, world(), inject<GameSettings>().BlobDetectionRadius * 1.5f, ::acceptOnlyBlobs)
    return PrioritySteering(owner).apply {
        add(BlendedSteering(owner).apply {
            add(
                Wander(owner).apply {
                    wanderRate = .25f
                    wanderOffset = 15f
                    wanderRadius = 250f
                    isFaceEnabled = false
                },
                3f
            )
            add(Separation(owner, box2dProximity).apply {

            }, 2f)
            add(Cohesion(owner, box2dProximity).apply {

            }, 1f)
            add(Alignment(owner, box2dProximity).apply {

            }, 1f)
        })
        add(
            RaycastObstacleAvoidance(
                owner, CentralRayWithWhiskersConfiguration(owner, 5f, 2.5f, 15f),
                Box2dRaycastCollisionDetector(world())
            )
        )
    }
}

fun getArriveAtFoodSteering(entity: Entity, owner: Steerable<Vector2>, target: Entity): SteeringBehavior<Vector2> {
    val box2dProximity =
        Box2dRadiusProximity(owner, world(), inject<GameSettings>().BlobDetectionRadius * 1.5f, ::acceptOnlyBlobs)
    return PrioritySteering(owner).apply {
        add(BlendedSteering(owner).apply {
            add(Arrive(owner, Box2dLocation(Box2d.get(target).body.position)).apply {
                arrivalTolerance = 2.5f
            }, 2f)
            add(Cohesion(owner, box2dProximity).apply {

            }, 1f)
            add(Separation(owner, box2dProximity).apply {
            }, 1.5f)
        })
        add(
            RaycastObstacleAvoidance(
                owner, CentralRayWithWhiskersConfiguration(owner, 2.5f, 1f, 15f),
                Box2dRaycastCollisionDetector(world())
            )
        )
    }
}


object BlobActions {
    private val wander =
        object : AiActionWithStateComponent<WanderStateComponent>("Wander with Steering", WanderStateComponent::class) {
            override fun scoreFunction(entity: Entity): Float {
                return PropsAndStuff.get(entity).getHealth().normalizedValue * 0.75f
            }

            override fun abortFunction(entity: Entity) {
                Box2dSteering.get(entity).steeringBehavior = null
            }

            override fun actFunction(entity: Entity, stateComponent: WanderStateComponent, deltaTime: Float) {
                when (stateComponent.state) {
                    WanderState.NotStarted -> {
                        /** Here we add the wander state steering stuff
                         * to this entities steeringthingie
                         */
                        if (Box2dSteering.has(entity)) {
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
            return if (BlobGrouper.canSplit)
                health.current / health.max * .5f
            else
                0f
        }

        override fun abort(entity: Entity) {
        }

        override fun act(entity: Entity, deltaTime: Float) {
            entity.log("We are splitting UP")
            val props = PropsAndStuff.get(entity)
            val health = props.getHealth()
            val remainingHealthForNewBlog = health.current / 2f
            health.current = remainingHealthForNewBlog
            val direction = Vector2.X.cpy().rotateDeg((0..359).random().toFloat())
            val at = Box2d.get(entity).body.position + direction.scl(5f)
            createBlob(at, remainingHealthForNewBlog)
        }
    }

    private val searchForAndArriveAtFood = object :
        AiActionWithStateComponent<Target.ArriveAtFoodTarget>("Search for Food!", Target.ArriveAtFoodTarget::class) {
        override fun scoreFunction(entity: Entity): Float {
            val health = PropsAndStuff.get(entity).getHealth()
            return if (Target.ArriveAtFoodTarget.has(entity) && Target.ArriveAtFoodTarget.get(entity).target != null && Food.has(
                    Target.ArriveAtFoodTarget.get(entity).target!!
                ) && Food.get(Target.ArriveAtFoodTarget.get(entity).target!!).foodEnergy > 5f
            )
                1f
            else
                1f - health.normalizedValue
        }

        override fun abortFunction(entity: Entity) {
            Box2dSteering.get(entity).steeringBehavior = null
        }

        val foodFamily = allOf(Food::class, Box2d::class).get()
        val gameSettings by lazy { inject<GameSettings>() }

        override fun actFunction(entity: Entity, stateComponent: Target.ArriveAtFoodTarget, deltaTime: Float) {
            /**
             * So, we should get the box2d and the state and move towards ta
             *
             * We shall not act directly on bodies etc, rather we shall act upon control
             * components of the entity, which is nicer, perhaps?
             */
            stateComponent.timer -= deltaTime
            when (stateComponent.state) {
                TargetState.NeedsSteering -> {
                    if (Box2d.has(stateComponent.target!!)) {
                        val steerable = Box2dSteering.get(entity)
                        steerable.steeringBehavior = getArriveAtFoodSteering(entity, steerable, stateComponent.target!!)
                        stateComponent.state = TargetState.IsSteering
                    } else {
                        stateComponent.state = TargetState.IsDoneWithTarget
                    }
                }

                TargetState.IsDoneWithTarget -> {
                    stateComponent.target = null
                    abort(entity)
                }

                TargetState.NeedsTarget -> {
                    val health = PropsAndStuff.get(entity).getHealth()
                    val body = Box2d.get(entity).body
                    val potentialTarget = engine().getEntitiesFor(foodFamily)
                        .filter {
                            Box2d.get(it).body.position.dst(body.position) < health.detectionRadius
                        }.randomOrNull()
                    if (potentialTarget != null) {
                        stateComponent.previousDistance = Box2d.get(potentialTarget).body.position.dst(body.position)
                        stateComponent.state = TargetState.NeedsSteering
                        stateComponent.target = potentialTarget
                        val blob = Blob.get(entity)
                        blob.neighbours.sendMessageTo(
                            BlobMessage.FoundAFoodTarget(
                                potentialTarget,
                                Food.get(potentialTarget).foodEnergy,
                                entity
                            )
                        )
                    } else {
                        if (stateComponent.steering == null)
                            stateComponent.steering = getWanderSteering(entity, Box2dSteering.get(entity))

                        Box2dSteering.get(entity).steeringBehavior = stateComponent.steering
                    }
                }

                TargetState.IsSteering -> {

                    if (stateComponent.timer > 0f && stateComponent.target != null && Box2d.has(stateComponent.target!!)) {
                        val position = Box2d.get(entity).body.position
                        val targetPosition = Box2d.get(stateComponent.target!!).body.position
                        val distance = position.dst(targetPosition)
                        if (distance < 2.5f) {
                            stateComponent.state = TargetState.ArrivedAtTarget
                        }
                    } else {
                        if (stateComponent.timer < 0f)
                            entity.log("Ran out of time, try something else")
                        stateComponent.state = TargetState.IsDoneWithTarget
                    }
                }

                TargetState.ArrivedAtTarget -> {
                    Box2dSteering.get(entity).steeringBehavior = null
                    val health = PropsAndStuff.get(entity).getHealth()
                    val toAdd = deltaTime * 50f
                    if (Food.has(stateComponent.target!!)) {
                        val food = Food.get(stateComponent.target!!)
                        food.foodEnergy -= toAdd
                        health.current += toAdd
                        if (food.foodEnergy < 0f) {
                            stateComponent.apply {
                                target!!.addComponent<Remove>()
                                state = TargetState.IsDoneWithTarget
                            }
                        }
                    } else {
                        stateComponent.state = TargetState.IsDoneWithTarget
                    }
                }
            }
        }
    }
    val allActions = listOf(splitInTwo, searchForAndArriveAtFood)
}