package uk.co.markormesher.prjandroid.scannerapp.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_scanner.*
import kotlinx.android.synthetic.main.list_item_scan_result.*
import uk.co.markormesher.prjandroid.scannerapp.R
import uk.co.markormesher.prjandroid.sdk.WifiScanResult
import uk.co.markormesher.prjandroid.sdk.WifiScanner
import java.util.*

class ScannerActivity : AppCompatActivity() {

	val statusUpdateHandler by lazy { Handler(Looper.getMainLooper()) }
	val statusUpdateRunnable by lazy { Runnable { updateScanStatus() } }
	val macAddressListAdapter by lazy { MacAddressListAdapter() }

	val scanResultReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val latestResults = WifiScanner.scanResults
			with(macAddressListAdapter.scanResults) {
				clear()
				addAll(latestResults)
				Collections.sort(this, { r1, r2 -> r1.mac.compareTo(r2.mac) })
			}
			macAddressListAdapter.notifyDataSetChanged()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_scanner)
		mac_address_list.adapter = macAddressListAdapter
	}

	override fun onResume() {
		super.onResume()
		registerReceiver(scanResultReceiver, IntentFilter(WifiScanner.INTENT_SCAN_RESULTS_UPDATED))
		WifiScanner.start(this, 10000)
		updateScanStatus()
	}

	override fun onPause() {
		super.onPause()
		unregisterReceiver(scanResultReceiver)
		WifiScanner.stop(this)
		statusUpdateHandler.removeCallbacks(statusUpdateRunnable)
	}

	fun updateScanStatus() {
		if (WifiScanner.lastScan < 0) {
			scan_status.text = "Waiting for first scan..."
		} else {
			val secsAgo = (System.currentTimeMillis() - WifiScanner.lastScan).toInt() / 1000
			scan_status.text = "Last scan: $secsAgo sec${if (secsAgo == 1) "" else "s"} ago"
		}
		if (WifiScanner.running) statusUpdateHandler.postDelayed(statusUpdateRunnable, 200)
	}

	inner class MacAddressListAdapter : BaseAdapter() {

		val scanResults = ArrayList<WifiScanResult>()

		override fun getCount(): Int {
			return scanResults.size
		}

		override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
			var view = convertView
			if (view == null) view = this@ScannerActivity.layoutInflater.inflate(R.layout.list_item_scan_result, parent, false)

			val result = getItem(position) as WifiScanResult
			mac_address.text = result.mac
			wifi_name.text = result.ssid
			return view
		}

		override fun getItem(position: Int): Any {
			return scanResults[position]
		}

		override fun getItemId(position: Int): Long {
			return 0 // unused
		}
	}
}