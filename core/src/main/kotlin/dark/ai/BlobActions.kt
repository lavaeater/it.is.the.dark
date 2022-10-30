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
import dark.ecs.systems.MemoryEvent
import dark.ecs.systems.log
import eater.ai.ashley.AiActionWithStateComponent
import eater.ai.ashley.AlsoGenericAction
import eater.ai.steering.box2d.Box2dLocation
import eater.ai.steering.box2d.Box2dRaycastCollisionDetector
import eater.ai.steering.box2d.Box2dSteerable
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.Memory
import eater.ecs.ashley.components.Remove
import eater.ecs.ashley.components.TransformComponent
import eater.injection.InjectionContext.Companion.inject
import eater.physics.addComponent
import ktx.ashley.allOf
import ktx.log.info
import ktx.math.plus

fun acceptOnlyBlobs(steerable: Steerable<Vector2>): Boolean {
    return steerable is Box2dSteerable && steerable.body.userData != null && steerable.body.userData is Entity && Blob.has(
        steerable.body.userData as Entity
    )
}

fun fleeFromYourMemoriesSteering(
    entity: Entity,
    owner: Steerable<Vector2>,
    avoidObstacles: Boolean,
    onlyLatestMemory: Boolean
): SteeringBehavior<Vector2> {

    val aNiceBlendOfSteering = BlendedSteering(owner)
    if (Memory.has(entity)) {
        val memory = Memory.get(entity)
        if(onlyLatestMemory) {
            val lightMemes = memory.generalMemory.filter { it.key is MemoryEvent.HitByLight }.maxByOrNull { it.value }
            if(lightMemes != null)
                aNiceBlendOfSteering.add(Flee(owner, Box2dLocation((lightMemes.key as MemoryEvent.HitByLight).lightSource, 0f)), 5f)
        } else {
            for (itHurts in memory.generalMemory.keys.filterIsInstance<MemoryEvent.HitByLight>()) {
                info { "The light at: ${itHurts.lightSource} is scary!" }
                aNiceBlendOfSteering.add(Flee(owner, Box2dLocation(itHurts.lightSource, 0f)), 5f)
            }
        }
    }

    return if (avoidObstacles) {
        PrioritySteering(owner)
            .add(aNiceBlendOfSteering)
            .add(
                RaycastObstacleAvoidance(
                    owner, CentralRayWithWhiskersConfiguration(owner, 2.5f, 1f, 15f),
                    Box2dRaycastCollisionDetector(world())
                )
            )
    } else {
        aNiceBlendOfSteering
    }
}

fun composeSteering(
    entity: Entity,
    owner: Steerable<Vector2>,
    mainBehavior: SteeringBehavior<Vector2>,
    avoidObstacles: Boolean
): SteeringBehavior<Vector2> {
    val neighbourGroupProximity = NeighbourProximity(entity)
    val aNiceBlendOfSteering = BlendedSteering(owner)
        .add(mainBehavior, 3f)
        .add(Separation(owner, neighbourGroupProximity), 2f)
        .add(Cohesion(owner, neighbourGroupProximity), 1f)
        .add(Alignment(owner, neighbourGroupProximity), 1f)


    return if (avoidObstacles) {
        PrioritySteering(owner)
            .add(aNiceBlendOfSteering)
            .add(
                RaycastObstacleAvoidance(
                    owner, CentralRayWithWhiskersConfiguration(owner, 2.5f, 1f, 15f),
                    Box2dRaycastCollisionDetector(world())
                )
            )
    } else {
        aNiceBlendOfSteering
    }
}

fun getWanderSteering(entity: Entity, owner: Steerable<Vector2>): SteeringBehavior<Vector2> {
    return composeSteering(
        entity, owner, Wander(owner).apply {
            wanderRate = .25f
            wanderOffset = 15f
            wanderRadius = 250f
            isFaceEnabled = false
        }, true
    )
}

fun getArriveAtFoodSteering(entity: Entity, owner: Steerable<Vector2>, target: Entity): SteeringBehavior<Vector2> {
    return composeSteering(
        entity,
        owner,
        Arrive(owner, Box2dLocation(TransformComponent.get(target).position)).apply {
            arrivalTolerance = 2.5f
        },
        true
    )
}


object BlobActions {
    private val wander =
        object :
            AiActionWithStateComponent<WanderStateComponent>("Wander with Steering", WanderStateComponent::class) {
            override fun scoreFunction(entity: Entity): Float {
                return PropsAndStuff.get(entity).getHealth().normalizedValue * 0.75f
            }

            override fun abortFunction(entity: Entity) {
                Box2dSteerable.get(entity).steeringBehavior = null
            }

            override fun actFunction(entity: Entity, stateComponent: WanderStateComponent, deltaTime: Float) {
                when (stateComponent.state) {
                    WanderState.NotStarted -> {
                        /** Here we add the wander state steering stuff
                         * to this entities steeringthingie
                         */
                        if (Box2dSteerable.has(entity)) {
                            val steerable = Box2dSteerable.get(entity)
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
    private val flee =
        object : AiActionWithStateComponent<FleeStateComponent>("Flee the Light!", FleeStateComponent::class) {
            override fun scoreFunction(entity: Entity): Float {
                return if (Memory.has(entity)) {
                    val memory = Memory.get(entity)
                    if (memory.generalMemory.keys.filterIsInstance<MemoryEvent.HitByLight>()
                            .any()
                    ) {
                        info { "The Score is 1, I wish to flee" }
                        1f
                    } else
                        0f
                } else 0f
            }

            override fun abortFunction(entity: Entity) {
                info { "I shall not flee"}
                val steerable = Box2dSteerable.get(entity)
                steerable.maxLinearSpeed = steerable.maxLinearSpeed / 100f
            }

            override fun actFunction(entity: Entity, stateComponent: FleeStateComponent, deltaTime: Float) {
                info { "I shall flee"}
                when (stateComponent.state) {
                    FleeState.IsFleeing -> {
                        if (Memory.has(entity) && Memory.get(entity).hasGeneralMemoryChanged) {
                            info { "Memory has changed, create new steering" }
                            stateComponent.state = FleeState.NeedsSteering
                        }
                    }

                    FleeState.NeedsSteering -> {
                        val steerable = Box2dSteerable.get(entity)
                        steerable.maxLinearSpeed = steerable.maxLinearSpeed * 10f
                        steerable.maxLinearAcceleration = steerable.maxLinearAcceleration * 100f
                        steerable.steeringBehavior = fleeFromYourMemoriesSteering(entity, steerable, false, true)
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
            val props = PropsAndStuff.get(entity)
            val health = props.getHealth()
            val remainingHealthForNewBlog = health.current / 2f
            health.current = remainingHealthForNewBlog
            val direction = Vector2.X.cpy().rotateDeg((0..359).random().toFloat())
            val at = TransformComponent.get(entity).position + direction.scl(5f)
            createBlob(at, remainingHealthForNewBlog)
        }
    }

    private val searchForAndArriveAtFood = object :
        AiActionWithStateComponent<Target.ArriveAtFoodTarget>(
            "Search for Food!",
            Target.ArriveAtFoodTarget::class
        ) {
        override fun scoreFunction(entity: Entity): Float {
            val health = PropsAndStuff.get(entity).getHealth()
            return if (Target.ArriveAtFoodTarget.has(entity) && Target.ArriveAtFoodTarget.get(entity).target != null && Food.has(
                    Target.ArriveAtFoodTarget.get(entity).target!!
                ) && Food.get(Target.ArriveAtFoodTarget.get(entity).target!!).foodEnergy > 5f
            ) {
                0.7f
            } else {
                1f - health.normalizedValue
            }
        }

        override fun abortFunction(entity: Entity) {
            Box2dSteerable.get(entity).steeringBehavior = null
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
                        val steerable = Box2dSteerable.get(entity)
                        steerable.steeringBehavior =
                            getArriveAtFoodSteering(entity, steerable, stateComponent.target!!)
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
                    val position = TransformComponent.get(entity).position
                    val potentialTarget = engine().getEntitiesFor(foodFamily)
                        .filter {
                            TransformComponent.get(it).position.dst(position) < health.detectionRadius
                        }.randomOrNull()
                    if (potentialTarget != null) {
                        stateComponent.previousDistance =
                            TransformComponent.get(potentialTarget).position.dst(position)
                        stateComponent.state = TargetState.NeedsSteering
                        stateComponent.target = potentialTarget
                        val blob = Blob.get(entity)
                        blob.sendMessageToNeighbours(
                            BlobMessage.FoundAFoodTarget(
                                potentialTarget,
                                Food.get(potentialTarget).foodEnergy,
                                entity
                            )
                        )
                    } else {
                        if (stateComponent.steering == null)
                            stateComponent.steering = getWanderSteering(entity, Box2dSteerable.get(entity))

                        Box2dSteerable.get(entity).steeringBehavior = stateComponent.steering
                    }
                }

                TargetState.IsSteering -> {
                    if (stateComponent.timer > 0f && stateComponent.target != null && Box2d.has(stateComponent.target!!)) {
                        val position = TransformComponent.get(entity).position
                        val targetPosition = TransformComponent.get(stateComponent.target!!).position
                        val distance = position.dst(targetPosition)
                        if (distance < gameSettings.BlobDetectionRadius / 3f) {
                            stateComponent.state = TargetState.ArrivedAtTarget
                        }
                    } else {
                        if (stateComponent.timer < 0f)
                            entity.log("Ran out of time, try something else")
                        stateComponent.state = TargetState.IsDoneWithTarget
                    }
                }

                TargetState.ArrivedAtTarget -> {
                    Box2dSteerable.get(entity).steeringBehavior = null
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
    val allActions = listOf(splitInTwo, searchForAndArriveAtFood, flee)
}