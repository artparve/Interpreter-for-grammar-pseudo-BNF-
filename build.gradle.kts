plugins {
    kotlin("jvm") version "1.9.20"
}
dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(8)
}