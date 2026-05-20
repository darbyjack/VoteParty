import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
// import net.kyori.indra.IndraExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("io.github.goooler.shadow") version "8.1.7" apply false
    id("com.github.ben-manes.versions") version "0.51.0"
    // id("net.kyori.indra") version "3.1.3"
    // id("net.kyori.indra.publishing") version "3.1.3"
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

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.github.cryptomorin:XSeries:11.1.0")
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

apply(plugin = "io.github.goooler.shadow")

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
    // spigot
    compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")

    // config
    implementation("ch.jalu:configme:1.3.0")

    // placeholderapi
    compileOnly("me.clip:placeholderapi:2.11.6")

    // NuVotifier hook
    compileOnly(files("libs/nuvotifier-2.7.3.jar"))

    // command handler
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    // bstats
    implementation("org.bstats:bstats-bukkit:3.0.2")

    // json stuff
    implementation("net.kyori:adventure-platform-bukkit:4.3.3")
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")

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