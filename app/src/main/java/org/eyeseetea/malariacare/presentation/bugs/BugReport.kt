package org.eyeseetea.malariacare.presentation.bugs

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.eyeseetea.malariacare.utils.AUtils.getCommitHash

private const val gitHashKey: String = "gitHash"
private const val serverKey: String = "server"
private const val userKey: String = "user"
private const val logPrefix: String = "BugReport"

fun addGitHash(context: Context) {
    val commitHash = getCommitHash(context)
    FirebaseCrashlytics.getInstance().setCustomKey(gitHashKey, commitHash)
    Log.d("$logPrefix.$gitHashKey", commitHash)
}

fun addServerAndUser(server: String, user: String) {
    FirebaseCrashlytics.getInstance().setCustomKey(serverKey, server)
    FirebaseCrashlytics.getInstance().setCustomKey(userKey, user)
    Log.d("$logPrefix.$serverKey", server)
    Log.d("$logPrefix.$userKey", user)
}

fun removeServerAndUser() {
    FirebaseCrashlytics.getInstance().setCustomKey(serverKey, "")
    FirebaseCrashlytics.getInstance().setCustomKey(userKey, "")
    Log.d("$logPrefix.$serverKey", "")
    Log.d("$logPrefix.$userKey", "")
}
