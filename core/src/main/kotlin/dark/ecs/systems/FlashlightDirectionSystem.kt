package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.World
import dark.ecs.components.Blob
import dark.ecs.components.BodyControl
import dark.ecs.components.Flashlight
import eater.ecs.ashley.components.Remove
import eater.ecs.ashley.components.TransformComponent
import eater.physics.getEntity
import kotlinx.coroutines.internal.artificialFrame
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.box2d.RayCast
import ktx.box2d.rayCast
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2

class FlashlightDirectionSystem: IteratingSystem(
    allOf(
        Flashlight::class,
        BodyControl::class,
        TransformComponent::class
    ).exclude(Remove::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val light = Flashlight.get(entity)
        val bodyControl = BodyControl.get(entity)
        val lightPos = TransformComponent.get(entity).position.cpy()
        light.direction.set(bodyControl.aimDirection)
        lightPos.add(light.direction * light.offset)
        light.light.setOriginBasedPosition(lightPos.x, lightPos.y)
        light.light.rotation = light.direction.angleDeg() - 90f
    }
}

class FlashlightRayTracingSystem(private val world: World): IteratingSystem(
    allOf(Flashlight::class,
        TransformComponent::class
    ).exclude(Remove::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val lightStart = TransformComponent.get(entity).position.cpy()
        val light = Flashlight.get(entity)
        lightStart.add(light.direction * light.offset)

        var closestFraction = 1.0f
        var closestFixture: Fixture? = null
        var closestPoint = vec2()
        var hit = false

        world.rayCast(lightStart, lightStart + light.direction.cpy().scl(50f)) { fixture, point, normal, fraction ->
            /**
             * If we hit a blob here, that blob should
             * a) take damage
             * b) get very panicked about that whole thing
             *
             * Perhaps blobs has a panic-meter that is reset every time they take damage
             * So when they do, they are panicked for a short period of time, so keep hitting them with that light!
             */
            if(fraction < closestFraction) {
                hit = true
                closestFraction = fraction
                closestFixture = fixture
                closestPoint.set(point)
            }
            RayCast.CONTINUE
        }

        if(hit) {
            val hitEntity = closestFixture!!.getEntity()
            if(Blob.has(hitEntity))
        }
    }
}