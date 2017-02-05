package uk.co.markormesher.easymaps.mapperapp.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_offline_data_download.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.mapperapp.helpers.getTintedDrawable
import uk.co.markormesher.easymaps.sdk.BaseActivity
import uk.co.markormesher.easymaps.sdk.makeHtml

// TODO: handle back-press during update

class OfflineDataDownloadActivity: BaseActivity() {

	private val iconSpinAnimation by lazy { AnimationUtils.loadAnimation(this, R.anim.icon_spin) }

	private var triedInitialOfflineDataDownload = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_offline_data_download)

		if (OfflineDatabase.isPopulated(this)) {
			return finish()
		}

		promptDownload()
	}

	override fun onResume() {
		super.onResume()
		registerReceiver(offlineDataUpdatedReceiver, IntentFilter(OfflineDatabase.STATE_UPDATED))
	}

	override fun onPause() {
		super.onPause()
		unregisterReceiver(offlineDataUpdatedReceiver)
	}

	private fun setUiState(state: UiState, message: String) {
		status_icon.setImageDrawable(when (state) {
			UiState.INFO -> getTintedDrawable(R.drawable.ic_info_outline_white_48dp, R.color.primary_dark)
			UiState.ERROR -> getTintedDrawable(R.drawable.ic_info_outline_white_48dp, R.color.primary_dark)
			UiState.WAITING -> getTintedDrawable(R.drawable.ic_hourglass_empty_white_48dp, R.color.primary_dark)
		})

		if (state == UiState.WAITING) {
			status_icon.startAnimation(iconSpinAnimation)
		} else {
			status_icon.clearAnimation()
		}

		status_message.text = message

		status_message.setOnClickListener { }
		status_icon.setOnClickListener { }
	}

	enum class UiState {
		INFO, ERROR, WAITING
	}

	private fun promptDownload() {
		if (triedInitialOfflineDataDownload) {
			setUiState(UiState.ERROR, getString(R.string.offline_data_download_failed))
			status_message.setOnClickListener { startDownload() }
			status_icon.setOnClickListener { startDownload() }
		} else {
			setUiState(UiState.ERROR, getString(R.string.no_offline_data))
			status_message.setOnClickListener { confirmDownload() }
			status_icon.setOnClickListener { confirmDownload() }
		}
	}

	private fun confirmDownload() {
		with(AlertDialog.Builder(this)) {
			setTitle(getString(R.string.initial_download_title))
			setMessage(makeHtml(getString(R.string.initial_download_body)))
			setPositiveButton(getString(R.string.initial_download_btn_positive), { dialogInterface, i ->
				triedInitialOfflineDataDownload = true
				startDownload(true)
			})
			setNegativeButton(getString(R.string.initial_download_btn_negative), null)
			create().show()
		}
	}

	private fun startDownload(force: Boolean = false) {
		setUiState(UiState.WAITING, getString(R.string.waiting_for_offline_data))
		OfflineDatabase.startBackgroundUpdate(this, force)
	}

	private val offlineDataUpdatedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (OfflineDatabase.isPopulated(this@OfflineDataDownloadActivity)) {
				finish()
			} else {
				promptDownload()
			}
		}
	}

}
