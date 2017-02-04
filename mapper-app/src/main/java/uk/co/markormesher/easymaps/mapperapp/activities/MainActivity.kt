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
import uk.co.markormesher.easymaps.mapperapp.adapters.AttractionListAdapter
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.mapperapp.services.DataDownloaderService
import uk.co.markormesher.easymaps.sdk.BaseActivity
import uk.co.markormesher.easymaps.sdk.getLongPref
import uk.co.markormesher.easymaps.sdk.makeHtml

// TODO: new activity - route planner
// TODO: new server - location sensing

// TODO: click listener on recycler view

class MainActivity: BaseActivity() {

	val iconSpinAnimation: Animation? by lazy { AnimationUtils.loadAnimation(this, R.anim.icon_spin) }

	val initialOfflineDataDownloadConfirmation: AlertDialog by lazy {
		with(AlertDialog.Builder(this)) {
			setTitle(getString(R.string.initial_download_title))
			setMessage(makeHtml(getString(R.string.initial_download_body)))
			setPositiveButton(getString(R.string.initial_download_btn_positive), { dialogInterface, i ->
				triedInitialOfflineDataDownload = true
				startOfflineDataUpdate(blockUi = true, force = true)
			})
			setNegativeButton(getString(R.string.initial_download_btn_negative), null)
			create()
		}
	}

	val attractionListAdapter by lazy { AttractionListAdapter(this) }

	var triedInitialOfflineDataDownload = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// check for offline data
		updateFullPageStatus(FullPageStatusType.WAITING, getString(R.string.checking_for_offline_data))
		if (hasOfflineData()) {
			loadAttractions()
			startOfflineDataUpdate()
		} else {
			promptInitialOfflineDataDownload()
		}

		// set up attraction list
		val screenWidthInDp = resources.configuration.screenWidthDp
		val columns = screenWidthInDp / 110
		val gridLayoutManager = GridLayoutManager(this, columns)
		gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
			override fun getSpanSize(position: Int): Int = if (position == 0) columns else 1
		}
		attraction_grid.layoutManager = gridLayoutManager
		attraction_grid.adapter = attractionListAdapter

		status_icon.setOnClickListener {
			startActivity(Intent(this, LocationSearchActivity::class.java))
		}
	}

	override fun onResume() {
		super.onResume()
		registerReceiver(offlineDataUpdatedReceiver, IntentFilter(OfflineDatabase.STATE_UPDATED))
	}

	override fun onPause() {
		super.onPause()
		unregisterReceiver(offlineDataUpdatedReceiver)
	}

	private fun hasOfflineData(): Boolean {
		return getLongPref(LATEST_LABELLING_VERSION_KEY) > 0 && getLongPref(LATEST_DATA_PACK_VERSION_KEY) > 0
	}

	private fun promptInitialOfflineDataDownload() {
		if (triedInitialOfflineDataDownload) {
			updateFullPageStatus(FullPageStatusType.ERROR, getString(R.string.offline_data_download_failed))
		} else {
			updateFullPageStatus(FullPageStatusType.ERROR, getString(R.string.no_offline_data))
		}
		full_page_status_message.setOnClickListener { initialOfflineDataDownloadConfirmation.show() }
		full_page_status_icon.setOnClickListener { initialOfflineDataDownloadConfirmation.show() }
	}

	private fun startOfflineDataUpdate(blockUi: Boolean = false, force: Boolean = false) {
		if (blockUi) {
			updateFullPageStatus(FullPageStatusType.WAITING, getString(R.string.waiting_for_offline_data))
		}
		val intent = Intent(this, DataDownloaderService::class.java)
		if (force) {
			intent.putExtra(DataDownloaderService.FORCE, true)
		}
		startService(intent)
	}

	private val offlineDataUpdatedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (hasOfflineData()) {
				loadAttractions()
			} else {
				promptInitialOfflineDataDownload()
			}
		}
	}

	private fun loadAttractions() {
		updateFullPageStatus(FullPageStatusType.WAITING, getString(R.string.loading_attractions))
		val attractions = OfflineDatabase(this).getAttractions()
		attractionListAdapter.attractions.clear()
		attractionListAdapter.attractions.addAll(attractions)
		attractionListAdapter.notifyDataSetChanged()
		updateFullPageStatus(FullPageStatusType.NONE)
	}

	private fun updateFullPageStatus(type: FullPageStatusType, message: String = "") {
		when (type) {
			FullPageStatusType.NONE -> {
				attraction_grid.visibility = View.VISIBLE
				full_page_status_wrapper.visibility = View.GONE
				full_page_status_icon.clearAnimation()
				full_page_status_message.text = ""
			}

			FullPageStatusType.WAITING -> {
				attraction_grid.visibility = View.GONE
				full_page_status_wrapper.visibility = View.VISIBLE
				full_page_status_icon.setImageResource(R.drawable.ic_hourglass_empty_white_48dp)
				full_page_status_icon.startAnimation(iconSpinAnimation)
				full_page_status_message.text = message
			}

			FullPageStatusType.ERROR -> {
				attraction_grid.visibility = View.GONE
				full_page_status_wrapper.visibility = View.VISIBLE
				full_page_status_icon.setImageResource(R.drawable.ic_info_outline_white_48dp)
				full_page_status_icon.clearAnimation()
				full_page_status_message.text = message
			}
		}

		full_page_status_wrapper.setOnClickListener(null)
		full_page_status_message.setOnClickListener(null)
		full_page_status_icon.setOnClickListener(null)
	}

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

	enum class FullPageStatusType {
		WAITING, ERROR, NONE
	}

}

