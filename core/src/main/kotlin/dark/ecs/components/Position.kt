package dark.ecs.components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Joint
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import eater.core.selectedItemListOf

data class Position(var x: Float, var y: Float) : Component<Position> {
    override fun type(): ComponentType<Position> = Position

    companion object : ComponentType<Position>()
}

data class DarkMonster(val centerBody: Body,) : Component<DarkMonster> {
    val outershell = mutableListOf<Body>()
    val outerPairs = selectedItemListOf<Pair<Body, Body>>()
    val allJoints = mutableListOf<Joint>()
    val ropeySlimey = selectedItemListOf<SlimeRope>()
    val allSections = mutableListOf<Triple<Body, Body, Body>>()

    override fun type() = DarkMonster

    companion object : ComponentType<DarkMonster>()
}

class SlimeRope(val nodes: MutableMap<Body, Entity>, val joints: MutableList<Joint>) {
    lateinit var triangle: Triple<Body, Body, Body>
    lateinit var anchorBodies: Pair<Body, Body>
}