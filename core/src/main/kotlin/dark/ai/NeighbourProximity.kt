package dark.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.steer.Proximity
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture
import dark.ecs.components.Blob
import eater.ai.steering.box2d.Box2dSteering

class NeighbourProximity(
    private var ownerEntity: Entity,
    acceptFunction: (Steerable<Vector2>) -> Boolean = { _ -> true }
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
        for (blobEntity in ownerBlob.neighbours) {
            behaviorCallback.reportNeighbor(Box2dSteering.get(blobEntity.second))
        }
        return ownerBlob.neighbours.count()
    }
}