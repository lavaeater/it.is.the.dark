package dark.ecs.components


import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Joint
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentHook
import com.github.quillraven.fleks.ComponentType
import eater.core.selectedItemListOf

class DarkMonster : Component<DarkMonster> {
    lateinit var centerBody: Body
    val outershell = mutableListOf<Body>()
    val outerPairs = selectedItemListOf<Pair<Body, Body>>()
    val allJoints = mutableListOf<Joint>()
    val ropeySlimey = selectedItemListOf<SlimeRope>()
    val allSections = mutableListOf<Triple<Body, Body, Body>>()

    override fun type() = DarkMonster

    companion object : ComponentType<DarkMonster>() {
        val onRemove: ComponentHook<DarkMonster> = {entity, darkMonster ->
            darkMonster.centerBody.world.destroyBody(darkMonster.centerBody)
            darkMonster.centerBody.userData = null
        }
    }
}