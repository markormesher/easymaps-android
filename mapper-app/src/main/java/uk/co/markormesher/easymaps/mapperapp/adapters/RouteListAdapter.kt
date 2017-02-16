package uk.co.markormesher.easymaps.mapperapp.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.list_item_route.view.*
import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.TravelMode
import uk.co.markormesher.easymaps.mapperapp.routing.Route
import java.util.*

class RouteListAdapter(val context: Context, val selectListener: OnSelectListener? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>(), AnkoLogger {

	private val layoutInflater by lazy { LayoutInflater.from(context)!! }

	private val routes = ArrayList<Route>()
	private val routeSummaries = ArrayList<RouteSummary>()

	fun updateRoutes(routes: List<Route>) {
		this.routes.clear()
		this.routes.addAll(routes)
		updateRouteSummaries()
		notifyDataSetChanged()
	}

	private fun updateRouteSummaries() {
		routeSummaries.clear()
		routes.forEach { route ->
			var lastMode = TravelMode.UNKNOWN
			var runLength = 0
			with(RouteSummary()) {
				cost = route.cost
				route.modes.forEach { mode ->
					if (lastMode != mode) {
						if (lastMode != TravelMode.UNKNOWN) {
							travelSegments.add(TravelSegment(lastMode, runLength))
							++changes
						}
						lastMode = mode
						runLength = 1
					} else {
						++runLength
					}
					lastMode = mode
				}
				travelSegments.add(TravelSegment(lastMode, runLength))
				routeSummaries.add(this)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
		return RouteViewHolder(layoutInflater.inflate(R.layout.list_item_route, parent, false))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		with(holder as RouteViewHolder) {
			val route = routes[position]
			val routeSummary = routeSummaries[position]

			colourBlocks.forEachIndexed { i, block ->
				val lp = block.layoutParams as LinearLayout.LayoutParams
				if (i < routeSummary.travelSegments.size) {
					val segment = routeSummary.travelSegments[i]
					lp.weight = segment.count.toFloat() / route.modes.size
					block.setBackgroundColor(segment.mode.colourCode)
					block.visibility = View.VISIBLE
				} else {
					lp.weight = 0.0f
					block.visibility = View.GONE
				}
				block.layoutParams = lp
			}
			colourBar.requestLayout()

			val summaryText = with(StringBuilder()) {
				append(context.resources.getQuantityString(R.plurals.route_changes, routeSummary.changes, routeSummary.changes))
				append("\n")
				route.modes.forEachIndexed { i, mode ->
					append(String.format(mode.verb, route.locations[i + 1].getDisplayTitle(context)))
					append("\n")
				}
				toString().trim()
			}

			title.text = summaryText

			rootView.setOnClickListener { selectListener?.onRouteSelected(position) }
		}
	}

	override fun getItemCount() = routes.size

	class RouteViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val rootView = v
		val title = v.route_title!!
		val colourBar = v.route_colour_bar!!
		val colourBlocks = (0..colourBar.childCount - 1).map({ i -> colourBar.getChildAt(i) }).toList()
	}

	interface OnSelectListener {
		fun onRouteSelected(index: Int)
	}

	private data class TravelSegment(val mode: TravelMode, val count: Int)

	private data class RouteSummary(
			val travelSegments: LinkedList<TravelSegment> = LinkedList<TravelSegment>(),
			var changes: Int = 0,
			var cost: Int = 0)

}
