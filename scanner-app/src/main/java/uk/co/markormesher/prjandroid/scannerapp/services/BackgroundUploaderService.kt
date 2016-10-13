package uk.co.markormesher.prjandroid.scannerapp.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import uk.co.markormesher.prjandroid.scannerapp.LOG_TAG
import uk.co.markormesher.prjandroid.scannerapp.UPLOAD_INTERVAL
import uk.co.markormesher.prjandroid.scannerapp.getClosedScanResultsFiles

fun Context.setupAlarmForBackgroundUploaderService() {
	val pendingIntent = PendingIntent.getService(this, 0, Intent(this, BackgroundUploaderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
	val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
	alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, UPLOAD_INTERVAL, UPLOAD_INTERVAL, pendingIntent)
}

class BackgroundUploaderService : Service() {

	override fun onBind(intent: Intent?): IBinder? = null

	override fun onCreate() {
		super.onCreate()
		val filesToUpload = getClosedScanResultsFiles()
		Log.d(LOG_TAG, "There are ${filesToUpload.size} file(s) to upload")
		stopSelf()
	}
}