package dark.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.steer.Proximity
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.math.Vector2
import dark.ecs.components.blobcomponents.Blob
import eater.ai.steering.box2d.Box2dSteerable

class NeighbourProximity(
    private var ownerEntity: Entity,
    acceptFunction: (Steerable<Vector2>) -> Boolean = { _ -> true }
) : Proximity<Vector2> {
    /** Returns the box2d world.  */
    override fun getOwner(): Steerable<Vector2> {
        return Box2dSteerable.get(ownerEntity)
    }

    override fun setOwner(owner: Steerable<Vector2>) {
        ownerEntity = (owner as Box2dSteerable).body.userData as Entity
    }

    override fun findNeighbors(behaviorCallback: Proximity.ProximityCallback<Vector2>): Int {
        val ownerBlob = Blob.get(ownerEntity)
        var count = 0
        for (blobEntity in ownerBlob.neighbours) {
            if(Box2dSteerable.has(blobEntity.key)) {
                behaviorCallback.reportNeighbor(Box2dSteerable.get(blobEntity.key))
                count++
            }
        }
        return ownerBlob.neighbours.count()
    }
}