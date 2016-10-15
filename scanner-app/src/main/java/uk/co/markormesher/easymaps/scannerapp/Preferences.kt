package uk.co.markormesher.easymaps.scannerapp

import android.content.Context
import uk.co.markormesher.easymaps.sdk.getDateString
import uk.co.markormesher.easymaps.sdk.getStringPref
import uk.co.markormesher.easymaps.sdk.setStringPref

private val PREF_LAST_UPLOAD = "pref.last_upload"
private val PREF_LAST_UPLOAD_CHECK = "pref.last_upload_check"

fun Context.setLastUploadCheckTime() = setStringPref(PREF_LAST_UPLOAD_CHECK, getDateString())

fun Context.getLastUploadCheckTime() : String = getStringPref(PREF_LAST_UPLOAD_CHECK, "never")

fun Context.setLastUploadTime() = setStringPref(PREF_LAST_UPLOAD, getDateString())

fun Context.getLastUploadTime() : String = getStringPref(PREF_LAST_UPLOAD, "never")