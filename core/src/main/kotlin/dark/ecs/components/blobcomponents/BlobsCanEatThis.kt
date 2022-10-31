package dark.ecs.components.blobcomponents

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class BlobsCanEatThis: Component, Pool.Poolable {
    override fun reset() {

    }

    companion object {
        val mapper = mapperFor<BlobsCanEatThis>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): BlobsCanEatThis {
            return mapper.get(entity)
        }
    }
}