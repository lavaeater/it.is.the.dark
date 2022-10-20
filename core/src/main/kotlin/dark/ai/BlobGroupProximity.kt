package dark.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.steer.Proximity
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.math.Vector2
import dark.ecs.components.Blob
import dark.ecs.systems.BlobGrouper
import eater.ai.steering.box2d.Box2dSteering

class BlobGroupProximity(
    private var ownerEntity: Entity,
) : Proximity<Vector2> {
    /** Returns the box2d world.  */
    override fun getOwner(): Steerable<Vector2> {
        return Box2dSteering.get(ownerEntity)
    }

    override fun setOwner(owner: Steerable<Vector2>) {
        ownerEntity = (owner as Box2dSteering).body.userData as Entity
    }

    override fun findNeighbors(behaviorCallback: Proximity.ProximityCallback<Vector2>): Int {
        val ownerBlob = Blob.get(ownerEntity)
        val blobEntities = BlobGrouper.getBlobsForGroup(ownerBlob.blobGroup)
        for (blobEntity in blobEntities - ownerEntity) {
            behaviorCallback.reportNeighbor(Box2dSteering.get(blobEntity))
        }
        return blobEntities.count()
    }
}