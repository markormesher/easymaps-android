package uk.co.markormesher.easymaps.mapperapp.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.ui_route_step_indicator.view.*
import uk.co.markormesher.easymaps.mapperapp.R

class RouteStepIndicator: RelativeLayout {

	constructor(context: Context): super(context) {
		init()
	}

	constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
		init()
	}

	constructor(context: Context, attrs: AttributeSet?, defStyle: Int): super(context, attrs, defStyle) {
		init()
	}

	private fun init() {
		LinearLayout.inflate(context, R.layout.ui_route_step_indicator, this)
	}

	fun setIndicatorType(type: IndicatorType) {
		when (type) {
			IndicatorType.STATION -> {
				station_marker.visibility = View.VISIBLE
				change_marker.visibility = View.INVISIBLE
			}

			IndicatorType.CHANGE -> {
				station_marker.visibility = View.INVISIBLE
				change_marker.visibility = View.VISIBLE
			}
		}
	}

	fun setTopPipeColour(colour: Int) = top_pipe.setBackgroundColor(colour)

	fun setBottomPipeColour(colour: Int) = bottom_pipe.setBackgroundColor(colour)

	fun setStationMarkerColour(colour: Int) = station_marker.setBackgroundColor(colour)

	enum class IndicatorType {
		STATION, CHANGE
	}
}
