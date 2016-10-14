package uk.co.markormesher.easymaps.scannerapp.activities

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.markormesher.easymaps.scannerapp.R
import uk.co.markormesher.easymaps.scannerapp.services.ScannerService
import uk.co.markormesher.easymaps.sdk.makeHtml
import uk.co.markormesher.easymaps.sdk.readDeviceID
import java.util.*

class MainActivity : AppCompatActivity(), ServiceConnection {

	private var scannerService: ScannerService? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		text1.text = makeHtml(R.string.intro_text)
		text2.text = makeHtml(R.string.intro_usage_text)
		text3.text = makeHtml(R.string.intro_collection_text)
		text4.text = makeHtml(R.string.intro_ps_text)
		text5.text = makeHtml(R.string.intro_anon_id, readDeviceID().replace("-", "-\u200b"))

		text5.setOnClickListener {
			val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
			clipboard.primaryClip = ClipData.newPlainText(getString(R.string.copy_id_label), readDeviceID())
			Toast.makeText(this, R.string.copy_id_done, Toast.LENGTH_SHORT).show()
		}
		toggle_scanning_button.setOnClickListener { sendBroadcast(Intent(getString(R.string.intent_toggle_scan))) }
	}

	override fun onResume() {
		super.onResume()

		val serviceIntent = Intent(baseContext, ScannerService::class.java)
		baseContext.startService(serviceIntent)
		baseContext.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)

		registerReceiver(scanStateUpdatedReceiver, IntentFilter(getString(R.string.intent_scan_status_updated)))

		updateStatusFromService()
	}

	override fun onPause() {
		super.onPause()
		unregisterReceiver(scanStateUpdatedReceiver)
	}

	override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
		if (binder is ScannerService.LocalBinder) {
			scannerService = binder.getScannerService()
			updateStatusFromService()
		}
	}

	override fun onServiceDisconnected(name: ComponentName?) {
		scannerService = null
	}

	private val scanStateUpdatedReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			updateStatusFromService()
		}
	}

	private fun updateStatusFromService() {
		toggle_scanning_button.isEnabled = scannerService != null
		toggle_scanning_button.text = getString(if (scannerService?.running ?: false) R.string.scan_toggle_stop else R.string.scan_toggle_start)

		if (scannerService == null) {
			status_message.text = makeHtml(R.string.scan_status_unknown)
		} else {
			val messages = ArrayList<String>()
			val lifetimeDataPoints = scannerService!!.lifetimeDataPoints
			val sessionDataPoints = scannerService!!.sessionDataPoints
			if (scannerService!!.running) {
				messages.add(getString(R.string.scan_status_running))
				messages.add(getString(R.string.session_data_points_count, sessionDataPoints))
			} else {
				messages.add(getString(R.string.scan_status_stopped))
			}
			messages.add(getString(R.string.lifetime_data_points_count, lifetimeDataPoints))

			status_message.text = messages.joinToString("\n")
		}
	}
}