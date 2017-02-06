package uk.co.markormesher.easymaps.mapperapp.helpers

import android.content.Context
import uk.co.markormesher.easymaps.sdk.getLongPref
import uk.co.markormesher.easymaps.sdk.setLongPref

private val PREF_LATEST_LABELLING_VERSION = "pref.latest_labelling_version"
private val PREF_LATEST_DATA_PACK_VERSION = "pref.latest_data_pack_version"
private val PREF_LAST_DOWNLOAD_SUCCESS = "pref.last_download_success"

fun Context.setLatestLabellingVersion(version: Long) = setLongPref(PREF_LATEST_LABELLING_VERSION, version)
fun Context.getLatestLabellingVersion() = getLongPref(PREF_LATEST_LABELLING_VERSION)

fun Context.setLatestDataPackVersion(version: Long) = setLongPref(PREF_LATEST_DATA_PACK_VERSION, version)
fun Context.getLatestDataPackVersion() = getLongPref(PREF_LATEST_DATA_PACK_VERSION)

fun Context.setLastDownloadSuccess() = setLongPref(PREF_LAST_DOWNLOAD_SUCCESS, System.currentTimeMillis())
fun Context.getLastDownloadSuccess() = getLongPref(PREF_LAST_DOWNLOAD_SUCCESS)

