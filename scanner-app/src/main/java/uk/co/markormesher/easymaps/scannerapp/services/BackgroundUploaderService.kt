package uk.co.markormesher.easymaps.scannerapp.services

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import okhttp3.*
import uk.co.markormesher.easymaps.scannerapp.*
import uk.co.markormesher.easymaps.sdk.readDeviceID
import java.io.File
import java.io.IOException

fun Context.setupAlarmForBackgroundUploaderService() {
	val pendingIntent = PendingIntent.getService(this, 0, Intent(this, BackgroundUploaderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
	val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
	alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, UPLOAD_INTERVAL, UPLOAD_INTERVAL, pendingIntent)
}

class BackgroundUploaderService : Service() {

	private var filesToUpload: List<File>? = null
	private var filesFinished = 0

	override fun onBind(intent: Intent?): IBinder? = null

	override fun onCreate() {
		super.onCreate()

		// store last check date
		setLastUploadCheckTime()

		// check whether we're online
		val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val activeNetworkInfo = connManager.activeNetworkInfo
		val connected = activeNetworkInfo?.isConnectedOrConnecting ?: false
		if (!connected) {
			Log.d(LOG_TAG, "No connection - aborting file upload")
			return finishUploadingFiles()
		} else if (activeNetworkInfo?.type != ConnectivityManager.TYPE_WIFI) {
			Log.d(LOG_TAG, "Connected, but not WiFi - aborting file upload")
			return finishUploadingFiles()
		}

		// check whether there's any work to do
		filesToUpload = getClosedScanResultsFiles()
		Log.d(LOG_TAG, "${filesToUpload?.size} file(s) to upload!")
		if (filesToUpload?.isNotEmpty() ?: false) {
			startUploadingFiles()
		} else {
			finishUploadingFiles()
		}
	}

	private fun startUploadingFiles() {
		updateNotification()
		uploadFile(0)
	}

	private fun uploadFile(index: Int) {
		if (index >= (filesToUpload?.size ?: 0)) return finishUploadingFiles()
		val file = filesToUpload?.get(index) ?: return finishUploadingFiles()
		val requestBody = MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("userId", readDeviceID())
				.addFormDataPart("network", "london") // TODO: network selection
				.addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("text/plain"), file))
				.build()
		val request = Request.Builder().url(UPLOAD_URL).post(requestBody).build()
		OkHttpClient().newCall(request).enqueue(object : Callback {
			override fun onFailure(call: Call?, e: IOException?) = uploadNextFile(index)

			override fun onResponse(call: Call?, response: Response?) {
				if (response?.isSuccessful ?: false) {
					file.delete()
					this@BackgroundUploaderService.setLastUploadTime()
				}
				uploadNextFile(index)
			}
		})
	}

	private fun uploadNextFile(index: Int) {
		++filesFinished
		updateNotification()
		uploadFile(index + 1)
	}

	private fun finishUploadingFiles() {
		stopForeground(true)
		notificationManager.cancel(NOTIFICATION_ID)
		stopSelf()
	}

	private val NOTIFICATION_ID = 61193
	private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

	private fun updateNotification() {
		val nBuilder = NotificationCompat.Builder(this)
		with(nBuilder) {
			setContentTitle(getString(R.string.uploading_notification_title))
			setProgress(filesToUpload?.size ?: 0, filesFinished, false)
			setSmallIcon(R.mipmap.ic_launcher) // TODO: app icon
		}
		startForeground(NOTIFICATION_ID, nBuilder.build())
	}
}