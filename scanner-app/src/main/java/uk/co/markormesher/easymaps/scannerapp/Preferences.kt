package uk.co.markormesher.easymaps.scannerapp

import android.content.Context
import uk.co.markormesher.easymaps.sdk.*

private val PREF_LAST_UPLOAD = "pref.last_upload"
private val PREF_LAST_UPLOAD_CHECK = "pref.last_upload_check"
private val PREF_SCAN_INTERVAL = "pref.scan_interval"
private val PREF_SUPER_USER = "pref.super_user"
private val PREF_NETWORK = "pref.network"

fun Context.setLastUploadCheckTime() = setStringPref(PREF_LAST_UPLOAD_CHECK, getDateString())

fun Context.getLastUploadCheckTime() = getStringPref(PREF_LAST_UPLOAD_CHECK, "never")

fun Context.setLastUploadTime() = setStringPref(PREF_LAST_UPLOAD, getDateString())

fun Context.getLastUploadTime() = getStringPref(PREF_LAST_UPLOAD, "never")

fun Context.setIsSuperUser(enabled: Boolean) = setBooleanPref(PREF_SUPER_USER, enabled)

fun Context.isSuperUser() = getBooleanPref(PREF_SUPER_USER, false)

fun Context.setScanIntervalOption(intervalOption: Int) = setIntPref(PREF_SCAN_INTERVAL, intervalOption)

fun Context.getScanIntervalOption() = getIntPref(PREF_SCAN_INTERVAL, DEFAULT_SCAN_INTERVAL_OPTION)

fun Context.setNetwork(network: String) = setStringPref(PREF_NETWORK, network)

fun Context.getNetwork() = getStringPref(PREF_NETWORK, NO_NETWORK)
