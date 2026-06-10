plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

group = "me.bill"
    version = "1.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":fpp-aichat"))
    implementation(project(":fpp-chat"))
    implementation(project(":fpp-luckperms"))
    implementation(project(":fpp-pathfinder"))
    implementation(project(":fpp-ping"))
    implementation(project(":fpp-skin"))
    implementation(project(":fpp-swap"))
    implementation(project(":fpp-waypoints"))
}

tasks.shadowJar {
    archiveBaseName.set("fpp-spoof")
    archiveVersion.set(project.version.toString())
    
    dependsOn(
        ":fpp-aichat:jar",
        ":fpp-chat:jar",
        ":fpp-luckperms:jar",
        ":fpp-pathfinder:jar",
        ":fpp-ping:jar",
        ":fpp-skin:jar",
        ":fpp-swap:jar",
        ":fpp-waypoints:jar"
    )
}

tasks.register<Copy>("copySpoof") {
    from(tasks.shadowJar)
    into("../../builds")
}

tasks.build {
    finalizedBy("copySpoof")
}
