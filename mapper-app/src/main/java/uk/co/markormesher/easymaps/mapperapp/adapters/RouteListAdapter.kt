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
							uniqueModes.add(Pair(lastMode, runLength))
							++changes
						}
						lastMode = mode
						runLength = 1
					} else {
						++runLength
					}
					lastMode = mode
				}
				uniqueModes.add(Pair(lastMode, runLength))
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

			for (i in (0..colourBar.childCount - 1)) {
				val block = colourBar.getChildAt(i)
				val lp = block.layoutParams as LinearLayout.LayoutParams
				if (i < routeSummary.uniqueModes.size) {
					val mode = routeSummary.uniqueModes[i]
					lp.weight = (mode.second.toFloat() / route.modes.size) * 100
					block.setBackgroundColor(mode.first.colourCode)
				} else {
					lp.weight = 0.0f
				}
				block.layoutParams = lp
			}
			colourBar.requestLayout()

			title.text = context.resources.getQuantityString(R.plurals.route_changes, routeSummary.changes, routeSummary.changes)

			rootView.setOnClickListener { selectListener?.onRouteSelected(position) }
		}
	}

	override fun getItemCount() = routes.size

	class RouteViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val rootView = v
		val title = v.route_title!!
		val colourBar = v.route_colour_bar!!
	}

	interface OnSelectListener {
		fun onRouteSelected(index: Int)
	}

	private data class RouteSummary(
			val uniqueModes: LinkedList<Pair<TravelMode, Int>> = LinkedList<Pair<TravelMode, Int>>(),
			var changes: Int = 0,
			var cost: Int = 0)

}
