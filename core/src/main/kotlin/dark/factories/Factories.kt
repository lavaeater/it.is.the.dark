import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.utils.Pool
import dark.ai.BlobActions
import dark.core.GameSettings
import dark.ecs.components.BlobMonster
import dark.ecs.components.BodyControl
import dark.ecs.components.Prop
import dark.ecs.components.PropsAndStuff
import eater.ai.ashley.AiComponent
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.AgentProperties
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.CameraFollow
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.entity
import ktx.ashley.mapperFor
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.filter
import ktx.math.vec2
import kotlin.experimental.or

object Categories {
    const val none: Short = 0
    const val blob: Short = 1
    const val food: Short = 2

    val whatBlobsCollideWith = blob or food
    val whatFoodCollidesWith = blob

}

object RandomRanges {
    val positionRange = -20..20
    fun getRandomPosition(): Vector2 {
        return vec2(positionRange.random()*5f, positionRange.random() * 5f)
    }
}

fun createLight() {
    engine().entity {
        with<Light>()
        with<Box2d> {
            body = world().body {
                type = BodyDef.BodyType.DynamicBody
                position.set(RandomRanges.getRandomPosition())
                circle(1.0f) {

                }
            }
        }
    }
}

fun createFood() {
    engine().entity {
        with<Food>()
        with<Box2d> {
            body = world().body {
                type = BodyDef.BodyType.StaticBody
                position.set(RandomRanges.getRandomPosition())
                circle(1.0f) {
                    filter {
                        categoryBits = Categories.food
                        maskBits = Categories.whatFoodCollidesWith
                    }
                }
            }
        }
    }
}

class Blob: Component, Pool.Poolable {
    override fun reset() {

    }

    companion object {
        val mapper = mapperFor<Blob>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): Blob {
            return mapper.get(entity)
        }
    }
}


fun createBlob(at:Vector2, health: Float = 100f, settings: GameSettings = inject()) {
    engine().entity {
        with<Blob>()
        with<PropsAndStuff> {
            props.add(Prop.Health(health))
        }
        with<BodyControl> {
            maxForce = 50f
        }
        with<AiComponent> {
            actions.addAll(BlobActions.allActions)
        }
        with<CameraFollow>()
        with<Box2d> {
            body = world().body {
                type = BodyDef.BodyType.DynamicBody
                position.set(at)
                circle(1.0f) {
                    filter {
                        categoryBits = Categories.blob
                        maskBits = Categories.whatBlobsCollideWith
                    }
                }
            }
        }
    }
}

fun createDarkEntity(at: Vector2, radius: Float): Body {
    return world().body {
        type = BodyDef.BodyType.DynamicBody
        position.set(at)
        fixedRotation = false
        circle(radius) {
            density = .1f
        }
    }
}