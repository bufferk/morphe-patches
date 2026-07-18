group = "app.bounce"

patches {
    about {
        name = "bufferk patches"
        description = "bufferk patches"
        author = "bufferk"
        website = "https://github.com/bufferk/morphe-patches"
        source = "git@github.com:bufferk/morphe-patches"
        contact = ""
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    // Used by JsonGenerator.
    implementation(libs.gson)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }
    register<JavaExec>("runPatchRunner") {
        description = "Run patcher locally on MyGate APK"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("util.PatchRunnerKt")
    }
    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}
