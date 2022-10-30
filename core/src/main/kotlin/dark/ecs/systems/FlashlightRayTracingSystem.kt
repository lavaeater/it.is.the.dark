package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.World
import dark.core.GameSettings
import dark.ecs.components.Blob
import dark.ecs.components.Flashlight
import dark.ecs.components.PropsAndStuff
import eater.ai.steering.box2d.Box2dSteerable
import eater.ecs.ashley.components.GeneralMemory
import eater.ecs.ashley.components.Memory
import eater.ecs.ashley.components.Remove
import eater.ecs.ashley.components.TransformComponent
import eater.injection.InjectionContext.Companion.inject
import eater.physics.getEntity
import eater.physics.isEntity
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.box2d.RayCast
import ktx.box2d.rayCast
import ktx.log.info
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2

class FlashlightRayTracingSystem(private val world: World) : IteratingSystem(
    allOf(
        Flashlight::class,
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

        world.rayCast(lightStart, lightStart + light.direction.cpy().scl(100f)) { fixture, point, normal, fraction ->
            /**
             * If we hit a blob here, that blob should
             * a) take damage
             * b) get very panicked about that whole thing
             *
             * Perhaps blobs has a panic-meter that is reset every time they take damage
             * So when they do, they are panicked for a short period of time, so keep hitting them with that light!
             */
            if (fraction < closestFraction) {
                hit = true
                closestFraction = fraction
                closestFixture = fixture
                closestPoint.set(point)
            }
            RayCast.CONTINUE
        }

        if (hit) {
            if (closestFixture!!.isEntity()) {
                val hitEntity = closestFixture!!.getEntity()
                if (Blob.has(hitEntity) && !Remove.has(hitEntity)) {
                    val blob = Blob.get(hitEntity)
                    val health = PropsAndStuff.get(hitEntity).getHealth()
                    val memories = Memory.get(hitEntity)
                    health.current -= deltaTime * inject<GameSettings>().LightDamage
                    memories.addGeneralMemory(MemoryEvent.HitByLight(lightStart))
                }
            }
        }
    }
}

class FleeLightSystem:IteratingSystem(allOf(
    Blob::class,
    Box2dSteerable::class,
    Memory::class
).exclude(Remove::class).get()) {
    override fun processEntity(entity: Entity?, deltaTime: Float) {
        TODO("Not yet implemented")
    }
}

sealed class MemoryEvent: GeneralMemory {
    class HitByLight(val lightSource: Vector2): MemoryEvent()
}