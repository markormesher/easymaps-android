package uk.co.markormesher.easymaps.mapperapp.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.GridLayoutManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.adapters.LocationListAdapter
import uk.co.markormesher.easymaps.sdk.BaseActivity

class MainActivity : BaseActivity() {

	val locationListAdapter by lazy { LocationListAdapter(this) }
	val searchingIconAnimation: Animation? by lazy { AnimationUtils.loadAnimation(this, R.anim.searching_icon) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val widthInDp = resources.configuration.screenWidthDp
		val columns = widthInDp / 110
		val gridLayoutManager = GridLayoutManager(this, columns)
		gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
			override fun getSpanSize(position: Int): Int = if (position == 0) columns else 1
		}
		location_grid.layoutManager = gridLayoutManager
		location_grid.adapter = locationListAdapter

		// sample status demo
		setStatus(Status.LOCATION_OFF, "You have location services disabled!", "Just tap here to turn them on")
		with(Handler(Looper.getMainLooper())) {
			postDelayed({ setStatus(Status.SEARCHING, "Looking for you...", "Hold on a sec!") }, 4000)
			postDelayed({ setStatus(Status.LOCATION_ON, "You're at Oxford Circus", "Bakerloo, Central and Victoria lines") }, 8000)
		}
	}

	private fun setStatus(status: Status, heading: String, message: String) {
		status_heading.text = heading
		status_message.text = message

		// icon
		status_icon.setImageResource(when (status) {
			Status.SEARCHING -> R.drawable.ic_location_searching_white_48dp
			Status.INFO -> R.drawable.ic_info_outline_white_48dp
			Status.LOCATION_ON -> R.drawable.ic_location_on_white_48dp
			Status.LOCATION_OFF -> R.drawable.ic_location_off_white_48dp
			else -> R.drawable.ic_info_outline_white_48dp
		})

		// icon animation
		if (status == Status.SEARCHING) {
			status_icon.startAnimation(searchingIconAnimation)
		} else {
			status_icon.clearAnimation()
		}
	}

	enum class Status {
		SEARCHING,
		INFO,
		LOCATION_ON,
		LOCATION_OFF,
	}
}

