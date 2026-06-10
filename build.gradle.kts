import org.gradle.jvm.tasks.Jar

plugins {
    base
}

val workspaceBuildsDir = rootProject.file("../builds")

val extensionProjects = listOf(
    "fpp-aichat",
    "fpp-chat",
    "fpp-luckperms",
    "fpp-pathfinder",
    "fpp-ping",
    "fpp-skin",
    "fpp-swap",
    "fpp-waypoints",
)

subprojects {
    apply(plugin = "java")

    group = "me.bill.fpp.extensions"
    version = "1.1.1"

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }

    dependencies {
        "compileOnly"("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
        "compileOnly"("net.luckperms:api:5.5")
        "compileOnly"("com.google.code.gson:gson:2.11.0")
        "compileOnly"(files(rootProject.file("../fake-player-plugin/build/fpp.jar")))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<Jar>().configureEach {
        archiveBaseName.set(project.name)
        archiveVersion.set("")
    }

    afterEvaluate {
        tasks.withType<Copy>().configureEach {
            if (name == "copyExtension" || name == "copySpoof") {
                into(workspaceBuildsDir)
            }
        }

        val copyTask = if (tasks.findByName("copyExtension") == null) {
            tasks.register<Copy>("copyExtension") {
                dependsOn(tasks.named("jar"))
                from(tasks.named("jar"))
                into(workspaceBuildsDir)
            }
        } else {
            tasks.named<Copy>("copyExtension") {
                dependsOn(tasks.named("jar"))
                into(workspaceBuildsDir)
            }
        }

        listOf("compileJava", "jar", "assemble", "build").forEach { taskName ->
            tasks.findByName(taskName)?.let {
                tasks.named(taskName) {
                    finalizedBy(copyTask)
                }
            }
        }
    }

}

val extensionJarTasks = extensionProjects.map { project(":$it").tasks.named<Jar>("jar") }
val bundledJarPaths = extensionProjects.joinToString(",") { "extensions/$it.jar" }

val bundleExtensions by tasks.registering(Jar::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Packages all first-party FPP extensions into one bundle jar."
    archiveBaseName.set("fpp-spoof")
    archiveVersion.set("")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
    dependsOn(extensionJarTasks)

    manifest {
        attributes(
            "FPP-Extension-Bundle" to "true",
            "FPP-Extension-Jars" to bundledJarPaths,
        )
    }

    extensionJarTasks.forEach { jarTask ->
        from(jarTask.map { it.archiveFile }) {
            into("extensions")
            rename { "${jarTask.get().project.name}.jar" }
        }
    }
}

val copyExtensionBundle by tasks.registering(Copy::class) {
    dependsOn(bundleExtensions)
    from(bundleExtensions)
    into(workspaceBuildsDir)
}

tasks.named("build") {
    dependsOn(subprojects.map { it.tasks.named("build") })
    finalizedBy(copyExtensionBundle)
}
