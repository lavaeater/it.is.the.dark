package dark.ecs.components.blobcomponents

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Joint
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.Remove
import eater.physics.addComponent
import ktx.ashley.remove
object JointsToDestroy {
    val joints = mutableListOf<Joint>()
    val ropeNodes = mutableMapOf<Body, Entity>()
}
class SlimeRope(val nodes: MutableMap<Body, Entity>, val joints: MutableList<Joint>) {
    var from: Body? = null
    var to: Body? = null
    var toEntity: Entity? = null
    var destroyed = false
    fun destroy() {
        destroyed = true
        JointsToDestroy.joints.addAll(joints)
        joints.clear()
        for (node in nodes) {
            JointsToDestroy.ropeNodes[node.key] = node.value
        }
        nodes.clear()
        from = null
        to = null
        toEntity = null
    }
}