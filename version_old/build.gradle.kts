group = "me.clip"
version = "2.0"

repositories {
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":version"))

    implementation(libs.kotlin.stdlib.jdk8)
	implementation(libs.particleapi)

    compileOnly(libs.spigot.legacy)
}