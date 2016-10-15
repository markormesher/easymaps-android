package uk.co.markormesher.easymaps.sdk

import android.content.Context
import android.preference.PreferenceManager

private fun getPrefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
private fun getPrefsEditor(context: Context) = getPrefs(context).edit()

fun Context.getStringPref(key: String, default: String) = getPrefs(this).getString(key, default)
fun Context.setStringPref(key: String, value: String) = getPrefsEditor(this).putString(key, value).apply()

fun Context.getLongPref(key: String, default: Long = -1) = getPrefs(this).getLong(key, default)
fun Context.setLongPref(key: String, value: Long) = getPrefsEditor(this).putLong(key, value).apply()