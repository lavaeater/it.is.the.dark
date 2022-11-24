package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.World
import dark.core.GameSettings
import dark.ecs.components.Flashlight
import dark.ecs.components.PropsAndStuff
import dark.ecs.components.blobcomponents.Blob
import dark.ecs.systems.stackai.MemoryEvent
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
        lateinit var closestFixture: Fixture
        var hit = false
        val hitPoint = vec2()
        val ray = light.direction.cpy().scl(100f)

        val rayNumber = 10
        val lightCone = 30f
        val anglePerRay = lightCone / rayNumber
        ray.rotateDeg(-lightCone / 2f)

        for (r in 0 until rayNumber) {
            if (r > 0)
                ray.rotateDeg(anglePerRay)

            world.rayCast(lightStart, lightStart + light.direction.cpy().scl(100f)) { fixture, point, _, fraction ->
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
                    hitPoint.set(point)
                    closestFraction = fraction
                    closestFixture = fixture
                }
                if (fraction < 0.01f)
                    RayCast.TERMINATE
                else
                    RayCast.CONTINUE
            }

            if (hit) {
                if (closestFixture.isEntity()) {
                    val hitEntity = closestFixture.getEntity()
                    if (Blob.has(hitEntity) && !Remove.has(hitEntity)) {
                        val health = PropsAndStuff.get(hitEntity).getHealth()
                        val memories = Memory.get(hitEntity)
                        health.current -= deltaTime * inject<GameSettings>().LightDamage
                        memories.addGeneralMemory(MemoryEvent.HitByLight(hitPoint))
                    }
                }
            }
        }
    }
}

