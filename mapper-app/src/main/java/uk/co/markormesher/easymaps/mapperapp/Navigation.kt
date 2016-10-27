package uk.co.markormesher.easymaps.mapperapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.nav_item.view.*

data class NavItem(val id: Int, val label: Int, val icon: Int)

val NAV_HOME = 1
val NAV_ABOUT = 2
val NAV_SETTINGS = 3

private val NAVIGATION_ITEMS = arrayOf(
		NavItem(NAV_HOME, R.string.nav_home, R.drawable.ic_home_white),
		NavItem(NAV_ABOUT, R.string.nav_about, R.drawable.ic_info_outline_white),
		NavItem(NAV_SETTINGS, R.string.nav_settings, R.drawable.ic_settings_white)
)

class NavAdapter(val context: Context) : BaseAdapter() {

	val inflater: LayoutInflater? by lazy { LayoutInflater.from(context) }

	override fun getView(position: Int, recycleView: View?, parent: ViewGroup?): View {
		val view = recycleView
				?: inflater?.inflate(R.layout.nav_item, parent, false)
				?: throw Exception("Could not recycle or inflate view")

		val item = getItem(position)
		view.id = item.id
		view.label.text = context.getString(item.label)
		view.icon.setImageResource(item.icon)

		return view
	}

	override fun getItem(position: Int): NavItem = NAVIGATION_ITEMS[position]

	override fun getCount(): Int = NAVIGATION_ITEMS.size

	override fun getItemId(position: Int): Long = 0L

}
