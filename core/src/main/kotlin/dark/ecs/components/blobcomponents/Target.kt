package dark.ecs.components.blobcomponents

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.steer.SteeringBehavior
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

sealed class Target : Component, Pool.Poolable {
    var target: Entity? = null
    var state: TargetState = TargetState.NeedsTarget
    var previousDistance = 0f
    var timer = 10f
    var steering: SteeringBehavior<Vector2>? = null

    override fun reset() {
        timer = 10f
        steering = null
        previousDistance = 0f
        target = null
        state = TargetState.NeedsTarget
    }

    companion object {
        val mapper = mapperFor<Target>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }

        fun get(entity: Entity): Target {
            return mapper.get(entity)
        }
    }

    class GenericTarget : Target() {

        companion object {
            val mapper = mapperFor<GenericTarget>()
            fun has(entity: Entity): Boolean {
                return mapper.has(entity)
            }

            fun get(entity: Entity): GenericTarget {
                return mapper.get(entity)
            }
        }
    }

    class HuntingTarget: Target() {
        companion object {
            val mapper = mapperFor<HuntingTarget>()
            fun has(entity: Entity): Boolean {
                return mapper.has(entity)
            }

            fun get(entity: Entity): HuntingTarget {
                return mapper.get(entity)
            }
        }
    }
    class MoveTowardsFoodTarget : Target() {
        companion object {
            val mapper = mapperFor<MoveTowardsFoodTarget>()
            fun has(entity: Entity): Boolean {
                return mapper.has(entity)
            }

            fun get(entity: Entity): MoveTowardsFoodTarget {
                return mapper.get(entity)
            }
        }
    }
}