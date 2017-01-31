package uk.co.markormesher.easymaps.mapperapp.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.markormesher.easymaps.mapperapp.LATEST_DATA_PACK_VERSION_KEY
import uk.co.markormesher.easymaps.mapperapp.LATEST_LABELLING_VERSION_KEY
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.adapters.LocationListAdapter
import uk.co.markormesher.easymaps.mapperapp.services.DataDownloaderService
import uk.co.markormesher.easymaps.sdk.BaseActivity
import uk.co.markormesher.easymaps.sdk.getLongPref
import uk.co.markormesher.easymaps.sdk.makeHtml

class MainActivity: BaseActivity() {

	val iconSpinAnimation: Animation? by lazy { AnimationUtils.loadAnimation(this, R.anim.icon_spin) }

	val locationListAdapter by lazy { LocationListAdapter(this) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// check for offline data
		updateFullPageStatus(FullPageStatusType.WAITING, getString(R.string.checking_for_offline_data))
		if (hasOfflineData()) {
			loadLocations()
		} else {
			prepareForInitialOfflineDataDownload()
		}

		// set up location list
		val screenWidthInDp = resources.configuration.screenWidthDp
		val columns = screenWidthInDp / 110
		val gridLayoutManager = GridLayoutManager(this, columns)
		gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
			override fun getSpanSize(position: Int): Int = if (position == 0) columns else 1
		}
		location_grid.layoutManager = gridLayoutManager
		location_grid.adapter = locationListAdapter
	}

	override fun onResume() {
		super.onResume()
		registerReceiver(offlineDataUpdatedReceiver, IntentFilter(getString(R.string.intent_offline_data_updated)))
	}

	override fun onPause() {
		super.onPause()
		unregisterReceiver(offlineDataUpdatedReceiver)
	}

	/*
	private fun updateStatusBar(type: StatusBarType, heading: String, message: String) {
		status_heading.text = heading
		status_message.text = message

		// icon
		status_icon.setImageResource(when (type) {
			StatusBarType.SEARCHING -> R.drawable.ic_location_searching_white_48dp
			StatusBarType.INFO -> R.drawable.ic_info_outline_white_48dp
			StatusBarType.LOCATION_ON -> R.drawable.ic_location_on_white_48dp
			StatusBarType.LOCATION_OFF -> R.drawable.ic_location_off_white_48dp
			else -> R.drawable.ic_info_outline_white_48dp
		})

		// icon animation
		if (type == StatusBarType.SEARCHING) {
			status_icon.startAnimation(iconSpinAnimation)
		} else {
			status_icon.clearAnimation()
		}
	}

	enum class StatusBarType {
		SEARCHING,
		INFO,
		LOCATION_ON,
		LOCATION_OFF,
	}
	*/

	private fun updateFullPageStatus(type: FullPageStatusType, message: String = "") {
		when (type) {
			FullPageStatusType.NONE -> {
				location_grid.visibility = View.VISIBLE
				full_page_status_wrapper.visibility = View.GONE
				full_page_status_icon.clearAnimation()
				full_page_status_message.text = ""
			}

			FullPageStatusType.WAITING -> {
				location_grid.visibility = View.GONE
				full_page_status_wrapper.visibility = View.VISIBLE
				full_page_status_icon.setImageResource(R.drawable.ic_hourglass_empty_white_48dp)
				full_page_status_icon.startAnimation(iconSpinAnimation)
				full_page_status_message.text = message
			}

			FullPageStatusType.ERROR -> {
				location_grid.visibility = View.GONE
				full_page_status_wrapper.visibility = View.VISIBLE
				full_page_status_icon.setImageResource(R.drawable.ic_info_outline_white_48dp)
				full_page_status_icon.clearAnimation()
				full_page_status_message.text = message
			}
		}
	}

	enum class FullPageStatusType {
		WAITING, ERROR, NONE
	}

	private fun hasOfflineData(): Boolean {
		return getLongPref(LATEST_LABELLING_VERSION_KEY) > 0 && getLongPref(LATEST_DATA_PACK_VERSION_KEY) > 0
	}

	private fun prepareForInitialOfflineDataDownload() {
		updateFullPageStatus(FullPageStatusType.ERROR, getString(R.string.no_offline_data))
		full_page_status_message.setOnClickListener {
			with(AlertDialog.Builder(this)) {
				setTitle(getString(R.string.initial_download_title))
				setMessage(makeHtml(getString(R.string.initial_download_body)))
				setPositiveButton(getString(R.string.initial_download_btn_positive), { dialogInterface, i ->
					startInitialOfflineDataDownload()
				})
				setNegativeButton(getString(R.string.initial_download_btn_negative), null)
				create().show()
			}
		}
	}

	private fun startInitialOfflineDataDownload() {
		updateFullPageStatus(FullPageStatusType.WAITING, getString(R.string.waiting_for_offline_data))
		startService(Intent(this, DataDownloaderService::class.java))
	}

	private val offlineDataUpdatedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (hasOfflineData()) {
				loadLocations()
			} else {
				prepareForInitialOfflineDataDownload()
			}
		}
	}

	private fun loadLocations() {
		updateFullPageStatus(FullPageStatusType.NONE)
	}

}

