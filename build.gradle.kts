buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24" apply false
}

// 移除 allprojects 中的 repositories，因为 settings.gradle.kts 中设置了 FAIL_ON_PROJECT_REPOS
// allprojects { ... }

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}