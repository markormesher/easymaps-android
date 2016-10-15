package uk.co.markormesher.easymaps.sdk

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun Context.copyToClipboard(label: String, text: String) {
	val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	clipboard.primaryClip = ClipData.newPlainText(label, text)
}