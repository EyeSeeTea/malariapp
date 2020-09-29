package org.eyeseetea.malariacare.data.database.utils.monitor

import android.util.Log
import android.webkit.WebView
import org.eyeseetea.malariacare.domain.entity.ServerClassification

fun invokeInitMessages(webView: WebView, json: String) {
    val command = "initMessages($json)"
    invokeJsMethod(webView, command)
}

fun invokeSetServerClassification(webView: WebView, serverClassification: ServerClassification) {
    val command = "setServerClassification(${serverClassification.code})"
    invokeJsMethod(webView, command)
}

fun invokeSetClassificationContext(webView: WebView, classificationCtxJson: String) {
    val command = "setClassificationContext($classificationCtxJson)"
    invokeJsMethod(webView, command)
}

fun invokeUpdateOrgUnitFilter(webView: WebView, selectedOrgUnitFilter: String) {
    val command = "updateOrgUnitFilter('$selectedOrgUnitFilter')"
    invokeJsMethod(webView, command)
}

fun invokeUpdateProgramFilter(webView: WebView, selectedProgramFilter: String) {
    val command = "updateProgramFilter('$selectedProgramFilter')"
    invokeJsMethod(webView, command)
}

fun invokeSetOrgUnitPieData(webView: WebView, dataJson: String) {
    val command = "setOrgUnitPieData($dataJson)"
    invokeJsMethod(webView, command)
}

fun invokeSetProgramPieData(webView: WebView, dataJson: String) {
    val command = "setProgramPieData($dataJson)"
    invokeJsMethod(webView, command)
}

fun invokeSetDataTablesPerOrgUnit(webView: WebView, orgUnit: String, dataJson: String) {
    val command = "setDataTablesPerOrgUnit('$orgUnit',$dataJson)"
    invokeJsMethod(webView, command)
}

fun invokeSetDataTablesPerProgram(webView: WebView, program: String, dataJson: String) {
    val command = "setDataTablesPerProgram('$program',$dataJson)"
    invokeJsMethod(webView, command)
}

private fun invokeJsMethod(webView: WebView, command: String) {
    val finalCommand = "javascript:$command"
    Log.d("JavascriptInvoker", finalCommand)
    webView.loadUrl(finalCommand)
}