package uk.co.markormesher.easymaps.mapperapp.helpers

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat

fun Context.getTintedDrawable(@DrawableRes drawableResId: Int, @ColorRes colorResId: Int): Drawable? {
	if (drawableResId == 0) return null
	val drawable = ContextCompat.getDrawable(this, drawableResId).mutate()
	val color = ContextCompat.getColor(this, colorResId)
	drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
	return drawable
}