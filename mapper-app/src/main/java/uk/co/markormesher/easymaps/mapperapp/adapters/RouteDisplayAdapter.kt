package uk.co.markormesher.easymaps.mapperapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_route_step.view.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.TravelMode
import uk.co.markormesher.easymaps.mapperapp.routing.Route
import java.util.*

class RouteDisplayAdapter(val context: Context, val route: Route): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	@SuppressLint("UseSparseArrays")
	private val modeEndIndexes = HashMap<Int, Int>()
	private val layoutInflater by lazy { LayoutInflater.from(context)!! }

	init {
		var lastChangeIndex = 0
		for (i in 1..route.locations.size - 1) {
			val prevMode = if (i > 0) route.modes[i - 1] else null
			val nextMode = if (i < route.modes.size) route.modes[i] else null
			if (prevMode != nextMode) {
				modeEndIndexes[lastChangeIndex] = i
				lastChangeIndex = i
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
		return RouteStepViewHolder(layoutInflater.inflate(R.layout.list_item_route_step, parent, false))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		with(holder as RouteStepViewHolder) {
			val location = route.locations[position]
			val modeEndIndex = modeEndIndexes[position] ?: -1
			val modeEndLocation = if (modeEndIndex >= 0) route.locations[modeEndIndex] else null
			val prevMode = if (position > 0) route.modes[position - 1] else null
			val nextMode = if (position < route.modes.size) route.modes[position] else null

			var instruction = ""

			if (prevMode == null) { // start of route
				if (nextMode == null) {
					instruction = ""
				} else if (nextMode == TravelMode.WALK) {
					instruction = context.getString(R.string.route_guidance_walk_to, modeEndLocation?.getDisplayTitle(context))
				} else if (nextMode.isTube()) {
					instruction = context.getString(R.string.route_guidance_tube_to, nextMode.displayName, modeEndLocation?.getDisplayTitle(context))
				}

			} else if (nextMode == null) { // end of route
				instruction = ""

			} else if (prevMode == TravelMode.WALK) { // mid-route, transition from walk
				if (nextMode.isTube()) {
					instruction = context.getString(R.string.route_guidance_tube_to, nextMode.displayName, modeEndLocation?.getDisplayTitle(context))
				}

			} else if (prevMode.isTube()) { // mid-route, transition from tube
				if (nextMode == TravelMode.WALK) {
					instruction = context.getString(R.string.route_guidance_walk_to, modeEndLocation?.getDisplayTitle(context))
				} else if (prevMode == nextMode) {
					instruction = ""
				} else if (nextMode.isTube()) {
					instruction = context.getString(R.string.route_guidance_tube_to, nextMode.displayName, modeEndLocation?.getDisplayTitle(context))
				}

			} else {
				instruction = ""
			}

			locationNameView.text = location.getDisplayTitle(context)
			if (instruction.isBlank()) {
				instructionView.visibility = View.GONE
			} else {
				instructionView.visibility = View.VISIBLE
				instructionView.text = instruction
			}
		}
	}

	override fun getItemCount() = route.locations.size

	class RouteStepViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val rootView = v
		val locationNameView = v.location_name!!
		val instructionView = v.instruction!!
	}

}
