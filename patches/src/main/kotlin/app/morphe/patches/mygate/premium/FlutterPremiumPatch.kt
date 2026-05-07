package app.morphe.patches.mygate.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.Fingerprint
import app.morphe.patches.mygate.shared.Constants.COMPATIBILITY_MYGATE

internal object UserProfileEngineBFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/userprofile/engine/UserProfileEngine;",
    name = "b",
    returnType = "V",
    parameters = listOf(
        "Lcom/mygate/user/modules/userprofile/engine/UserProfileEngine;",
        "Lorg/json/JSONObject;",
        "Z"
    )
)

val flutterPremiumPatch = bytecodePatch(
    name = "Flutter Premium Bypass",
    description = "Forces premium status inside the JSON payload sent to Flutter",
    default = true
) {
    compatibleWith(COMPATIBILITY_MYGATE)

    execute {
        UserProfileEngineBFingerprint.method.addInstructions(
            0,
            """
            new-instance v0, Lorg/json/JSONObject;
            invoke-direct {v0}, Lorg/json/JSONObject;-><init>()V
            const-string v1, "status"
            const-string v2, "ACTIVE"
            invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            
            const-string v1, "user_plan"
            invoke-virtual {p1, v1, v0}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            
            new-instance v0, Lorg/json/JSONObject;
            invoke-direct {v0}, Lorg/json/JSONObject;-><init>()V
            const-string v1, "status"
            const-string v2, "ACTIVE"
            invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            const-string v1, "sort_index"
            const/4 v2, 0x3
            invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;
            
            const-string v1, "society_highest_plan"
            invoke-virtual {p1, v1, v0}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            """
        )
    }
}
