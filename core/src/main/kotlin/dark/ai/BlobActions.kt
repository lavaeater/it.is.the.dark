package dark.ai

import Categories
import InRangeForShootingTarget
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.ai.steer.SteeringBehavior
import com.badlogic.gdx.ai.steer.behaviors.*
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import createBlob
import dark.core.GameSettings
import dark.ecs.components.*
import dark.ecs.components.Food
import dark.ecs.components.blobcomponents.*
import dark.ecs.components.blobcomponents.Target
import dark.ecs.systems.BlobGrouper
import dark.ecs.systems.MemoryEvent
import dark.ecs.systems.log
import eater.ai.ashley.*
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
import ktx.ashley.entity
import ktx.ashley.remove
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.distanceJointWith
import ktx.box2d.filter
import ktx.log.info
import ktx.math.plus
import kotlin.math.pow
import kotlin.reflect.typeOf

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
        if (onlyLatestMemory) {
            val lightMemes = memory.generalMemory.filter { it.key is MemoryEvent.HitByLight }.maxByOrNull { it.value }
            if (lightMemes != null)
                aNiceBlendOfSteering.add(
                    Flee(
                        owner,
                        Box2dLocation((lightMemes.key as MemoryEvent.HitByLight).lightSource, 0f)
                    ), 5f
                )
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

fun getArriveAtFoodSteering(
    entity: Entity,
    owner: Steerable<Vector2>,
    target: Entity,
    tolerance: Float = 2.5f,
    avoidObstacles: Boolean = true
): SteeringBehavior<Vector2> {
    return composeSteering(
        entity,
        owner,
        Arrive(owner, Box2dLocation(TransformComponent.get(target).position)).apply {
            arrivalTolerance = tolerance
        },
        avoidObstacles
    )
}


object BlobActions {
    private val gameSettings by lazy { inject<GameSettings>() }

    private val aimlesslyWander =
        object :
            AiActionWithStateComponent<WanderStateComponent>("Wander with Steering", WanderStateComponent::class) {
            override fun scoreFunction(entity: Entity): Float {
                return PropsAndStuff.get(entity).getHealth().normalizedValue * 0.75f
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
                            steerable.steeringBehavior = getWanderSteering(entity, steerable)
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
    private val fleeTheLight =
        object : AiActionWithStateComponent<FleeStateComponent>("Flee the Light!", FleeStateComponent::class) {
            override fun scoreFunction(entity: Entity): Float {
                return if (Memory.has(entity)) {
                    val memory = Memory.get(entity)
                    if (memory.generalMemory.keys.filterIsInstance<MemoryEvent.HitByLight>()
                            .any()
                    ) {
                        1f
                    } else
                        0f
                } else 0f
            }

            override fun abortFunction(entity: Entity) {
                info { "I shall not flee" }
                val steerable = Box2dSteerable.get(entity)
                steerable.maxLinearSpeed = inject<GameSettings>().BlobMaxSpeed
                steerable.maxLinearAcceleration = inject<GameSettings>().BlobMaxAcceleration
                steerable.steeringBehavior = null
            }

            override fun actFunction(entity: Entity, stateComponent: FleeStateComponent, deltaTime: Float): Boolean {
                when (stateComponent.state) {
                    FleeState.IsFleeing -> {
                        if (Memory.has(entity) && Memory.get(entity).hasGeneralMemoryChanged) {
                            info { "Memory has changed, create new steering" }
                            stateComponent.state = FleeState.NeedsSteering
                        }
                    }

                    FleeState.NeedsSteering -> {
                        val steerable = Box2dSteerable.get(entity)
                        steerable.maxLinearSpeed = inject<GameSettings>().BlobMaxSpeed * 5f
                        steerable.maxLinearAcceleration = inject<GameSettings>().BlobMaxAcceleration * 5f
                        steerable.steeringBehavior = fleeFromYourMemoriesSteering(
                            entity, steerable,
                            avoidObstacles = false,
                            onlyLatestMemory = true
                        )
                    }
                }
                return false
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

        override fun act(entity: Entity, deltaTime: Float): Boolean {
            val props = PropsAndStuff.get(entity)
            val health = props.getHealth()
            val remainingHealthForNewBlog = health.current / 2f
            health.current = remainingHealthForNewBlog
            val direction = Vector2.X.cpy().rotateDeg((0..359).random().toFloat())
            val at = TransformComponent.get(entity).position + direction.scl(5f)
            createBlob(at, remainingHealthForNewBlog)
            return false
        }
    }

    private var previousHuntState: TargetState = TargetState.IsDoneWithTarget
    private var previousShootState: ShootAndEatState = ShootAndEatState.IsEating

    private fun logIfNewHuntState(newState: TargetState) {
        if (previousHuntState != newState) {
            info { newState.toString() }
            previousHuntState = newState
        }
    }

    private fun logIfNewShootState(newState: ShootAndEatState) {
        if (previousShootState != newState) {
            info { newState.toString() }
            previousShootState = newState
        }
    }


    private val shootTheFood = ConsideredActionWithState(
        "Shoot the food - then eat it",
        { entity, stateComponent, deltaTime ->
            logIfNewShootState(stateComponent.state)
            when (stateComponent.state) {
                ShootAndEatState.HasNotYetShot -> {
                    val target = InRangeForShootingTarget.get(entity).target
                    if (target != null &&
                        TransformComponent
                            .get(target)
                            .position
                            .dst2(
                                TransformComponent
                                    .get(entity)
                                    .position
                            ) < (gameSettings.BlobDetectionRadius / 2f).pow(2)) {
                        val targetBody = Box2d.get(target).body
                        val blob = Blob.get(entity)

                        val rope = SlimeRope(mutableMapOf(), mutableListOf())
                        stateComponent.rope = rope
                        blob.ropes.add(rope)

                        rope.from = Box2d.get(entity).body
                        rope.to = targetBody
                        rope.toEntity = target

                        // Create some nice little nodes for this
                        val distance = rope.from!!.position.dst(rope.to!!.position)
                        val segmentLength = 3f
                        val segments = (distance / segmentLength).toInt()
                        lateinit var currentBody: Body
                        var previousBody = rope.from!!
                        val segmentVector = rope.to!!.position.cpy().sub(rope.from!!.position).nor().scl(segmentLength)
                        for (segment in 0 until segments) {
                            /**
                             */
                            val newPos = rope.from!!.position.cpy().add(segmentVector)
                            currentBody = createRopeNodeBody(newPos, 0.5f)
                            val currentEntity = createRopeNodeEntity(currentBody)
                            rope.nodes[currentBody] = currentEntity
                            rope.joints.add(currentBody.distanceJointWith(previousBody) {
                                length = segmentLength / 4
                                frequencyHz = gameSettings.outerShellHz
                                dampingRatio = gameSettings.outerShellDamp
                                collideConnected = false
                            })
                            previousBody = currentBody
                            if (segment == segments - 1) {
                                rope.joints.add(currentBody.distanceJointWith(rope.to!!) {
                                    //localAnchorB.set(rope.to.getLocalPoint(endPosition))
                                    length = segmentLength / 4
                                    frequencyHz = gameSettings.outerShellHz
                                    dampingRatio = gameSettings.outerShellDamp
                                    collideConnected = false
                                })
                            }
                        }
                        stateComponent.state = ShootAndEatState.HasShot
                    } else
                        stateComponent.state = ShootAndEatState.TotallyDone
                }

                ShootAndEatState.HasShot -> {
                    /**
                     * Aww, man, what do we do?
                     * We should pull the rope, and pull fast, somehow.
                     */
                    val target = stateComponent.rope!!.toEntity
                    if (Box2d.has(target!!)) {
                        val health = PropsAndStuff.get(entity).getHealth()
                        val toAdd = deltaTime * gameSettings.BlobEatRate
                        if (Human.has(target)) {
                            val humanHealth = PropsAndStuff.get(target).getHealth()
                            humanHealth.current -= toAdd
                            health.current += toAdd
                            if(health.current < 0f)
                                stateComponent.rope?.destroy()
                        } else if (Food.has(target)) {
                            val food = Food.get(target)
                            food.foodEnergy -= toAdd
                            health.current += toAdd
                            if (food.foodEnergy < 0f) {
                                stateComponent.rope?.destroy()
                                target.addComponent<Remove>()
                                stateComponent.state = ShootAndEatState.TotallyDone
                            }
                        }
                    } else
                        stateComponent.state = ShootAndEatState.TotallyDone
                }

                ShootAndEatState.IsEating -> {
                }

                ShootAndEatState.TotallyDone -> {
                    entity.remove<InRangeForShootingTarget>()
                }
            }

            false
        },
        ShootingAndEatingTargets::class,
        0f..0.8f,
        DoIHaveThisComponentConsideration(InRangeForShootingTarget::class)
    )

    private val moveTowardsFood = ConsideredActionWithState(
        "Move towards food - if we have a target!",
        { entity, stateComponent, deltaTime ->
            logIfNewHuntState(stateComponent.state)
            when (stateComponent.state) {
                TargetState.NeedsTarget -> {
                    /**
                     * We now that we have a target and that we are hungry, so now we
                     * search through our memory for that target!
                     */
                    val memory = Memory.get(entity)
                    val entities = memory.seenEntities[typeOf<BlobsCanEatThis>()]
                    val potentialTarget = entities?.keys?.randomOrNull()
                    if (potentialTarget != null) {
                        stateComponent.target = potentialTarget //DOH!
                        stateComponent.state = TargetState.NeedsSteering
                    } else {
                        stateComponent.state = TargetState.IsDoneWithTarget
                    }
                    false
                }

                TargetState.NeedsSteering -> {
                    // Get into shooting distance of the target
                    if (stateComponent.target != null && Box2d.has(stateComponent.target!!)) {
                        val steerable = Box2dSteerable.get(entity)

                        steerable.steeringBehavior =
                            getArriveAtFoodSteering(
                                entity,
                                steerable,
                                stateComponent.target!!,
                                gameSettings.BlobDetectionRadius / 2.25f, // Otherwise we stop just outside of range for shooting
                                true
                            )
                        stateComponent.steering = steerable.steeringBehavior
                        stateComponent.state = TargetState.IsSteering
                    } else {
                        stateComponent.state = TargetState.IsDoneWithTarget
                    }
                    false
                }

                TargetState.IsSteering -> {
                    if (stateComponent.target == null || !Box2d.has(stateComponent.target!!) || !TransformComponent.has(stateComponent.target!!)) {
                        stateComponent.state = TargetState.IsDoneWithTarget
                    }
                    /** DOH-OH!**/
                    val targetPosition = TransformComponent.get(stateComponent.target!!).position
                    val position = TransformComponent.get(entity).position
                    if (position.dst2(targetPosition) < (gameSettings.BlobDetectionRadius / 2f).pow(2)) {
                        stateComponent.state = TargetState.ArrivedAtTarget
                    }
                    false
                }

                TargetState.ArrivedAtTarget -> {
                    if (stateComponent.target != null && Box2d.has(stateComponent.target!!)) {
                        entity.addComponent<InRangeForShootingTarget> {
                            target = stateComponent.target!!
                        }
                    }
                    stateComponent.state = TargetState.IsDoneWithTarget
                    true
                }

                TargetState.IsDoneWithTarget -> {
                    entity.remove<FoundHuntingTarget>()
                    true
                }
            }
        },
        Target.HuntingTarget::class,
        0f..0.7f,
        DoIRememberThisConsideration(BlobsCanEatThis::class)
    )

    /**
     * This action doesn't need any score or anything else - it stores stuff into memories
     * So if we are hungry AND have, in our memory, something we can eat, we should move
     * towards that thing.
     *
     * The list of things remembered will be updated always...
     */
    private val senseFood = ConsideredAction(
        "If Hungry, find food, you know",
        0.0f..0.0f,
        { _, _ -> false },
        CanISeeThisConsideration(BlobsCanEatThis::class)
    )

    private val searchForAndArriveAtFood = object :
        AiActionWithStateComponent<Target.MoveTowardsFoodTarget>(
            "Search for Food!",
            Target.MoveTowardsFoodTarget::class
        ) {
        override fun scoreFunction(entity: Entity): Float {
            val health = PropsAndStuff.get(entity).getHealth()
            return if (Target.MoveTowardsFoodTarget.has(entity) && Target.MoveTowardsFoodTarget.get(entity).target != null && ((Food.has(
                    Target.MoveTowardsFoodTarget.get(entity).target!!
                ) && Food.get(Target.MoveTowardsFoodTarget.get(entity).target!!).foodEnergy > 5f) || (Human.has(
                    Target.MoveTowardsFoodTarget.get(
                        entity
                    ).target!!
                )))
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
        val humanFamily = allOf(Human::class, Box2d::class).get()
        val gameSettings by lazy { inject<GameSettings>() }

        override fun actFunction(
            entity: Entity,
            stateComponent: Target.MoveTowardsFoodTarget,
            deltaTime: Float
        ): Boolean {
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
                    val potentialHumanTarget = engine().getEntitiesFor(humanFamily)
                        .filter {
                            TransformComponent.get(it).position.dst(position) < health.detectionRadius
                        }.randomOrNull()


                    val potentialTarget = engine().getEntitiesFor(foodFamily)
                        .filter {
                            TransformComponent.get(it).position.dst(position) < health.detectionRadius
                        }.randomOrNull()
                    if (potentialHumanTarget != null) {
                        stateComponent.previousDistance =
                            TransformComponent.get(potentialHumanTarget).position.dst(position)
                        stateComponent.state = TargetState.NeedsSteering
                        stateComponent.target = potentialHumanTarget
                        val blob = Blob.get(entity)
                        blob.sendMessageToNeighbours(
                            BlobMessage.FoundAFoodTarget(
                                potentialHumanTarget,
                                PropsAndStuff.get(potentialHumanTarget).getHealth().current,
                                entity
                            )
                        )
                    } else if (potentialTarget != null) {
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
                    if (stateComponent.timer > 0f && stateComponent.target != null && TransformComponent.has(
                            stateComponent.target!!
                        )
                    ) {
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
                    if (TransformComponent.has(stateComponent.target!!)) {
                        val position = TransformComponent.get(entity).position
                        val targetPos = TransformComponent.get(stateComponent.target!!).position
                        val distance = position.dst(targetPos)
                        if (distance > gameSettings.BlobDetectionRadius / 3f) {
                            stateComponent.state = TargetState.IsDoneWithTarget
                        } else {
                            Box2dSteerable.get(entity).steeringBehavior = null
                            val health = PropsAndStuff.get(entity).getHealth()
                            val toAdd = deltaTime * gameSettings.BlobEatRate
                            if (Human.has(stateComponent.target!!)) {
                                val humanHealth = PropsAndStuff.get(stateComponent.target!!).getHealth()
                                humanHealth.current -= toAdd
                                health.current += toAdd
                            } else if (Food.has(stateComponent.target!!)) {
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
            return false
        }
    }
    val allActions = listOf(senseFood, moveTowardsFood, shootTheFood, fleeTheLight)
}

fun createRopeNodeEntity(body: Body): Entity {
    val entity = engine().entity {
        with<Box2d> {
            this.body = body
        }
        with<TransformComponent>()
    }
    body.userData = entity
    return entity
}

fun createRopeNodeBody(at: Vector2, radius: Float): Body {
    return world().body {
        type = BodyDef.BodyType.DynamicBody
        position.set(at)
        fixedRotation = false
        circle(radius) {
            density = .1f
            filter {
                categoryBits = Categories.ropes
                maskBits = Categories.whatRopesCollideWith
            }
        }
    }
}