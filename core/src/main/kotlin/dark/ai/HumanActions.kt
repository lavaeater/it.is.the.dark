package dark.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.ai.steer.SteeringBehavior
import com.badlogic.gdx.ai.steer.behaviors.*
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration
import com.badlogic.gdx.math.Vector2
import dark.core.GameSettings
import eater.ecs.ashley.components.LightComponent
import dark.ecs.components.blobcomponents.Target
import dark.ecs.components.blobcomponents.TargetState
import eater.ai.ashley.AiAction
import eater.ai.ashley.AiActionWithStateComponent
import eater.ai.steering.box2d.Box2dLocation
import eater.ai.steering.box2d.Box2dRaycastCollisionDetector
import eater.ai.steering.box2d.Box2dSteerable
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.Remove
import eater.ecs.ashley.components.TransformComponent
import eater.injection.InjectionContext
import ktx.ashley.allOf
import ktx.ashley.exclude

fun getArriveAtLightSteering(owner: Steerable<Vector2>, target: Entity): SteeringBehavior<Vector2> {
    return BlendedSteering(owner).apply {
        add(Arrive(owner, Box2dLocation(TransformComponent.get(target).position)).apply {
            arrivalTolerance = 2.5f
        }, 0.8f)
        add(
            RaycastObstacleAvoidance(
                owner, CentralRayWithWhiskersConfiguration(owner, 2.5f, 1f, 15f),
                Box2dRaycastCollisionDetector(world())
            ), 0.2f
        )
    }
}

fun getHumanWanderSteering(owner: Steerable<Vector2>) : SteeringBehavior<Vector2> {
    return PrioritySteering(owner).apply {
        add(Wander(owner).apply {
            wanderRate = .1f
            wanderOffset = 30f
            wanderRadius = 150f
            isFaceEnabled = false
        })
        add(RaycastObstacleAvoidance(owner).apply {
            rayConfiguration = CentralRayWithWhiskersConfiguration(owner, 5f, 2.5f, 15f)
        })
    }
}

object HumanActions {
    private val arriveAtLight = object :
        AiActionWithStateComponent<Target.GenericTarget>("Move towards the light", Target.GenericTarget::class) {
        override fun scoreFunction(entity: Entity): Float {
            val position = TransformComponent.get(entity).position
            return if (Target.GenericTarget.has(entity))
                0.8f
            else if (engine().getEntitiesFor(lightComponentFamily)
                    .any { TransformComponent.get(it).position.dst(position) < gameSettings.HumanLightDetectionRadius }
            )
                0.8f
            else 0.0f
        }

        override fun abortFunction(entity: Entity) {
        }

        val lightComponentFamily = allOf(LightComponent::class, TransformComponent::class).exclude(Remove::class).get()
        val gameSettings by lazy { InjectionContext.inject<GameSettings>() }


        override fun actFunction(entity: Entity, stateComponent: Target.GenericTarget, deltaTime: Float): Boolean {
            /**
             * So, we should get the box2d and the state and move towards ta
             *
             * We shall not act directly on bodies etc, rather we shall act upon control
             * components of the entity, which is nicer, perhaps?
             */
            when (stateComponent.state) {
                TargetState.NeedsSteering -> {
                    if (Box2d.has(stateComponent.target!!)) {
                        val steerable = Box2dSteerable.get(entity)
                        steerable.steeringBehavior = getArriveAtLightSteering(steerable, stateComponent.target!!)
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
                    val position = TransformComponent.get(entity).position
                    val potentialTarget = engine().getEntitiesFor(lightComponentFamily)
                        .minByOrNull {
                            TransformComponent.get(it).position.dst(position)
                        }
                    if (potentialTarget != null && TransformComponent.get(potentialTarget).position.dst(position) < gameSettings.HumanLightDetectionRadius) {
                        stateComponent.previousDistance = TransformComponent.get(potentialTarget).position.dst(position)
                        stateComponent.state = TargetState.NeedsSteering
                        stateComponent.target = potentialTarget
                    } else {
                        abort(entity)
                    }
                }

                TargetState.IsSteering -> {
                    /*
                    Bah, check distance manually
                     */
                    if (stateComponent.target != null && Box2d.has(stateComponent.target!!)) {
                        val position = TransformComponent.get(entity).position
                        val targetPosition = TransformComponent.get(stateComponent.target!!).position
                        val distance = position.dst(targetPosition)
                        if (distance < 2.5f) {
                            stateComponent.state = TargetState.ArrivedAtTarget
                        } else {
                            stateComponent.previousDistance = distance
                        }
                    } else {
                        stateComponent.state = TargetState.IsDoneWithTarget
                    }
                }

                TargetState.ArrivedAtTarget -> {
                    /** Diminish fear - and stay in the light
                     *
                     */
                }
            }
            return false
        }
    }

    private val wander =
        object : AiActionWithStateComponent<WanderStateComponent>("Human wandering", WanderStateComponent::class) {
            override fun scoreFunction(entity: Entity): Float {
                return 0.3f
            }

            override fun abortFunction(entity: Entity) {
                Box2dSteerable.get(entity).steeringBehavior = null
            }

            override fun actFunction(entity: Entity, stateComponent: WanderStateComponent, deltaTime: Float): Boolean {
                when (stateComponent.state) {
                    WanderState.NotStarted -> {
                        /** Here we add the wander state steering stuff
                         * to this entities steeringthingie
                         */
                        if (Box2dSteerable.has(entity)) {
                            val steerable = Box2dSteerable.get(entity)
                            steerable.steeringBehavior = getHumanWanderSteering(steerable)
                            stateComponent.state = WanderState.Running
                        }

                    }

                    WanderState.Running -> {
                        //The steering handles this one.
                    }
                }
                return false
            }
        }

    val actions = listOf<AiAction>(wander, arriveAtLight)
}