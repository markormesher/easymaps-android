package uk.co.markormesher.easymaps.mapperapp.adapters

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import kotlinx.android.synthetic.main.list_item_attraction.view.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.Location
import java.util.*

class LocationListAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

	private val layoutInflater by lazy { LayoutInflater.from(context)!! }

	val locations = ArrayList<Location>()
	private val filteredLocations = ArrayList<Location>()
	private var activeFilter = ""

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
		return LocationViewHolder(layoutInflater.inflate(R.layout.list_item_location, parent, false))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		with(holder as LocationViewHolder) {
			if (filteredLocations.isEmpty()) {
				title.text = context.getString(R.string.select_location_no_locations, activeFilter)
				title.setTypeface(null, Typeface.ITALIC)
			} else {
				val location = filteredLocations[position]
				title.text = highlightByFilter(location.getDisplayTitle(context))
				title.setTypeface(null, Typeface.NORMAL)
			}
		}
	}

	override fun getItemCount(): Int {
		if (filteredLocations.isEmpty()) {
			return 1
		} else {
			return filteredLocations.size
		}
	}

	class LocationViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val title = v.location_title!!
	}

	private val locationFilter by lazy {
		object: Filter() {

			override fun performFiltering(constraint: CharSequence?): Filter.FilterResults? {
				activeFilter = constraint?.toString() ?: ""

				if (constraint?.isBlank() ?: true) {
					return null
				}

				val filterResults = FilterResults()
				filterResults.values = locations.filter { l ->
					l.getDisplayTitle(context).contains(constraint!!, true)
				}

				return filterResults
			}

			@Suppress("UNCHECKED_CAST")
			override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
				filteredLocations.clear()
				if (results == null) {
					filteredLocations.addAll(locations)
				} else {
					filteredLocations.addAll(results.values as Collection<Location>)
				}
				notifyDataSetChanged()
			}
		}
	}

	override fun getFilter(): Filter = locationFilter

	private fun highlightByFilter(raw: String): SpannableString {
		val output = SpannableString(raw)
		if (!activeFilter.isBlank()) {
			var pos = raw.indexOf(activeFilter, 0, true)
			while (pos >= 0) {
				output.setSpan(StyleSpan(Typeface.BOLD), pos, pos + activeFilter.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
				pos = raw.indexOf(activeFilter, pos + 1, true)
			}
		}
		return output
	}

}
