package dark.ecs.components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Joint

class SlimeRope(val nodes: MutableMap<Body, Entity>, val joints: MutableList<Joint>) {
    lateinit var triangle: Triple<Body, Body, Body>
    lateinit var anchorBodies: Pair<Body, Body>
}