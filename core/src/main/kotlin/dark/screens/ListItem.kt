package dark.screens

sealed class ListItem(val name: String, val path: String) {
    class SoundFile(name: String, path: String, var selected: Boolean = false): ListItem(name, path) {
    }

    class Directory(
        name: String,
        path: String,
        val parent: Directory?,
        val childDirs: MutableList<Directory> = mutableListOf(),
        val files: MutableList<SoundFile> = mutableListOf()
    ): ListItem(name, path)
    override fun toString(): String {
        return name
    }
}