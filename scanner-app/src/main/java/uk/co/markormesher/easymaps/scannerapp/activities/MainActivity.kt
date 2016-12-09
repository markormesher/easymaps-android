package uk.co.markormesher.easymaps.scannerapp.activities

import android.content.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import uk.co.markormesher.easymaps.scannerapp.*
import uk.co.markormesher.easymaps.scannerapp.BuildConfig
import uk.co.markormesher.easymaps.scannerapp.R
import uk.co.markormesher.easymaps.scannerapp.services.ScannerService
import uk.co.markormesher.easymaps.sdk.*
import java.io.IOException
import java.util.*

class MainActivity: BaseActivity(), ServiceConnection {

	private var scannerService: ScannerService? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		toggle_scanning_button.setOnClickListener {
			if (checkSettings()) {
				sendBroadcast(Intent(getString(R.string.intent_toggle_scan)))
			}
		}

		ethics_reference.setOnLongClickListener {
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

		checkSettings()
	}

	override fun onPause() {
		super.onPause()
		unregisterReceiver(scanStateUpdatedReceiver)
		settingCheckHandler.removeCallbacks(settingCheckRunnable)
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

	private val scanStateUpdatedReceiver = object: BroadcastReceiver() {
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

			if (isSuperUser()) messages.add(getString(R.string.su_enabled))

			val lifetimeDataPoints = scannerService!!.lifetimeDataPoints
			val sessionDataPoints = scannerService!!.sessionDataPoints

			if (scannerService!!.running) {
				messages.add(getString(R.string.scan_status_running))
				messages.add(getString(R.string.session_data_points_count, sessionDataPoints))
			} else {
				messages.add(getString(R.string.scan_status_stopped))
			}

			messages.add(getString(R.string.lifetime_data_points_count, lifetimeDataPoints))
			messages.add(getString(R.string.scan_status_interval, getScanInterval()))
			if (isSuperUser()) messages.add(getString(R.string.scan_status_network, getNetwork()))

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
			R.id.scan_interval -> changeScanInterval()
			R.id.withdraw -> startWithdrawal()
			R.id.change_network -> changeNetwork()
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
			setCancelable(true)
			setPositiveButton(R.string.ok, null)
			create().show()
		}
	}

	private fun getDebugMessage(): String {
		return "Android SDK: ${Build.VERSION.SDK_INT}\n" +
				"App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
				"Debug build: ${BuildConfig.DEBUG_MODE}\n" +
				"Network: ${getNetwork()}\n" +
				"Scan interval: ${getScanInterval()}\n" +
				"Super user: ${isSuperUser()}\n" +
				"Files to upload: ${getClosedScanResultsFiles().size}\n" +
				"Last upload: ${getLastUploadTime()}\n" +
				"Last check: ${getLastUploadCheckTime()}"
	}

	private fun createDebugReport() {
		val message = getDebugMessage()

		copyToClipboard(getString(R.string.debug_report_title), message)
		Toast.makeText(this, R.string.debug_report_copied, Toast.LENGTH_SHORT).show()

		val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", CONTACT_EMAIL, null))
		emailIntent.putExtra(Intent.EXTRA_EMAIL, CONTACT_EMAIL)
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.debug_report_title))
		emailIntent.putExtra(Intent.EXTRA_TEXT, message)
		startActivity(Intent.createChooser(emailIntent, getString(R.string.debug_report_chooser_title)))
	}

	private fun startContact() {
		val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", CONTACT_EMAIL, null))
		emailIntent.putExtra(Intent.EXTRA_EMAIL, CONTACT_EMAIL)
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
		emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_text, getDebugMessage()))
		startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_chooser_title)))
	}

	private fun changeScanInterval() {
		val numberPicker = NumberPicker(this)
		with(numberPicker) {
			minValue = MIN_SCAN_INTERVAL
			maxValue = MAX_SCAN_INTERVAL
			value = getScanInterval()
			wrapSelectorWheel = false
			displayedValues = (MIN_SCAN_INTERVAL..MAX_SCAN_INTERVAL).map({ i -> "$i seconds" }).toTypedArray()
			descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
		}

		val alertBuilder = AlertDialog.Builder(this)
		with(alertBuilder) {
			setTitle(R.string.change_scan_interval_title)
			setMessage(R.string.change_scan_interval_body)
			setView(numberPicker)
			setCancelable(true)
			setPositiveButton(R.string.ok, { p0, p1 ->
				setScanInterval(numberPicker.value)
				updateStatusFromService()
			})
			create().show()
		}
	}

	private fun startWithdrawal() {
		val alertBuilder = AlertDialog.Builder(this)
		with(alertBuilder) {
			setTitle(R.string.withdraw_data_confirm_title)
			setMessage(makeHtml(R.string.withdraw_data_confirm_body))
			setCancelable(false)
			setPositiveButton(R.string.yes) { p0, p1 ->
				val requestBody = FormBody.Builder().add("userId", readDeviceID()).build()
				val request = Request.Builder().url(WITHDRAW_URL).post(requestBody).build()
				OkHttpClient().newCall(request).enqueue(object: Callback {
					override fun onFailure(call: Call?, e: IOException?) = runOnUiThread {
						Toast.makeText(this@MainActivity, R.string.withdraw_failure, Toast.LENGTH_SHORT).show()
					}

					override fun onResponse(call: Call?, response: Response?) = runOnUiThread {
						if (response?.isSuccessful ?: false) {
							Toast.makeText(this@MainActivity, R.string.withdraw_success, Toast.LENGTH_SHORT).show()
						} else {
							Toast.makeText(this@MainActivity, R.string.withdraw_failure, Toast.LENGTH_SHORT).show()
						}
					}
				})
			}
			setNegativeButton(R.string.no) { p0, p1 -> }
			create().show()
		}
	}

	private fun changeNetwork() {
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

	/* settings watchers */

	private val settingCheckHandler = Handler(Looper.getMainLooper())
	private val settingCheckRunnable = Runnable { checkSettings() }

	private fun checkSettings(): Boolean {
		var result = true

		if (!deviceLocationEnabled()) {
			setting_warning.text = getString(R.string.setting_warning_location)
			setting_warning_note.text = getString(R.string.setting_warning_location_note)
			setting_warning_note.visibility = View.VISIBLE
			setting_warning_button.setOnClickListener { startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }

			setting_warning_button_wrapper.visibility = View.VISIBLE
			toggle_scanning_button_wrapper.visibility = View.GONE
			result = false

		} else if (!deviceWifiScanningEnabled()) {
			setting_warning.text = getString(R.string.setting_warning_wifi)
			setting_warning_note.visibility = View.GONE
			setting_warning_button.setOnClickListener { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }

			setting_warning_button_wrapper.visibility = View.VISIBLE
			toggle_scanning_button_wrapper.visibility = View.GONE
			result = false

		} else {
			setting_warning_button_wrapper.visibility = View.GONE
			toggle_scanning_button_wrapper.visibility = View.VISIBLE
		}

		settingCheckHandler.removeCallbacks(settingCheckRunnable)
		settingCheckHandler.postDelayed(settingCheckRunnable, 5000)

		return result
	}

}
