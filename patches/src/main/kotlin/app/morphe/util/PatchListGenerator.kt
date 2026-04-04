package app.bounce.util

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.util.jar.JarFile

/**
 * Scans the compiled patch classes for all bytecodePatch val declarations
 * and outputs them as JSON to patches-list.json.
 *
 * Run via: ./gradlew generatePatchesList
 */
data class PatchInfo(
    val name: String,
    val description: String,
    val default: Boolean = true
)

/**
 * Reflectively finds all BytecodePatch instances by scanning loaded classes.
 * Looks for static fields of type matching the patcher's BytecodePatch class.
 */
fun main() {
    // Find the BytecodePatch class from the patcher library
    val bytecodePatchClass = try {
        Class.forName("app.morphe.patcher.patch.BytecodePatch")
    } catch (e: ClassNotFoundException) {
        println("ERROR: BytecodePatch class not found on classpath.")
        println("Make sure the task dependsOn(build) so the patcher library is available.")
        System.exit(1)
        return
    }

    val patches = mutableListOf<PatchInfo>()

    // Scan all classes that could contain bytecodePatch vals.
    // Top-level vals in Kotlin become static fields on a class named "<FileName>Kt".
    // We scan from the app.bounce.patches package recursively.
    val classLoader = Thread.currentThread().contextClassLoader
    val prefix = "app/bounce/patches/"

    val classFileList = mutableListOf<java.net.URL>()

    // Collect URLs from the classloader for scanning
    fun collectUrls(urls: Array<java.net.URL>) {
        urls.forEach { url ->
            classFileList.add(url)
        }
    }

    // Walk through the classloader URLs
    when {
        classLoader is java.net.URLClassLoader -> {
            collectUrls(classLoader.urLs)
        }
        else -> {
            // For Gradle's AppClassLoader, try the java.class.path
            System.getProperty("java.class.path")?.split(File.pathSeparatorChar)?.forEach { path ->
                try {
                    classFileList.add(File(path).toURI().toURL())
                } catch (_: Exception) {}
            }
        }
    }

    // Scan each URL for classes
    val foundClasses = mutableSetOf<Class<*>>()

    classFileList.forEach { url ->
        try {
            if (url.protocol == "file") {
                val file = File(url.toURI())
                when {
                    file.isDirectory -> {
                        scanDirectory(file, foundClasses, prefix)
                    }
                    file.name.endsWith(".jar") || file.name.endsWith(".class") -> {
                        if (file.name.endsWith(".jar")) {
                            scanJar(file, foundClasses, prefix)
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    // Check each found class for BytecodePatch static fields
    foundClasses.forEach { clazz ->
        try {
            clazz.fields.forEach { field ->
                if (
                    Modifier.isStatic(field.modifiers) &&
                    bytecodePatchClass.isAssignableFrom(field.type)
                ) {
                    val patch = field.get(null)
                    try {
                        val nameField = patch.javaClass.getDeclaredField("name")
                        nameField.isAccessible = true
                        val descField = patch.javaClass.getDeclaredField("description")
                        descField.isAccessible = true
                        val defaultField = patch.javaClass.getDeclaredField("default")
                        defaultField.isAccessible = true

                        patches.add(
                            PatchInfo(
                                name = nameField.get(patch) as String,
                                description = descField.get(patch) as String,
                                default = defaultField.get(patch) as Boolean
                            )
                        )
                    } catch (e: NoSuchFieldException) {
                        // Try with Kotlin property access
                        try {
                            val getName = patch.javaClass.getMethod("getName")
                            val getDesc = patch.javaClass.getMethod("getDescription")
                            val getDefault = patch.javaClass.getMethod("isDefault")

                            patches.add(
                                PatchInfo(
                                    name = getName.invoke(patch) as String,
                                    description = getDesc.invoke(patch) as String,
                                    default = getDefault.invoke(patch) as Boolean
                                )
                            )
                        } catch (_: Exception) {
                            patches.add(PatchInfo(name = field.name, description = "(unknown)"))
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    // Output results
    val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
    val json = gson.toJson(patches)

    // Write to patches-list.json in the project root
    val projectDir = System.getProperty("user.dir")
        ?: System.getProperty("java.io.tmpdir")
    var outputFile = File(projectDir, "patches/patches-list.json")

    // If that doesn't work, try the working directory directly
    if (!outputFile.parentFile.exists()) {
        // Try writing to current working directory
        outputFile = File(projectDir, "patches-list.json")
    }

    outputFile.parentFile.mkdirs()
    outputFile.writeText(json + "\n")
    println("Wrote ${patches.size} patches to ${outputFile.absolutePath}")
    println(json)
}

fun scanDirectory(dir: File, foundClasses: MutableSet<Class<*>>, prefix: String) {
    dir.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            scanDirectory(file, foundClasses, prefix)
        } else if (file.name.endsWith(".class")) {
            val relPath = file.relativeTo(dir).path.replace(File.separatorChar, '/')
            if (relPath.startsWith(prefix) || relPath.contains("/patches/")) {
                val className = relPath.removeSuffix(".class").replace(File.separatorChar, '.')
                try {
                    foundClasses.add(Class.forName(className))
                } catch (_: Exception) {}
            }
        }
    }
}

fun scanJar(jarFile: File, foundClasses: MutableSet<Class<*>>, prefix: String) {
    try {
        JarFile(jarFile).use { jar ->
            jar.entries().asSequence().forEach { entry ->
                if (entry.name.endsWith(".class") &&
                    (entry.name.startsWith(prefix) || entry.name.contains("/patches/"))
                ) {
                    val className = entry.name.removeSuffix(".class").replace('/', '.')
                    try {
                        foundClasses.add(Class.forName(className))
                    } catch (_: Exception) {}
                }
            }
        }
    } catch (_: Exception) {}
}
