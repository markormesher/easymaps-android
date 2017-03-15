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
				duration = route.duration
				route.modes.forEach { mode ->
					if (lastMode != mode) {
						if (lastMode != TravelMode.UNKNOWN) {
							segments.add(TravelSegment(lastMode, runLength))
							++changes
						}
						lastMode = mode
						runLength = 1
					} else {
						++runLength
					}
					lastMode = mode
				}
				segments.add(TravelSegment(lastMode, runLength))
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
				if (i < routeSummary.segments.size) {
					val segment = routeSummary.segments[i]
					lp.weight = segment.count.toFloat() / route.modes.size
					if (segment.mode == TravelMode.WALK) {
						block.setBackgroundResource(R.drawable.walking_bg_horizontal)
					} else {
						block.setBackgroundColor(segment.mode.colourCode)
					}
					block.visibility = View.VISIBLE
				} else {
					lp.weight = 0.0f
					block.visibility = View.GONE
				}
				block.layoutParams = lp
			}
			colourBar.requestLayout()

			val segmentSummary = routeSummary.segments.map({ s -> s.mode.displayName }).joinToString(", ")
			val mins = Math.ceil(routeSummary.duration.toDouble() / 60).toInt()
			val duration = context.resources.getQuantityString(R.plurals.route_duration, mins, mins)
			val changes = context.resources.getQuantityString(R.plurals.route_changes, routeSummary.changes, routeSummary.changes)

			summary.text = segmentSummary
			details.text = context.getString(R.string.route_details, duration, changes)

			rootView.setOnClickListener { selectListener?.onRouteSelected(position) }
		}
	}

	override fun getItemCount() = routes.size

	class RouteViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val rootView = v
		val summary = v.route_summary!!
		val details = v.route_details!!
		val colourBar = v.route_colour_bar!!
		val colourBlocks = (0..colourBar.childCount - 1).map({ i -> colourBar.getChildAt(i) }).toList()
	}

	interface OnSelectListener {
		fun onRouteSelected(index: Int)
	}

	private data class TravelSegment(val mode: TravelMode, val count: Int)

	private data class RouteSummary(
			val segments: LinkedList<TravelSegment> = LinkedList<TravelSegment>(),
			var changes: Int = 0,
			var duration: Int = 0)

}
