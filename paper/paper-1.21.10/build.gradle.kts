plugins {
    id("spectatorplus.platform")
    id("io.github.goooler.shadow")
    id("xyz.jpenilla.run-paper")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${property("paper_version")}")
    implementation(project(":paper:paper-core"))
}

tasks {
    shadowJar {
       // logic to shade core
    }
}
