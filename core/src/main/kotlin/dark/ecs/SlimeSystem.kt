//package core.ecs.systems
//
//import com.badlogic.ashley.core.Entity
//import com.badlogic.ashley.systems.IteratingSystem
//import com.badlogic.gdx.graphics.Color
//import com.badlogic.gdx.math.Vector2
//import com.badlogic.gdx.physics.box2d.Body
//import com.badlogic.gdx.physics.box2d.Fixture
//import core.ControlObject
//import core.Direction
//import core.Factories.ball
//import core.Factories.createSlimeEntity
//import core.Factories.createSlimeNode
//import core.ecs.AshleyMappers.slimerComponent
//import core.ecs.AshleyMappers.spriteComponent
//import core.ecs.components.SlimeRope
//import core.ecs.components.SlimerComponent
//import core.world
//import dark.ecs.components.DarkMonster
//import injection.GameConstants
//import injection.GameConstants.outerShellDamp
//import injection.GameConstants.outerShellHz
//import injection.GameConstants.segmentLength
//import ktx.ashley.allOf
//import ktx.box2d.RayCast
//import ktx.box2d.distanceJointWith
//import ktx.box2d.jointWith
//import ktx.box2d.rayCast
//import ktx.math.component1
//import ktx.math.vec2
//
//
//
//class SlimeSystem : IteratingSystem(allOf(SlimerComponent::class).get()) {
//    init {
//        ControlObject.rightTriggerOffCallbacks.add(::destroyCurrentRope)
//    }
//
//    var destroyCurrentRope = false
//    private fun destroyCurrentRope() {
//        destroyCurrentRope = true
//    }
//
//    override fun processEntity(entity: Entity, deltaTime: Float) {
//        val slimer = slimerComponent.get(entity)
//        val endVec = slimer.centerBody.position.cpy().add(ControlObject.aimVector.cpy().scl(15f))
//
//        val anchorPair = slimer.outerPairs.sortedWith(object: Comparator<Pair<Body, Body>> {
//            override fun compare(o1: Pair<Body, Body>, o2: Pair<Body, Body>): Int {
//                val dist1 = (o1.first.position.dst(endVec) + o1.second.position.dst(endVec)) / 2
//                val dist2 = (o2.first.position.dst(endVec) + o2.second.position.dst(endVec)) / 2
//                return dist1.compareTo(dist2)
//            }
//        }).first()
//        if (ControlObject.leftTrigger && ControlObject.leftTriggerEnabled) {
//            ControlObject.leftTriggerEnabled = false
//            /**
//             * 1. Create a new body / entity and then add sections of joints out from it...
//             *
//             * First body should be at endVec
//             */
//
//
//            /**
//             * Then we ray cast from the new node to... mouseposition, of course
//             */
//            val rayCastEnd = slimer.centerBody.position.cpy().add(ControlObject.aimVector.cpy().scl(200f))
//            var lastFraction = 10f
//            lateinit var lastFixture: Fixture
//            val lastHitPoint = vec2()
//            world.rayCast(
//                startX = endVec.x,
//                startY = endVec.y,
//                endX = rayCastEnd.x,
//                endY = rayCastEnd.y
//            ) { fixture, point, normal, fraction ->
//
//                /**
//                 *
//                 * Now, create a slimey rope of string between these.
//                 */
//                if (fraction < lastFraction) {
//                    lastFraction = fraction
//                    lastFixture = fixture
//                    lastHitPoint.set(point)
//                }
//                RayCast.CONTINUE
//            }
//            val rope = SlimeRope(mutableMapOf(), mutableListOf())
//            slimer.ropeySlimey.add(rope)
//            val firstBody = createSlimeNode(endVec, .5f)
//            val entity = createSlimeEntity(firstBody)
//            rope.nodes[firstBody] = entity
//            for (b in anchorPair.toList()) {
//                rope.joints.add(b.distanceJointWith(firstBody) {
//                    this.length = b.position.dst(firstBody.position)
//                    this.frequencyHz = GameConstants.outerShellHz
//                    this.dampingRatio = GameConstants.outerShellDamp
//                })
//            }
//            rope.triangle = Triple(firstBody, anchorPair.first, anchorPair.second)
//            rope.anchorBodies = anchorPair
//
//            if (lastFraction < 1f) {
//                val endPosition = lastHitPoint.cpy()
//                val distance = firstBody.position.dst(endPosition)
//                val numberOfSegments = (distance / segmentLength).toInt()
//
//                val segmentVector = endPosition.cpy().sub(firstBody.position).nor().scl(segmentLength)
//
//                lateinit var currentBody: Body
//                var previousBody = firstBody
//                for (segment in 0 until numberOfSegments) {
//                    val newPos = firstBody.position.cpy().add(segmentVector)
//                    currentBody = createSlimeNode(newPos, .5f)
//                    val currentEntity = createSlimeEntity(currentBody)
//                    rope.nodes[currentBody] = currentEntity
//                    rope.joints.add(currentBody.distanceJointWith(previousBody) {
//                        length = segmentLength / 4
//                        frequencyHz = outerShellHz
//                        dampingRatio = outerShellDamp
//                        collideConnected = false
//                    })
//                    previousBody = currentBody
//                    if (segment == numberOfSegments - 1) {
//                        rope.joints.add(currentBody.distanceJointWith(lastFixture.body) {
//                            localAnchorB.set(lastFixture.body.getLocalPoint(endPosition))
//                            length = 0.1f
//                            frequencyHz = 50f
//                            dampingRatio = 1f
//                            collideConnected = false
//                        })
//                    }
//                }
//            }
//        }
//
//        if(slimer.ropeySlimey.any()) {
//            when (ControlObject.horizontalDirection) {
//                Direction.Left -> slimer.ropeySlimey.nextItem()
//                Direction.Right -> slimer.ropeySlimey.previousItem()
//                else -> {
//                    val pair = slimer.ropeySlimey.sortedBy {
//                        val angleToCentre = slimer.centerBody.position.cpy().sub(it.anchorBodies.first.position).angleRad()
//                        angleToCentre - ControlObject.aimVector.angleRad()
//                    }.first()
//                    slimer.ropeySlimey.selectedIndex = slimer.ropeySlimey.indexOf(pair)
//                }
//            }
//            //Select the one closest to the mousevector
//
//        }
//        if (ControlObject.directionVector != Vector2.Zero) {
//            /**
//             * Take the directionvector and apply it as a force on the normal
//             * of the body in relation to the center point. This might
//             * create a rotating thing
//             *
//             * Or, here's a thing, just start rotating all of the balls,
//             * that works too, right?
//             */
//
//            for (body in slimer.outershell) {
//                body.applyTorque(ControlObject.directionVector.x * 100f * deltaTime, true)
//            }
//        }
//        if(destroyCurrentRope) {
//            if(slimer.ropeySlimey.any()) {
//                val rope = slimer.ropeySlimey.selectedItem
//                slimer.ropeySlimey.remove(rope)
//                for(joint in  rope.joints) {
//                    world.destroyJoint(joint)
//                }
//                for(node in rope.nodes) {
//                    world.destroyBody(node.key)
//                    engine.removeEntity(node.value)
//                }
//            }
//            destroyCurrentRope = false
//        }
//    }
//}