package uk.co.markormesher.easymaps.mapperapp.services

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import uk.co.markormesher.easymaps.mapperapp.*
import uk.co.markormesher.easymaps.sdk.getLongPref
import java.io.IOException

class DataDownloaderService: Service() {

	val httpClient by lazy { OkHttpClient() }

	var localLabellingVersion = -1L
	var localDataPackVersion = -1L

	var serverLabellingVersion = -1L
	var serverDataPackVersion = -1L

	var labellingContent = ""
	var dataPackContent = ""

	override fun onBind(intent: Intent?): IBinder? = null

	override fun onCreate() {
		super.onCreate()

		// get initial versions
		localLabellingVersion = getLongPref(LAST_LABELLING_VERSION_KEY)
		localDataPackVersion = getLongPref(LAST_DATA_PACK_VERSION_KEY)

		// run only if online
		val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val activeNetworkInfo = connManager.activeNetworkInfo
		val connected = activeNetworkInfo?.isConnectedOrConnecting ?: false
		if (!connected) {
			finish()
		} else {
			runStage(1)
		}
	}

	private fun runStage(stage: Int) {
		when (stage) {
			1 -> { // get latest labelling version
				updateNotification(getString(R.string.labelling_check_notification_title))
				val request = Request.Builder().url("$API_ROOT/labellings/$NETWORK/stats").get().build()
				httpClient.newCall(request).enqueue(object: Callback {
					override fun onFailure(call: Call?, e: IOException?) = finish()

					override fun onResponse(call: Call?, response: Response?) {
						if (response?.isSuccessful ?: false) {
							try {
								serverLabellingVersion = JSONObject(response?.body()?.string()).getLong("latestVersion")
								runStage(2)
							} catch (e: JSONException) {
								finish()
							}
						}
					}
				})
			}

			2 -> { // get latest data pack version
				updateNotification(getString(R.string.data_pack_check_notification_title))
				val request = Request.Builder().url("$API_ROOT/data-packs/$NETWORK/stats").get().build()
				httpClient.newCall(request).enqueue(object: Callback {
					override fun onFailure(call: Call?, e: IOException?) = finish()

					override fun onResponse(call: Call?, response: Response?) {
						if (response?.isSuccessful ?: false) {
							try {
								serverDataPackVersion = JSONObject(response?.body()?.string()).getLong("latestVersion")
								runStage(3)
							} catch (e: JSONException) {
								finish()
							}
						}
					}
				})
			}

			3 -> { // get latest labelling, if needed
				updateNotification(getString(R.string.labelling_download_notification_title))
				if (localLabellingVersion < serverLabellingVersion) {
					val request = Request.Builder().url("$API_ROOT/labellings/$NETWORK/latest").get().build()
					httpClient.newCall(request).enqueue(object: Callback {
						override fun onFailure(call: Call?, e: IOException?) = finish()

						override fun onResponse(call: Call?, response: Response?) {
							if (response?.isSuccessful ?: false) {
								labellingContent = response?.body()?.string() ?: return finish()
								runStage(4)
							} else {
								finish()
							}
						}
					})
				} else {
					runStage(4)
				}
			}

			4 -> { // get latest data pack, if needed
				updateNotification(getString(R.string.data_pack_download_notification_title))
				if (localLabellingVersion < serverLabellingVersion) {
					val request = Request.Builder().url("$API_ROOT/data-packs/$NETWORK/latest").get().build()
					httpClient.newCall(request).enqueue(object: Callback {
						override fun onFailure(call: Call?, e: IOException?) = finish()

						override fun onResponse(call: Call?, response: Response?) {
							if (response?.isSuccessful ?: false) {
								dataPackContent = response?.body()?.string() ?: return finish()
								runStage(5)
							} else {
								finish()
							}
						}
					})
				} else {
					runStage(5)
				}
			}

			5 -> { // save downloaded data
				updateNotification(getString(R.string.data_pack_download_notification_title))
				// TODO: write data to store
				finish()
			}

			else -> finish()
		}
	}

	private fun finish() {
		sendBroadcast(Intent(getString(R.string.intent_offline_data_updated)))
		stopForeground(true)
		notificationManager.cancel(NOTIFICATION_ID)
		stopSelf()
	}

	private val NOTIFICATION_ID = 61193
	private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

	private fun updateNotification(message: String) {
		val nBuilder = NotificationCompat.Builder(this)
		with(nBuilder) {
			setContentTitle(message)
			setProgress(1, 0, true)
			setSmallIcon(R.drawable.ic_mapper_app_white)
		}
		startForeground(NOTIFICATION_ID, nBuilder.build())
	}
}
