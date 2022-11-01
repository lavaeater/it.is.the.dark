package dark.ecs.components.blobcomponents

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

sealed class ShootAndEatState {
    object HasNotYetShot: ShootAndEatState()
    object HasShot: ShootAndEatState()
    object IsEating: ShootAndEatState()
    object TotallyDone: ShootAndEatState()
}

class ShootingAndEatingTargets: Component, Pool.Poolable {
    var state: ShootAndEatState = ShootAndEatState.HasNotYetShot
    override fun reset() {

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