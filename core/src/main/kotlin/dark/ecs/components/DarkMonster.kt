package dark.ecs.components


import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Joint
import com.badlogic.gdx.utils.Pool.Poolable
import dark.ecs.components.blobcomponents.SlimeRope
import eater.core.selectedItemListOf
import ktx.ashley.mapperFor

class DarkMonster: Component, Poolable {
    lateinit var centerBody: Body
    val outershell = mutableListOf<Body>()
    val outerPairs = selectedItemListOf<Pair<Body, Body>>()
    val allJoints = mutableListOf<Joint>()
    val ropeySlimey = selectedItemListOf<SlimeRope>()
    val allSections = mutableListOf<Triple<Body, Body, Body>>()
    override fun reset() {

    }

    companion object {
        val mapper = mapperFor<DarkMonster>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): DarkMonster {
            return mapper.get(entity)
        }
    }
}