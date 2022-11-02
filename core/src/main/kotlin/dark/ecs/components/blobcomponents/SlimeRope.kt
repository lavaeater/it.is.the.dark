package dark.ecs.components.blobcomponents

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Joint

class SlimeRope(val nodes: MutableMap<Body, Entity>, val joints: MutableList<Joint>) {
    lateinit var from: Body
    lateinit var to: Body
    lateinit var toEntity: Entity
}