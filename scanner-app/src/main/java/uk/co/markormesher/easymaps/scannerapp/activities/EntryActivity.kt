package uk.co.markormesher.easymaps.scannerapp.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import uk.co.markormesher.easymaps.scannerapp.services.setupAlarmForBackgroundUploaderService
import uk.co.markormesher.easymaps.sdk.BaseActivity
import uk.co.markormesher.easymaps.sdk.checkPermissionList
import uk.co.markormesher.easymaps.sdk.checkPermissionRequestResult
import uk.co.markormesher.easymaps.sdk.requestPermissionList

class EntryActivity : BaseActivity() {

	val REQUIRED_PERMISSIONS = arrayOf(
			Manifest.permission.ACCESS_WIFI_STATE,
			Manifest.permission.CHANGE_WIFI_STATE,
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.RECEIVE_BOOT_COMPLETED,
			Manifest.permission.ACCESS_NETWORK_STATE,
			Manifest.permission.INTERNET
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (checkPermissionList(REQUIRED_PERMISSIONS)) {
			permissionsGrantedSuccessfully()
		} else {
			requestPermissionList(REQUIRED_PERMISSIONS)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		if (checkPermissionRequestResult(requestCode, grantResults)) {
			permissionsGrantedSuccessfully()
		} else {
			requestPermissionList(REQUIRED_PERMISSIONS, true)
		}
	}

	fun permissionsGrantedSuccessfully() {
		setupAlarmForBackgroundUploaderService()
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}
}