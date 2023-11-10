plugins {
    kotlin("jvm") version "1.9.20"
}
dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(8)
}
tasks.test {
    useJUnitPlatform()
}
