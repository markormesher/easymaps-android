package uk.co.markormesher.easymaps.mapperapp.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_route_step.view.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.routing.Route

class RouteDisplayAdapter(val context: Context, val route: Route): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	private val layoutInflater by lazy { LayoutInflater.from(context)!! }

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
		return RouteStepViewHolder(layoutInflater.inflate(R.layout.list_item_route_step, parent, false))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		with(holder as RouteStepViewHolder) {
			if (position % 2 == 0) {
				val location = route.locations[position / 2]
				title.text = location.getDisplayTitle(context)
			} else {
				val mode = route.modes[(position - 1) / 2]
				title.text = mode.displayName
			}
		}
	}

	override fun getItemCount() = (route.locations.size * 2) - 1

	class RouteStepViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val rootView = v
		val title = v.step_title!!
	}

}
