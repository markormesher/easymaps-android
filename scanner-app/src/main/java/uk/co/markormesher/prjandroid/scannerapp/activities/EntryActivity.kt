package uk.co.markormesher.prjandroid.scannerapp.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import uk.co.markormesher.prjandroid.sdk.checkPermissionList
import uk.co.markormesher.prjandroid.sdk.checkPermissionRequestResult
import uk.co.markormesher.prjandroid.sdk.requestPermissionList

class EntryActivity : AppCompatActivity() {

	val REQUIRED_PERMISSIONS = arrayOf(
			Manifest.permission.ACCESS_WIFI_STATE,
			Manifest.permission.CHANGE_WIFI_STATE,
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION
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
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}
}