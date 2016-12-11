package uk.co.markormesher.easymaps.sdk

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import java.text.SimpleDateFormat
import java.util.*

fun Context.makeHtml(source: Int, vararg formatArgs: Any?): Spanned {
	return makeHtml(getString(source, *formatArgs))
}

fun makeHtml(html: String): Spanned {
	if (Build.VERSION.SDK_INT < 24) {
		@Suppress("DEPRECATION")
		return Html.fromHtml(html)
	} else {
		return Html.fromHtml(html, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
	}
}

fun getDateString(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())
