plugins {
    id("java")
}

val modVersion = property("version")!!.toString()
private val minecraftVersion = findProperty("minecraft_version")?.toString()
private val buildType = findProperty("build.type")?.let { BuildType.valueOf(it.toString().uppercase()) } ?: BuildType.LOCAL

group = property("group")!!
version = createVersionString()

private fun createVersionString(): String {
    val builder = StringBuilder()

    builder.append(modVersion)

    if (buildType != BuildType.RELEASE) {
        builder.append('-').append(buildType.name.lowercase())

        if (buildType != BuildType.LOCAL) {
            builder.append('.').append(findProperty("build.number")?.toString() ?: "0")
        }
    }

    if (minecraftVersion != null) {
        builder.append('+').append("mc").append(minecraftVersion)
    }

    return builder.toString()
}

private fun createFileVersionString(): String {
    val builder = StringBuilder()

    builder.append(modVersion)

    if (buildType != BuildType.RELEASE) {
        builder.append('-').append(buildType.name.lowercase())

        if (buildType != BuildType.LOCAL) {
            builder.append('.').append(findProperty("build.number")?.toString() ?: "0")
        }
    }

    if (minecraftVersion != null) {
        builder.append("-mc").append(minecraftVersion)
    }

    return builder.toString()
}

private enum class BuildType {
    RELEASE,
    BETA,
    ALPHA,
    LOCAL,
}

tasks {
    getByName<Jar>("jar") {
        from("../LICENSE")
        archiveVersion = createFileVersionString()
    }
}
