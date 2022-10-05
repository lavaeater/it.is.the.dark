package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ecs.components.BodyControl
import eater.ecs.ashley.components.Box2d
import ktx.ashley.allOf
import ktx.math.times

class BodyControlSystem: IteratingSystem(allOf(Box2d::class, BodyControl::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = Box2d.get(entity).body
        val control = BodyControl.get(entity)

        body.applyForce(control.direction * control.maxForce, body.worldCenter, true)

    }
}