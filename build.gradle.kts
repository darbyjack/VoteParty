import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
// import net.kyori.indra.IndraExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.versions)
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    base {
        archivesName.set("VoteParty")
    }

    group = "me.clip"
    version = "2.40-SNAPSHOT"

    repositories {
        mavenCentral()

        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.glaremasters.me/repository/public/")
    }

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-parameters")
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()

        if (JavaVersion.current().isJava9Compatible) {
            options.release.set(8)
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
        }
    }
}

apply(plugin = "com.gradleup.shadow")

tasks.named<ShadowJar>("shadowJar") {
    minimize()

    relocate("co.aikar.commands", "me.clip.voteparty.libs.acf")
    relocate("co.aikar.locales", "me.clip.voteparty.libs.locales")
    relocate("ch.jalu.configme", "me.clip.voteparty.libs.configme")
    relocate("org.inventivetalent", "me.clip.voteparty.libs.inventivetalent")
    relocate("net.kyori", "me.clip.voteparty.libs.kyori")
    relocate("com.cryptomorin.xseries", "me.clip.voteparty.libs.xseries")
    relocate("kotlin", "me.clip.voteparty.libs.kotlin")
    relocate("org.bstats", "me.clip.voteparty.libs.bstats")

    archiveFileName.set("VoteParty-${project.version}.jar")
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.xseries)

    compileOnly(libs.spigot.modern)

    implementation(libs.configme)

    compileOnly(libs.placeholderapi)
    compileOnly(files("libs/nuvotifier-2.7.3.jar"))

    implementation(libs.acf.paper)
    implementation(libs.bstats.bukkit)

    implementation(libs.adventure.platform.bukkit)
    implementation(libs.adventure.api)
    implementation(libs.adventure.text.minimessage)

    implementation(project(":version"))
    implementation(project(":version_old"))
    implementation(project(":version_new"))
}

tasks.processResources {
    filter<ReplaceTokens>(
        "tokens" to mapOf(
            "version" to project.version.toString()
        )
    )
}