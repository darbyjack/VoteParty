pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
	id("io.github.ben-manes.versions.settings") version "0.65.0"
}

rootProject.name = "VP"

include("particle-api")
include("particle-legacy")
include("particle-modern")
