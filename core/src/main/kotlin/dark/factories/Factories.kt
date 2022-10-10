import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dark.ai.BlobActions
import dark.core.GameSettings
import dark.ecs.components.Blob
import dark.ecs.components.BodyControl
import dark.ecs.components.Prop
import dark.ecs.components.PropsAndStuff
import dark.injection.assets
import eater.ai.ashley.AiComponent
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.CameraFollow
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.filter

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

fun createBlob(at:Vector2, health: Float = 100f, settings: GameSettings = inject(), follow: Boolean = false) {
    engine().entity {
        with<Blob>()
        with<PropsAndStuff> {
            props.add(Prop.FloatProp.Health(health))
        }
        with<BodyControl> {
            maxForce = 50f
        }
        with<AiComponent> {
            actions.addAll(BlobActions.allActions)
        }
        if(follow)
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

fun createMap() {
    engine().entity {
        with<Map> {
            mapTextureRegion = TextureRegion(assets().mapOne)
            mapScale = 1.0f
            mapOrigin.set(-20f, -20f)
        }
    }
}

fun createBounds(intLayer: String, tileSize: Int, mapOffset: Vector2) {
    /*
    To make it super easy, we just create a square per int-tile in the layer.
     */
    intLayer.lines().forEachIndexed{y, l ->
        l.forEachIndexed{ x, c ->
            if(c == '1') {
                world().body {
                    type = BodyDef.BodyType.StaticBody
                    position.set()
                }
            }
        }
    }
}