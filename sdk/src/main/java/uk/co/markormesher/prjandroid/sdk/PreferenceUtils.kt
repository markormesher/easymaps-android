package uk.co.markormesher.prjandroid.sdk

import android.content.Context
import android.preference.PreferenceManager

private fun getPrefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
private fun getPrefsEditor(context: Context) = getPrefs(context).edit()

fun Context.getLongPref(key: String, default: Long = -1) = getPrefs(this).getLong(key, default)
fun Context.setLongPref(key: String, value: Long) = getPrefsEditor(this).putLong(key, value).apply()