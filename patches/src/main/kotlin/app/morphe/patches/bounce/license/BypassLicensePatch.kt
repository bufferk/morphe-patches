package app.bounce.patches.license

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.bounce.patches.shared.Constants.BOUNCE_COMPANION

private const val LICENSE_CLIENT = "Lcom/pairip/licensecheck/LicenseClient;"
private const val LICENSE_CHECK_STATE = "Lcom/pairip/licensecheck/LicenseClient\$LicenseCheckState;"

val bypassLicensePatch = bytecodePatch(
    name = "Bypass license check",
    description = "Bypasses the Google Play LVL backend license check, allowing full app access without activation.",
    default = true
) {
    compatibleWith(BOUNCE_COMPANION)

    execute {
        // Intercept the start of the license check method to manually set 
        // the client's verification state to OK, skipping Google Play LVL validation.
        InitializeLicenseCheckFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, $LICENSE_CHECK_STATE->LOCAL_CHECK_OK:$LICENSE_CHECK_STATE
                sput-object v0, $LICENSE_CLIENT->licenseCheckState:$LICENSE_CHECK_STATE
                return-void
            """
        )

        // Intercept javiersantos's PiracyChecker verification process and immediately
        // fire its internal 'allow()' callback interface to trick the app.
        PiracyCheckerVerifyFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lcom/github/javiersantos/piracychecker/PiracyChecker;->i:Lcom/github/javiersantos/piracychecker/PiracyChecker${'$'}callback${'$'}1;
                if-eqz v0, :cond_skip
                invoke-virtual {v0}, Lcom/github/javiersantos/piracychecker/PiracyChecker${'$'}callback${'$'}1;->allow()V
                :cond_skip
                return-void
            """
        )
    }
}
