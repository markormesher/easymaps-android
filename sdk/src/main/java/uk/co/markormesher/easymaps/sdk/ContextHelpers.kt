package uk.co.markormesher.easymaps.sdk

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

fun Context.copyToClipboard(label: String, text: String) {
	val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	clipboard.primaryClip = ClipData.newPlainText(label, text)
}

fun Context.getColour(id: Int): Int {
	if (Build.VERSION.SDK_INT < 23) {
		@Suppress("DEPRECATION")
		return resources.getColor(id)
	} else {
		return resources.getColor(id, null)
	}
}
