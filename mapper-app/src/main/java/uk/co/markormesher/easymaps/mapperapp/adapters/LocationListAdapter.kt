package uk.co.markormesher.easymaps.mapperapp.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import kotlinx.android.synthetic.main.list_item_attraction.view.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.Location
import java.util.*

// TODO: display message when no results match

class LocationListAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

	private val layoutInflater by lazy { LayoutInflater.from(context)!! }

	val locations = ArrayList<Location>()
	private val filteredLocations = ArrayList<Location>()
	private var activeFilter = ""

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
		return LocationViewHolder(layoutInflater.inflate(R.layout.list_item_location, parent, false))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val location = filteredLocations[position]
		with(holder as LocationViewHolder) {
			title.text = highlightByFilter(location.getDisplayTitle(context))
		}
	}

	override fun getItemCount(): Int = filteredLocations.size

	class LocationViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val title = v.location_title!!
	}

	override fun getFilter(): Filter {
		return object: Filter() {

			val emptyFilter by lazy { FilterResults() }

			override fun performFiltering(constraint: CharSequence?): FilterResults {
				activeFilter = constraint?.toString() ?: ""

				filteredLocations.clear()
				if (constraint?.isBlank() ?: true) {
					filteredLocations.addAll(locations)
				} else {
					filteredLocations.addAll(locations.filter { l ->
						l.getDisplayTitle(context).contains(constraint!!, true)
					})
				}

				return emptyFilter
			}

			@Suppress("UNCHECKED_CAST")
			override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
				notifyDataSetChanged()
			}
		}
	}

	private fun highlightByFilter(raw: String): SpannableString {
		val output = SpannableString(raw)
		if (!activeFilter.isBlank()) {
			val pos = raw.toLowerCase().indexOf(activeFilter.toLowerCase())
			if (pos >= 0) {
				output.setSpan(ForegroundColorSpan(Color.RED), pos, pos + activeFilter.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
			}
		}
		return output
	}

}
