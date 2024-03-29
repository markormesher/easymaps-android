package uk.co.markormesher.easymaps.mapperapp.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.ui_route_step_indicator.view.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.TravelMode

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

	fun setTopPipeMode(mode: TravelMode?) {
		if (mode == null) {
			top_pipe.visibility = View.INVISIBLE
		} else {
			top_pipe.visibility = View.VISIBLE
			if (mode == TravelMode.WALK) {
				top_pipe.setBackgroundResource(R.drawable.walking_bg_vertical)
			} else {
				top_pipe.setBackgroundColor(mode.colourCode)
			}
		}
	}

	fun setBottomPipeMode(mode: TravelMode?) {
		if (mode == null) {
			bottom_pipe.visibility = View.INVISIBLE
		} else {
			bottom_pipe.visibility = View.VISIBLE
			if (mode == TravelMode.WALK) {
				bottom_pipe.setBackgroundResource(R.drawable.walking_bg_vertical)
			} else {
				bottom_pipe.setBackgroundColor(mode.colourCode)
			}
		}
	}

	fun setStationMarkerMode(mode: TravelMode?) {
		station_marker.setBackgroundColor(mode?.colourCode ?: Color.TRANSPARENT)
	}

	enum class IndicatorType {
		STATION, CHANGE
	}
}
