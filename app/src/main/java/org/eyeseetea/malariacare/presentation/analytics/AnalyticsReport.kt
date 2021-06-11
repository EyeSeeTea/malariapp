package org.eyeseetea.malariacare.presentation.analytics

import android.content.Context
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

private const val serverKey: String = "server"
private const val logPrefix: String = "AnalyticsReport"

fun addServer(context: Context, server: String) {
    FirebaseAnalytics.getInstance(context).setUserProperty(serverKey, server)
    Log.d("$logPrefix.$serverKey", server)
}

fun removeServer(context: Context) {
    FirebaseAnalytics.getInstance(context).setUserProperty(serverKey, "")
    Log.d("$logPrefix.$serverKey", "")
}
