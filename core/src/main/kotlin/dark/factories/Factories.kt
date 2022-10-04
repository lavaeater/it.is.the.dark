import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dark.core.GameSettings
import eater.injection.InjectionContext.Companion.inject
import ktx.box2d.distanceJointWith
import ktx.math.vec2

fun createSlime(at: Vector2, radius: Float, numberOfPoints: Int, nodeRadius:Float) {
    val settings = inject<GameSettings>()
    /**
     * Let's assume a cirlce with 12 sections, to keep it
     * reasonable.
     * x = cx + r * cos(a)
     * y = cy + r * sin(a)
     */
    val slimer = SlimerComponent()
    val angleShift = MathUtils.PI2 / numberOfPoints
    var currentAngle = 0f
    val theta = MathUtils.PI - angleShift / 2 - MathUtils.PI / 2
    val baseLength = 2 * radius * MathUtils.cos(theta)
    /**
     * 2b ⋅ cosθ
     */

    val centerBody = createSlimeNode(at, nodeRadius)
    slimer.centerBody = centerBody
    lateinit var previousBody: Body
    lateinit var currentBody: Body
    lateinit var firstBody: Body
    for (index in 0 until numberOfPoints) {
        //1. Calculate point location using simple trigonometry
        val x = at.x + radius * MathUtils.cos(currentAngle)
        val y = at.y + radius * MathUtils.sin(currentAngle)
        val vertex = vec2(x, y)

        currentBody = createSlimeNode(vec2(at.x + vertex.x, at.y + vertex.y), nodeRadius)
        slimer.outershell.add(currentBody)
        slimer.allJoints.add(centerBody.distanceJointWith(currentBody) {
            this.length = radius
            this.frequencyHz = settings.spokeHz
            this.dampingRatio = settings.spokeDamp
            collideConnected = false
        })
        if(index == 0) {
            firstBody = currentBody
        }
        if(index > 0) {
            slimer.allSections.add(Triple(centerBody, previousBody, currentBody))
            slimer.outerPairs.add(Pair(previousBody, currentBody))
            slimer.allJoints.add(previousBody.distanceJointWith(currentBody) {
                this.length = baseLength
                this.frequencyHz = settings.outerShellHz
                this.dampingRatio = settings.outerShellDamp
                collideConnected = false
            })
        }
        if(index == numberOfPoints - 1) {
            slimer.allSections.add(Triple(centerBody, firstBody, currentBody))
            slimer.outerPairs.add(Pair(currentBody, firstBody))
            slimer.allJoints.add(firstBody.distanceJointWith(currentBody) {
                this.length = baseLength
                this.frequencyHz = settings.outerShellHz
                this.dampingRatio = settings.outerShellDamp
                collideConnected = false
            })
        }
        previousBody = currentBody
        currentAngle += angleShift
    }

    var entity = engine.createEntity()
    entity.add(slimer)
    entity.add(CameraFollowComponent())
    entity.add(SpriteComponent().apply { sprite = Assets.dummySprite })
    entity.add(BodyComponent().apply { body = centerBody })
    engine.addEntity(entity)
    centerBody.userData = entity

    for(body in slimer.outershell) {
        createSlimeEntity(body)
    }
}

fun createSlimeEntity(body: Body) : Entity {
    val entity = engine.entity {
        with<BodyComponent> {
            this.body = body
        }
    }
    body.userData = entity
    return entity
}

fun createSlimeNode(at: Vector2, radius: Float) : Body {
    return world.body {
        type = BodyDef.BodyType.DynamicBody
        position.set(at)
        fixedRotation = false
        circle(radius) {
            density = .1f
        }
    }
}