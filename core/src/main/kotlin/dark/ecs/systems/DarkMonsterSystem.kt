package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ecs.components.BodyControl
import dark.ecs.components.DarkMonster
import eater.ecs.ashley.components.Box2d
import ktx.ashley.allOf

class BodyControlSystem: IteratingSystem(allOf(Box2d::class, BodyControl::class).get()) {
    override fun processEntity(entity: Entity?, deltaTime: Float) {
        TODO("Not yet implemented")
    }
}

class DarkMonsterSystem: IteratingSystem(allOf(DarkMonster::class).get()){
    override fun processEntity(entity: Entity?, deltaTime: Float) {
        TODO("Not yet implemented")
    }
}

