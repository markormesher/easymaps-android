package uk.co.markormesher.easymaps.scannerapp

import android.content.Context
import uk.co.markormesher.easymaps.sdk.*

private val PREF_LAST_UPLOAD = "pref.last_upload"
private val PREF_LAST_UPLOAD_CHECK = "pref.last_upload_check"
private val PREF_HIGH_FREQ_MODE = "pref.high_freq_mode"
private val PREF_SUPER_USER = "pref.super_user"

fun Context.setLastUploadCheckTime() = setStringPref(PREF_LAST_UPLOAD_CHECK, getDateString())

fun Context.getLastUploadCheckTime() : String = getStringPref(PREF_LAST_UPLOAD_CHECK, "never")

fun Context.setLastUploadTime() = setStringPref(PREF_LAST_UPLOAD, getDateString())

fun Context.getLastUploadTime() : String = getStringPref(PREF_LAST_UPLOAD, "never")

fun Context.setIsSuperUser(enabled: Boolean) = setBooleanPref(PREF_SUPER_USER, enabled)

fun Context.isSuperUser() : Boolean = getBooleanPref(PREF_SUPER_USER, false)

fun Context.setIsHighFrequencyMode(enabled: Boolean) = setBooleanPref(PREF_HIGH_FREQ_MODE, enabled)

fun Context.isHighFrequencyMode() : Boolean = isSuperUser() && getBooleanPref(PREF_HIGH_FREQ_MODE, false)
