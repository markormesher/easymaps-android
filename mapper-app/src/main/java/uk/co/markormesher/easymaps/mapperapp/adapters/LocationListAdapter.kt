package uk.co.markormesher.easymaps.mapperapp.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_attraction.view.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.LocationType
import uk.co.markormesher.easymaps.sdk.makeHtml
import java.util.*

class LocationListAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	private val layoutInflater by lazy { LayoutInflater.from(context)!! }

	val locations = ArrayList<Location>()

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
		return LocationViewHolder(layoutInflater.inflate(R.layout.list_item_location, parent, false))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val location = locations[position]
		with(holder as LocationViewHolder) {
			when (location.type) {
				LocationType.ATTRACTION -> title.text = context.getString(R.string.select_location_attraction_template, location.title)
				LocationType.STATION -> title.text = context.getString(R.string.select_location_station_template, location.title)
				else -> title.text = location.title
			}
		}
	}

	override fun getItemCount(): Int = locations.size

	class LocationViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val title = v.location_title!!
	}

}
