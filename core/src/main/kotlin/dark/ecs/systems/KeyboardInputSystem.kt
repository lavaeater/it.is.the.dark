package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import dark.ecs.components.BodyControl
import dark.ecs.components.KeyboardAndMouseInput
import eater.ecs.ashley.components.Remove
import eater.ecs.ashley.components.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.math.vec2
import ktx.math.vec3

class KeyboardInputSystem(private val camera: OrthographicCamera) :
    IteratingSystem(
        allOf(
            BodyControl::class,
            KeyboardAndMouseInput::class,
            TransformComponent::class
        ).exclude(Remove::class).get()
    ) {

    private val mouseWorld3 = vec3()
        get() {
            field.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            camera.unproject(mouseWorld3)
            return field
        }
    val mouseWorld = vec2()
        get() {
            field.set(mouseWorld3.x, mouseWorld3.y)
            return field
        }

    val walkDirection = vec2()
        get() {
            field.x = if(Gdx.input.isKeyPressed(Keys.A)) -1f else if(Gdx.input.isKeyPressed(Keys.D)) 1f else 0f
            field.y = if(Gdx.input.isKeyPressed(Keys.W)) -1f else if(Gdx.input.isKeyPressed(Keys.S)) 1f else 0f
            return field.nor()
        }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val bodyControl = BodyControl.get(entity)
        val transform = TransformComponent.get(entity)
        bodyControl.aimDirection.set(mouseWorld).sub(transform.position).nor()
        bodyControl.moveDirection.set(walkDirection)
        bodyControl.currentForce = bodyControl.maxForce * walkDirection.len()

    }
}