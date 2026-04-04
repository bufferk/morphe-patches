package app.bounce.patches.license

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

private const val LICENSE_CLIENT_CLASS = "Lcom/pairip/licensecheck/LicenseClient;"

object InitializeLicenseCheckFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PRIVATE),
    returnType = "V",
    parameters = listOf(),

    filters = listOf(
        string("Connecting to the licensing service..."),

        methodCall(
            definingClass = "Landroid/util/Log;",
            name = "d",
        ),
    ),

    custom = { _, classDef ->
        classDef.type == LICENSE_CLIENT_CLASS
    }
)

object PiracyCheckerVerifyFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),

    custom = { method, classDef ->
        classDef.type == "Lcom/github/javiersantos/piracychecker/PiracyChecker;" &&
            method.name == "d"
    }
)
