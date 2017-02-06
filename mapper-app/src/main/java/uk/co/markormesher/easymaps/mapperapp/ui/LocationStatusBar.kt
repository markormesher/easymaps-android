package uk.co.markormesher.easymaps.mapperapp.ui

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.ui_status_bar.view.*
import uk.co.markormesher.easymaps.mapperapp.R

class LocationStatusBar: LinearLayout {

	private val iconSpinAnimation: Animation? by lazy { AnimationUtils.loadAnimation(context, R.anim.icon_spin) }
	private var iconIsAnimated = false

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
		inflate(context, R.layout.ui_status_bar, this)
	}

	fun setStatus(status: Status) {
		status_icon.setImageResource(when (status) {
			Status.SEARCHING -> R.drawable.ic_location_searching_white_48dp
			Status.WAITING -> R.drawable.ic_hourglass_empty_white_48dp
			Status.INFO -> R.drawable.ic_info_outline_white_48dp
			Status.LOCATION_ON -> R.drawable.ic_location_on_white_48dp
			Status.LOCATION_OFF -> R.drawable.ic_location_off_white_48dp
			Status.WIFI_OFF -> R.drawable.ic_signal_wifi_off_white_48dp
			else -> R.drawable.ic_info_outline_white_48dp
		})

		if (status == Status.SEARCHING || status == Status.WAITING) {
			if (!iconIsAnimated) {
				status_icon.startAnimation(iconSpinAnimation)
				iconIsAnimated = true
			}
		} else {
			if (iconIsAnimated) {
				status_icon.clearAnimation()
				iconIsAnimated = false
			}
		}
	}

	fun setHeading(heading: String) {
		status_heading.text = heading
	}

	fun setMessage(message: String) {
		status_message.text = message
	}

	enum class Status {
		SEARCHING,
		WAITING,
		INFO,
		LOCATION_ON,
		LOCATION_OFF,
		WIFI_OFF
	}

}
