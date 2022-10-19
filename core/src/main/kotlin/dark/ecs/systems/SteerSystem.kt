package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.ai.GdxAI
import eater.ai.steering.box2d.Box2dSteering
import ktx.ashley.allOf

class AiTimePieceSystem:EntitySystem() {
    override fun update(deltaTime: Float) {
        GdxAI.getTimepiece().update(deltaTime)
    }
}

class SteerSystem: IteratingSystem(allOf(Box2dSteering::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val steer = Box2dSteering.get(entity)
        steer.update(deltaTime)
    }
}