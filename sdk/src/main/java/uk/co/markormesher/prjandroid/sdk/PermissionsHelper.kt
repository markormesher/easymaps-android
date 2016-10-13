package uk.co.markormesher.prjandroid.sdk

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

private val PERMISSION_REQUEST_CODE = 1415

private fun shouldUseRuntimePermissions(): Boolean {
	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

private fun hasPermission(context: Context, permission: String): Boolean {
	return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.checkPermissionList(permissions: Array<String>): Boolean {
	if (!shouldUseRuntimePermissions()) return true
	for (perm in permissions) {
		if (!hasPermission(this, perm)) return false
	}
	return true
}

fun Activity.requestPermissionList(permissions: Array<String>, reRequest: Boolean = false) {
	val alertBuilder = AlertDialog.Builder(this)
	with(alertBuilder) {
		setTitle(R.string.permissions_request_title)
		if (reRequest) {
			setMessage(R.string.permissions_request_failure)
			setPositiveButton(R.string.yes) { p0, p1 -> doRequestPermissionList(permissions) }
			setNegativeButton(R.string.no) { p0, p1 -> finish() }
		} else {
			setMessage(R.string.permissions_request_primer)
			setPositiveButton(R.string.ok) { p0, p1 -> doRequestPermissionList(permissions) }
		}
		create().show()
	}
}

private fun Activity.doRequestPermissionList(permissions: Array<String>) {
	ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
}

fun checkPermissionRequestResult(requestCode: Int, grantResults: IntArray): Boolean {
	if (requestCode != PERMISSION_REQUEST_CODE) return false
	if (grantResults.isEmpty()) return false
	for (result in grantResults) {
		if (result != PackageManager.PERMISSION_GRANTED) return false
	}
	return true
}