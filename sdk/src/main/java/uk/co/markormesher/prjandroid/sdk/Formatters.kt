package uk.co.markormesher.prjandroid.sdk

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned

fun Context.makeHtml(source: Int, vararg formatArgs: Any?): Spanned {
	val str = getString(source, *formatArgs)
	if (Build.VERSION.SDK_INT < 24) {
		@Suppress("DEPRECATION")
		return Html.fromHtml(str)
	} else {
		return Html.fromHtml(str, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
	}
}