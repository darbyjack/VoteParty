group = "me.clip"
version = "2.0"

dependencies {
    implementation(project(":particle-api"))

    implementation(libs.kotlin.stdlib.jdk8)
    compileOnly(libs.spigot.modern)
    compileOnly(libs.xseries)
}