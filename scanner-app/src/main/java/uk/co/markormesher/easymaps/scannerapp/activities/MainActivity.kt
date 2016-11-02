package uk.co.markormesher.easymaps.scannerapp.activities

import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.markormesher.easymaps.scannerapp.*
import uk.co.markormesher.easymaps.scannerapp.services.ScannerService
import uk.co.markormesher.easymaps.sdk.BaseActivity
import uk.co.markormesher.easymaps.sdk.copyToClipboard
import uk.co.markormesher.easymaps.sdk.makeHtml
import uk.co.markormesher.easymaps.sdk.readDeviceID
import java.util.*

class MainActivity : BaseActivity(), ServiceConnection {

	private var scannerService: ScannerService? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		text1.text = makeHtml(R.string.intro_text)
		text2.text = makeHtml(R.string.intro_usage_text)
		text3.text = makeHtml(R.string.intro_collection_text)
		text4.text = makeHtml(R.string.intro_ps_text)

		toggle_scanning_button.setOnClickListener { sendBroadcast(Intent(getString(R.string.intent_toggle_scan))) }

		text1.setOnLongClickListener {
			displaySuperUserPrompt()
			true
		}

		checkNetwork()
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

	private fun checkNetwork() {
		if (getNetwork() == NO_NETWORK || getNetwork().isBlank()) {
			setNetwork(DEFAULT_NETWORK)
			Toast.makeText(this, getString(R.string.change_network_set_to, DEFAULT_NETWORK), Toast.LENGTH_SHORT).show()
		}
	}

	/* service binding */

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

			if (isSuperUser()) {
				messages.add(getString(R.string.su_enabled))
				if (isHighFrequencyMode()) {
					messages.add(getString(R.string.scan_status_high_freq_enabled))
				} else {
					messages.add(getString(R.string.scan_status_high_freq_disabled))
				}
			}

			val lifetimeDataPoints = scannerService!!.lifetimeDataPoints
			val sessionDataPoints = scannerService!!.sessionDataPoints

			if (scannerService!!.running) {
				messages.add(getString(R.string.scan_status_running))
				messages.add(getString(R.string.session_data_points_count, sessionDataPoints))
			} else {
				messages.add(getString(R.string.scan_status_stopped))
			}

			messages.add(getString(R.string.lifetime_data_points_count, lifetimeDataPoints))
			messages.add(getString(R.string.scan_status_network, getNetwork()))

			status_message.text = messages.joinToString("\n")
		}
	}

	/* options menu */

	private fun displaySuperUserPrompt() {
		val input = EditText(this)
		input.inputType = InputType.TYPE_CLASS_PHONE

		val alertBuilder = AlertDialog.Builder(this)
		with(alertBuilder) {
			setTitle(R.string.su_prompt_title)
			setMessage(R.string.su_prompt_body)
			setView(input)
			setCancelable(false)
			setPositiveButton(R.string.ok) { p0, p1 ->
				setIsSuperUser(input.text.toString() == SUPER_USER_PIN)
				invalidateOptionsMenu()
				updateStatusFromService()
			}
			create().show()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		if (isSuperUser()) {
			menuInflater.inflate(R.menu.activity_main_super_user, menu)
		} else {
			menuInflater.inflate(R.menu.activity_main, menu)
		}
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			R.id.user_id -> displayUserId()
			R.id.debug_report -> createDebugReport()
			R.id.contact -> startContact()
			R.id.high_freq -> {
				setIsHighFrequencyMode(!isHighFrequencyMode())
				updateStatusFromService()
			}
			R.id.change_network -> startNetworkChange()
		}
		return super.onOptionsItemSelected(item)
	}

	private fun displayUserId() {
		copyToClipboard(getString(R.string.debug_report_title), readDeviceID())
		Toast.makeText(this, R.string.user_id_copied, Toast.LENGTH_SHORT).show()

		val alertBuilder = AlertDialog.Builder(this)
		with(alertBuilder) {
			setTitle(R.string.user_id_title)
			setMessage(readDeviceID())
			setCancelable(false)
			setPositiveButton(R.string.ok, null)
			create().show()
		}
	}

	private fun getDebugMessage(): String {
		return "Android SDK: ${Build.VERSION.SDK_INT}\n" +
				"App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
				"Debug build: ${BuildConfig.DEBUG_MODE}\n" +
				"Super user: ${isSuperUser()}\n" +
				"High-frequency: ${isHighFrequencyMode()}\n" +
				"Files to upload: ${getClosedScanResultsFiles().size}\n" +
				"Last upload: ${getLastUploadTime()}\n" +
				"Last check: ${getLastUploadCheckTime()}"
	}

	private fun createDebugReport() {
		val message = getDebugMessage()

		copyToClipboard(getString(R.string.debug_report_title), message)
		Toast.makeText(this, R.string.debug_report_copied, Toast.LENGTH_SHORT).show()

		val alertBuilder = AlertDialog.Builder(this)
		with(alertBuilder) {
			setTitle(R.string.debug_report_title)
			setMessage(message)
			setCancelable(false)
			setPositiveButton(R.string.ok, null)
			create().show()
		}
	}

	private fun startContact() {
		val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", CONTACT_EMAIL, null))
		emailIntent.putExtra(Intent.EXTRA_EMAIL, CONTACT_EMAIL)
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
		emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_text, getDebugMessage()))
		startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_chooser_title)))
	}

	private fun startNetworkChange() {
		if (scannerService?.running ?: false) {
			Toast.makeText(this, R.string.change_network_error_scanner_running, Toast.LENGTH_SHORT).show()
			return
		}

		val input = EditText(this)
		input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
		input.setText(getNetwork())

		val alertBuilder = AlertDialog.Builder(this)
		with(alertBuilder) {
			setTitle(R.string.change_network_title)
			setView(input)
			setCancelable(false)
			setPositiveButton(R.string.ok) { p0, p1 ->
				val inputValue = input.text.toString().trim()

				if (inputValue.isEmpty()) {
					setNetwork(DEFAULT_NETWORK)
				} else if (!VALID_NETWORKS.contains(inputValue)) {
					Toast.makeText(this@MainActivity, R.string.change_network_invalid_input, Toast.LENGTH_SHORT).show()
				} else {
					setNetwork(inputValue)
					Toast.makeText(this@MainActivity, getString(R.string.change_network_set_to, inputValue), Toast.LENGTH_SHORT).show()
					updateStatusFromService()
				}
			}
			create().show()
		}
	}
}