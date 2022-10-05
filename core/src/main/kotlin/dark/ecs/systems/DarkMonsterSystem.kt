package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ecs.components.DarkMonster
import ktx.ashley.allOf

class DarkMonsterSystem: IteratingSystem(allOf(DarkMonster::class).get()){
    override fun processEntity(entity: Entity?, deltaTime: Float) {
        TODO("Not yet implemented")
    }
}

