import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dark.core.GameSettings
import eater.core.world
import eater.injection.InjectionContext.Companion.inject
import ktx.box2d.body
import ktx.box2d.circle

fun createBlob(at:Vector2, settings: GameSettings = inject()) {


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