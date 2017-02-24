package uk.co.markormesher.easymaps.mapperapp.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_route_step.view.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.routing.AugmentedRoute
import uk.co.markormesher.easymaps.mapperapp.ui.RouteStepIndicator

class RouteDisplayAdapter(val context: Context, val route: AugmentedRoute): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	private val layoutInflater by lazy { LayoutInflater.from(context)!! }

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
		return RouteStepViewHolder(layoutInflater.inflate(R.layout.list_item_route_step, parent, false))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		with(holder as RouteStepViewHolder) {
			val stage = route.stages[position]

			locationNameView.text = stage.location.getDisplayTitle(context)
			if (stage.isMajor) {
				instructionView.visibility = View.VISIBLE
				instructionView.text = stage.instruction
			} else {
				instructionView.visibility = View.GONE
			}

			if (stage.prevMode == stage.nextMode) {
				indicator.setIndicatorType(RouteStepIndicator.IndicatorType.STATION)
				indicator.setStationMarkerMode(stage.prevMode)
			} else {
				indicator.setIndicatorType(RouteStepIndicator.IndicatorType.CHANGE)
			}
			indicator.setTopPipeMode(stage.prevMode)
			indicator.setBottomPipeMode(stage.nextMode)
		}
	}

	override fun getItemCount() = route.stages.size

	class RouteStepViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val locationNameView = v.location_name!!
		val instructionView = v.instruction!!
		val indicator = v.indicator!!
	}

}
