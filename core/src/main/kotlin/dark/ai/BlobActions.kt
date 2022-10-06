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
import eater.ai.ashley.AiActionWithState
import eater.ai.ashley.AlsoGenericAction
import eater.core.engine
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.Remove
import eater.physics.addComponent
import ktx.ashley.allOf
import ktx.math.minus
import ktx.math.plus
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class SteerableEntity(val entity: Entity): Steerable<Vector2> {
    private val body by lazy { Box2d.get(entity).body }
    override fun getPosition(): Vector2 {
        return body.position
    }

    override fun getOrientation(): Float {
        return body.angle
    }

    override fun setOrientation(orientation: Float) {
        body.setTransform(body.transform.position, orientation)
    }

    override fun vectorToAngle(vector: Vector2): Float {
        return atan2(-vector.x, vector.y)
    }

    override fun angleToVector(outVector: Vector2, angle: Float): Vector2 {
        outVector.x = -sin(angle.toDouble()).toFloat()
        outVector.y = cos(angle.toDouble()).toFloat()
        return outVector
    }

    override fun newLocation(): Location<Vector2> {
        return Location<Vector2>()
    }

    override fun getZeroLinearSpeedThreshold(): Float {
        TODO("Not yet implemented")
    }

    override fun setZeroLinearSpeedThreshold(value: Float) {
        TODO("Not yet implemented")
    }

    override fun getMaxLinearSpeed(): Float {
        TODO("Not yet implemented")
    }

    override fun setMaxLinearSpeed(maxLinearSpeed: Float) {
        TODO("Not yet implemented")
    }

    override fun getMaxLinearAcceleration(): Float {
        TODO("Not yet implemented")
    }

    override fun setMaxLinearAcceleration(maxLinearAcceleration: Float) {
        TODO("Not yet implemented")
    }

    override fun getMaxAngularSpeed(): Float {
        TODO("Not yet implemented")
    }

    override fun setMaxAngularSpeed(maxAngularSpeed: Float) {
        TODO("Not yet implemented")
    }

    override fun getMaxAngularAcceleration(): Float {
        TODO("Not yet implemented")
    }

    override fun setMaxAngularAcceleration(maxAngularAcceleration: Float) {
        TODO("Not yet implemented")
    }

    override fun getLinearVelocity(): Vector2 {
        TODO("Not yet implemented")
    }

    override fun getAngularVelocity(): Float {
        TODO("Not yet implemented")
    }

    override fun getBoundingRadius(): Float {
        TODO("Not yet implemented")
    }

    override fun isTagged(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setTagged(tagged: Boolean) {
        TODO("Not yet implemented")
    }
}


object BlobActions {
    val splitInTwo = object : AlsoGenericAction("Split") {
        override fun scoreFunction(entity: Entity): Float {
            val props = PropsAndStuff.get(entity)
            val health = props.getHealth()
            return if(health.current > health.max)
                1f
            else 0f
        }

        override fun abort(entity: Entity) {
        }

        override fun act(entity: Entity, deltaTime: Float) {
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
            when (state.state) {
                TargetState.HasTarget -> {
                    if(Box2d.has(state.target!!)) {
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
                            if (food.foodEnergy < 0f)
                                state.apply {
                                    target!!.addComponent<Remove>()
                                    this.state = TargetState.IsDoneWithTarget
                                }
                        }
                    } else {
                        state.state = TargetState.IsDoneWithTarget
                    }
                }

                TargetState.IsDoneWithTarget -> {
                    state.target = null
                    state.state = TargetState.NeedsTarget
                }
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
    val allActions = listOf(goTowardsFood, splitInTwo)
}