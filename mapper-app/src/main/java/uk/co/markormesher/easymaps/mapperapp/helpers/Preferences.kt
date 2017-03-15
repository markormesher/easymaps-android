package uk.co.markormesher.easymaps.mapperapp.helpers

import android.content.Context
import uk.co.markormesher.easymaps.sdk.getLongPref
import uk.co.markormesher.easymaps.sdk.getStringPref
import uk.co.markormesher.easymaps.sdk.setLongPref
import uk.co.markormesher.easymaps.sdk.setStringPref

private val PREF_LATEST_LABELLING_VERSION = "pref.latest_labelling_version"
private val PREF_LATEST_DATA_PACK_VERSION = "pref.latest_data_pack_version"
private val PREF_LAST_DOWNLOAD_SUCCESS = "pref.last_download_success"
private val PREF_BLOOM_FILTER = "pref.bloom_filter"

fun Context.setLatestLabellingVersion(version: Long) = setLongPref(PREF_LATEST_LABELLING_VERSION, version)
fun Context.getLatestLabellingVersion() = getLongPref(PREF_LATEST_LABELLING_VERSION)

fun Context.setLatestDataPackVersion(version: Long) = setLongPref(PREF_LATEST_DATA_PACK_VERSION, version)
fun Context.getLatestDataPackVersion() = getLongPref(PREF_LATEST_DATA_PACK_VERSION)

fun Context.setLastDownloadSuccess() = setLongPref(PREF_LAST_DOWNLOAD_SUCCESS, System.currentTimeMillis())
fun Context.getLastDownloadSuccess() = getLongPref(PREF_LAST_DOWNLOAD_SUCCESS)

fun Context.setBloomFilterCache(filter: String) = setStringPref(PREF_BLOOM_FILTER, filter)
fun Context.getBloomFilterCache() = getStringPref(PREF_BLOOM_FILTER, "")
