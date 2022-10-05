package dark.ecs.components

sealed class Prop(val name: String) {
    data class Health(var current: Float = 100f, val min: Float = 0f, val max: Float = 100f) : Prop("Health")
}