import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	java
	kotlin("jvm") version "1.3.61"
}

group = "me.clip"
version = "2.0"

repositories {
	jcenter()
	maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	compileOnly("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT")
}


configure<JavaPluginConvention> {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().configureEach {
	kotlinOptions.jvmTarget = "1.8"
}