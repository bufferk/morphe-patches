package app.bounce.patches.at4k

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

private const val O3_CLASS = "LO3/O3;"
private const val MAIN_ACTIVITY_CLASS = "Lcom/overdevs/at4k/MainActivity;"

/**
 * Fingerprint for O3.j(boolean) — the central premium status setter.
 * Unique identifier: "Premium status updated to " log string.
 */
object SetPremiumStatusFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Z"),

    filters = listOf(
        string("Premium status updated to "),
        string("is_premium"),
    ),

    custom = { _, classDef ->
        classDef.type == O3_CLASS
    }
)

/**
 * Fingerprint for MainActivity.onCreate — where is_premium is read from SharedPreferences
 * on startup to initialise the in-memory premium LiveData.
 */
object MainActivityOnCreateFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),

    filters = listOf(
        string("launcher_prefs"),
        string("is_premium"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),

    custom = { _, classDef ->
        classDef.type == MAIN_ACTIVITY_CLASS
    }
)
