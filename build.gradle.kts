val invoker: Configuration by configurations.creating

plugins {
    kotlin("jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "org.gcp.cloud-function"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.cloud.functions:functions-framework-api")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.google.cloud.functions:functions-framework-api:1.0.1")
    invoker("com.google.cloud.functions.invoker:java-function-invoker:1.2.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

task<JavaExec>("runFunction") {
    mainClass.set("com.google.cloud.functions.invoker.runner.Invoker")
    classpath(invoker)
    inputs.files(configurations.runtimeClasspath, sourceSets["main"].output)
    args(
        "--target", project.findProperty("runFunction.target") ?: "CloudFunction",
        "--port", project.findProperty("runFunction.port") ?: 8080
    )
    doFirst {
        args("--classpath", files(configurations.runtimeClasspath, sourceSets["main"].output).asPath)
    }
}

tasks.register<Zip>("packageDistribution") {
    archiveFileName.set("${rootProject.name}-${rootProject.version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("dist"))

    from(layout.buildDirectory.file("libs/${rootProject.name}-${rootProject.version}-all.jar"))
}

tasks.register("shadowJarAndPackageDistribution") {
    dependsOn("shadowJar")
    dependsOn("packageDistribution")

    tasks.findByName("packageDistribution")?.mustRunAfter("shadowJar")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("CloudFunctionKt")
}