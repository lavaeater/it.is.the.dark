package dark.ecs.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.physics.box2d.World
import dark.ecs.components.blobcomponents.JointsToDestroy
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.Remove
import ktx.ashley.remove

class RemoveRopesSystem(private val world: World):EntitySystem() {
    override fun update(deltaTime: Float) {
//        if(JointsToDestroy.joints.any()) {
//            val joints = JointsToDestroy.joints.toList()
//            JointsToDestroy.joints.removeAll(joints)
//            for (joint in joints) {
//                world.destroyJoint(joint)
//            }
//        }
        if(JointsToDestroy.ropeNodes.any()) {
            val bodies = JointsToDestroy.ropeNodes.toList()
            for(ropeNode in bodies) {
                JointsToDestroy.ropeNodes.remove(ropeNode.first)
                if(!Remove.has(ropeNode.second)) {
                    world.destroyBody(ropeNode.first)
                    ropeNode.second.remove<Box2d>()
                    engine.removeEntity(ropeNode.second)
                } else {
                    val thisIsIt = "THis is why it crashed"
                }
            }
        }
    }
}