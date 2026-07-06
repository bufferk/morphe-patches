package util

import java.io.File

fun main() {
    val inputApk = File("../mygate_extracted/com.mygate.user.apk")
    val outputApk = File("../mygate_patched.apk")
    val cacheDir = "build/tmp/patch_cache"
    val tmpDir = "build/tmp/patch_tmp"
    
    File(cacheDir).mkdirs()
    File(tmpDir).mkdirs()
    
    println("Constructing PatcherConfig...")
    val cpuArchClass = Class.forName("app.morphe.patcher.resource.CpuArchitecture")
    val arm64Val = cpuArchClass.getField("ARM64_V8A").get(null)
    
    val configClass = Class.forName("app.morphe.patcher.PatcherConfig")
    val configConstructor = configClass.getConstructor(
        File::class.java, File::class.java, String::class.java, String::class.java, Boolean::class.javaPrimitiveType, Set::class.java
    )
    val config = configConstructor.newInstance(
        inputApk, outputApk, cacheDir, tmpDir, false, setOf(arm64Val)
    )
    
    val setResourceModeMethod = configClass.declaredMethods.first { it.name.startsWith("setResourceMode") }
    val resourceModeClass = Class.forName("app.morphe.patcher.resource.ResourceMode")
    val noneVal = resourceModeClass.getField("NONE").get(null)
    setResourceModeMethod.isAccessible = true
    setResourceModeMethod.invoke(config, noneVal)
    
    println("Constructing PackageMetadata...")
    val metadataClass = Class.forName("app.morphe.patcher.PackageMetadata")
    val metadataConstructor = metadataClass.getDeclaredConstructor(String::class.java, String::class.java, String::class.java)
    metadataConstructor.isAccessible = true
    val packageMetadata = metadataConstructor.newInstance("com.mygate.user", "7.30.1", "1")
    
    println("Constructing BytecodePatchContext...")
    val contextClass = Class.forName("app.morphe.patcher.patch.BytecodePatchContext")
    val contextConstructor = contextClass.getDeclaredConstructor(configClass, metadataClass)
    contextConstructor.isAccessible = true
    val context = contextConstructor.newInstance(config, packageMetadata)
    
    println("Testing all Premium fingerprints...")
    val fingerprintClass = Class.forName("app.morphe.patcher.Fingerprint")
    val matchMethod = fingerprintClass.getMethod("match", contextClass)
    
    val matchClass = Class.forName("app.morphe.patcher.Match")
    val getMethodMethod = matchClass.getMethod("getMethod")
    
    val methodClass = Class.forName("app.morphe.patcher.util.proxy.mutableTypes.MutableMethod")
    val getMethodNameMethod = methodClass.getMethod("getName")
    val getDefiningClassMethod = methodClass.getMethod("getDefiningClass")
    
    val fingerprints = listOf(
        "IsPremiumUserFingerprint",
        "ShowUpgradeDialogFingerprint",
        "CommonUtilityNotificationIntentFingerprint",
        "ReadPrefTokenNotFoundFingerprint",
        "AppNotificationSettingsGetEintercomFingerprint",
        "NotificationSettingsGetNotifyFingerprint",
        "TroubleshootingSettingsSuccessFingerprint",
        "TroubleshootingSettingsFailureFingerprint",
        "TroubleshootingAppSettingsSuccessFingerprint",
        "TroubleshootingAppSettingsFailureFingerprint"
    )
    
    for (name in fingerprints) {
        try {
            val fpClass = Class.forName("app.morphe.patches.mygate.premium.$name")
            val instance = fpClass.getField("INSTANCE").get(null)
            val match = matchMethod.invoke(instance, context)
            val method = getMethodMethod.invoke(match)
            println("Fingerprint $name: MATCHED -> ${getDefiningClassMethod.invoke(method)}.${getMethodNameMethod.invoke(method)}")
        } catch (e: Exception) {
            println("Fingerprint $name: FAILED -> ${e.cause?.message ?: e.message}")
        }
    }
}
