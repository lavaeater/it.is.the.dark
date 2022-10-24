package dark.ecs.components

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
    var steering: SteeringBehavior<Vector2>? = null

    override fun reset() {
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

    class ArriveAtFoodTarget : Target() {

        companion object {
            val mapper = mapperFor<ArriveAtFoodTarget>()
            fun has(entity: Entity): Boolean {
                return mapper.has(entity)
            }

            fun get(entity: Entity): ArriveAtFoodTarget {
                return mapper.get(entity)
            }
        }
    }
}