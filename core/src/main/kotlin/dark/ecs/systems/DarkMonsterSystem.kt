package dark.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import dark.ecs.components.DarkMonster

class DarkMonsterSystem: IteratingSystem(family = family { all(DarkMonster)}) {
    override fun onTickEntity(entity: Entity) {

    }
}

