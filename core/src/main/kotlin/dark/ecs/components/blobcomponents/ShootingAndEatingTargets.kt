package dark.ecs.components.blobcomponents

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import eater.core.engine
import eater.core.world
import ktx.ashley.mapperFor

class ShootingAndEatingTargets : Component, Pool.Poolable {
    var state: ShootAndEatState = ShootAndEatState.HasNotYetShot
    lateinit var rope: SlimeRope
    override fun reset() {
        state = ShootAndEatState.HasNotYetShot
        for (joint in rope.joints) {
            world().destroyJoint(joint)
        }
        for (node in rope.nodes) {
            world().destroyBody(node.key)
            engine().removeEntity(node.value)
        }
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