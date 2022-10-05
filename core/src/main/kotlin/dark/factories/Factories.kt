import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dark.core.GameSettings
import dark.ecs.components.*
import eater.core.fleks
import eater.core.world
import eater.ecs.fleks.components.Box2dBody
import eater.ecs.fleks.components.CameraFollow
import eater.ecs.fleks.components.Sprite
import eater.injection.InjectionContext.Companion.inject
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.distanceJointWith
import ktx.math.vec2

fun createBlob(at:Vector2, settings: GameSettings = inject()) {
    with(fleks()) {
        entity {
            it += Ai()
        }
    }
}

fun createDarkMonster(at: Vector2, radius: Float, numberOfPoints: Int, nodeRadius: Float) {
    val settings = inject<GameSettings>()

    with(fleks()) {
        entity {
            it += DarkMonster().apply {
                this.centerBody = createDarkEntity(at, nodeRadius)
                val angleShift = MathUtils.PI2 / numberOfPoints
                var currentAngle = 0f
                val theta = MathUtils.PI - angleShift / 2 - MathUtils.PI / 2
                val baseLength = 2 * radius * MathUtils.cos(theta)

                lateinit var previousBody: Body
                lateinit var currentBody: Body
                lateinit var firstBody: Body
                for (index in 0 until numberOfPoints) {
                    //1. Calculate point location using simple trigonometry
                    val x = at.x + radius * MathUtils.cos(currentAngle)
                    val y = at.y + radius * MathUtils.sin(currentAngle)
                    val vertex = vec2(x, y)

                    currentBody = createDarkEntity(vec2(at.x + vertex.x, at.y + vertex.y), nodeRadius)
                    this.outershell.add(currentBody)
                    this.allJoints.add(centerBody.distanceJointWith(currentBody) {
                        this.length = radius
                        this.frequencyHz = settings.spokeHz
                        this.dampingRatio = settings.spokeDamp
                        collideConnected = false
                    })
                    if (index == 0) {
                        firstBody = currentBody
                    }
                    if (index > 0) {
                        this.allSections.add(Triple(centerBody, previousBody, currentBody))
                        this.outerPairs.add(Pair(previousBody, currentBody))
                        this.allJoints.add(previousBody.distanceJointWith(currentBody) {
                            this.length = baseLength
                            this.frequencyHz = settings.outerShellHz
                            this.dampingRatio = settings.outerShellDamp
                            collideConnected = false
                        })
                    }
                    if (index == numberOfPoints - 1) {
                        this.allSections.add(Triple(centerBody, firstBody, currentBody))
                        this.outerPairs.add(Pair(currentBody, firstBody))
                        this.allJoints.add(firstBody.distanceJointWith(currentBody) {
                            this.length = baseLength
                            this.frequencyHz = settings.outerShellHz
                            this.dampingRatio = settings.outerShellDamp
                            collideConnected = false
                        })
                    }
                    previousBody = currentBody
                    currentAngle += angleShift
                }

                for (body in this.outershell) {
                    createDarkEntity(body)
                }
            }

            it += CameraFollow()
            it += Sprite(TextureRegion())
        }
    }
}

fun createDarkEntity(body: Body) {
    with(fleks()) {
        entity {
            it += Box2dBody(body)
            body.userData = it
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