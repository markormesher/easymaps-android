package uk.co.markormesher.easymaps.sdk

import android.content.Context
import android.preference.PreferenceManager

private fun getPrefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
private fun getPrefsEditor(context: Context) = getPrefs(context).edit()

fun Context.getStringPref(key: String, default: String) : String = getPrefs(this).getString(key, default)
fun Context.setStringPref(key: String, value: String) = getPrefsEditor(this).putString(key, value).apply()

fun Context.getBooleanPref(key: String, default: Boolean) : Boolean = getPrefs(this).getBoolean(key, default)
fun Context.setBooleanPref(key: String, value: Boolean) = getPrefsEditor(this).putBoolean(key, value).apply()

fun Context.getLongPref(key: String, default: Long = -1) : Long = getPrefs(this).getLong(key, default)
fun Context.setLongPref(key: String, value: Long) = getPrefsEditor(this).putLong(key, value).apply()