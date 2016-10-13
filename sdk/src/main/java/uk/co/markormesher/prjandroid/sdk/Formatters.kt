package uk.co.markormesher.prjandroid.sdk

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned

fun Context.makeHtml(source: Int): Spanned {
	if (Build.VERSION.SDK_INT < 24) {
		@Suppress("DEPRECATION")
		return Html.fromHtml(getString(source))
	} else {
		return Html.fromHtml(getString(source), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
	}
}