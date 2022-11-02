package dark.ecs.components.blobcomponents

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.Box2d
import ktx.ashley.mapperFor
import ktx.ashley.remove

class ShootingAndEatingTargets : Component, Pool.Poolable {
    var state: ShootAndEatState = ShootAndEatState.HasNotYetShot
    var rope: SlimeRope? = null
    override fun reset() {
        state = ShootAndEatState.HasNotYetShot
        rope?.destroy()
        rope = null
    }

    companion object {
        val mapper = mapperFor<ShootingAndEatingTargets>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }

        fun get(entity: Entity): ShootingAndEatingTargets {
            return mapper.get(entity)
        }
    }
}