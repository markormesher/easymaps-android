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
import uk.co.markormesher.easymaps.mapperapp.data.Connection
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.sdk.getLongPref
import uk.co.markormesher.easymaps.sdk.setLongPref
import java.io.IOException
import java.util.*

class DataDownloaderService: Service() {

	private var currentStep = 0

	private val httpClient by lazy { OkHttpClient() }

	private var localLabellingVersion = -1L
	private var localDataPackVersion = -1L
	private var serverLabellingVersion = -1L
	private var serverDataPackVersion = -1L

	private var labellingContent = ""
	private var dataPackContent = ""

	private var force = false

	companion object {
		val FORCE = "services.DataDownloaderService:FORCE"
		val LAST_SUCCESS = "services.DataDownloaderService:LAST_SUCCESS"

		private val MAX_DL_PART = 4
		private val DL_PART_LABELLING_VERSION = 1
		private val DL_PART_DATA_PACK_VERSION = 2
		private val DL_PART_LABELLING_CONTENT = 3
		private val DL_PART_DATA_PACK_CONTENT = 4

		private val MAX_SAVE_PART = 3
		private val SAVE_PART_LABELLING = 1
		private val SAVE_PART_LOCATIONS = 2
		private val SAVE_PART_CONNECTIONS = 3

		private val NOTIFICATION_ID = 12345678
	}

	override fun onBind(intent: Intent?): IBinder? = null

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		force = intent?.getBooleanExtra(FORCE, false) ?: false
		return super.onStartCommand(intent, flags, startId)
	}

	override fun onCreate() {
		super.onCreate()

		// run only if >24h since last successful check (or forced)
		val msSinceLastRun = System.currentTimeMillis() - getLongPref(LAST_SUCCESS, 0L)
		val msIn24Hours = 24 * 60 * 60 * 1000L
		if (!force && msSinceLastRun < msIn24Hours) {
			return finish()
		}

		// run only if online
		val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val activeNetworkInfo = connManager.activeNetworkInfo
		val connected = activeNetworkInfo?.isConnectedOrConnecting ?: false
		if (!connected) {
			return finish()
		} else {
			nextStep()
		}

		// get local versions
		localLabellingVersion = getLongPref(LATEST_LABELLING_VERSION_KEY)
		localDataPackVersion = getLongPref(LATEST_DATA_PACK_VERSION_KEY)
	}

	private fun nextStep() {
		++currentStep
		when (currentStep) {
			1 -> getLatestLabellingVersion()
			2 -> getLatestDataPackVersion()
			3 -> downloadLatestLabelling()
			4 -> downloadLatestDataPack()
			5 -> storeDownloadedLabelling()
			6 -> storeDownloadedDataPack()
			7 -> onSuccess()
			else -> finish()
		}
	}

	private fun getLatestLabellingVersion() {
		updateNotification(getString(R.string.downloading_offline_data_notification_title, DL_PART_LABELLING_VERSION, MAX_DL_PART))
		val request = Request.Builder().url("$API_ROOT/labellings/$NETWORK/stats").get().build()
		httpClient.newCall(request).enqueue(object: Callback {
			override fun onFailure(call: Call?, e: IOException?) = finish()
			override fun onResponse(call: Call?, response: Response?) {
				if (response?.isSuccessful ?: false) {
					try {
						serverLabellingVersion = JSONObject(response?.body()?.string()).getLong("latestVersion")
						nextStep()
					} catch (e: JSONException) {
						finish()
					}
				}
			}
		})
	}

	private fun getLatestDataPackVersion() {
		updateNotification(getString(R.string.downloading_offline_data_notification_title, DL_PART_DATA_PACK_VERSION, MAX_DL_PART))
		val request = Request.Builder().url("$API_ROOT/data-packs/$NETWORK/stats").get().build()
		httpClient.newCall(request).enqueue(object: Callback {
			override fun onFailure(call: Call?, e: IOException?) = finish()
			override fun onResponse(call: Call?, response: Response?) {
				if (response?.isSuccessful ?: false) {
					try {
						serverDataPackVersion = JSONObject(response?.body()?.string()).getLong("latestVersion")
						nextStep()
					} catch (e: JSONException) {
						finish()
					}
				}
			}
		})
	}

	private fun downloadLatestLabelling() {
		updateNotification(getString(R.string.downloading_offline_data_notification_title, DL_PART_LABELLING_CONTENT, MAX_DL_PART))
		if (localLabellingVersion >= serverLabellingVersion) {
			nextStep()
		} else {
			val request = Request.Builder().url("$API_ROOT/labellings/$NETWORK/latest").get().build()
			httpClient.newCall(request).enqueue(object: Callback {
				override fun onFailure(call: Call?, e: IOException?) = finish()
				override fun onResponse(call: Call?, response: Response?) {
					if (response?.isSuccessful ?: false) {
						labellingContent = response?.body()?.string() ?: return finish()
						nextStep()
					} else {
						finish()
					}
				}
			})
		}
	}

	private fun downloadLatestDataPack() {
		updateNotification(getString(R.string.downloading_offline_data_notification_title, DL_PART_DATA_PACK_CONTENT, MAX_DL_PART))
		if (localDataPackVersion >= serverDataPackVersion) {
			nextStep()
		} else {
			val request = Request.Builder().url("$API_ROOT/data-packs/$NETWORK/latest").get().build()
			httpClient.newCall(request).enqueue(object: Callback {
				override fun onFailure(call: Call?, e: IOException?) = finish()
				override fun onResponse(call: Call?, response: Response?) {
					if (response?.isSuccessful ?: false) {
						dataPackContent = response?.body()?.string() ?: return finish()
						nextStep()
					} else {
						finish()
					}
				}
			})
		}
	}

	private fun storeDownloadedLabelling() {
		updateNotification(getString(R.string.saving_offline_data_notification_title, SAVE_PART_LABELLING, MAX_SAVE_PART))

		// TODO: store labellings

		// pretend for now
		setLongPref(LATEST_LABELLING_VERSION_KEY, serverLabellingVersion)
		nextStep()
	}

	private fun storeDownloadedDataPack() {
		if (localDataPackVersion >= serverDataPackVersion) {
			return nextStep()
		}

		updateNotification(getString(R.string.saving_offline_data_notification_title_no_number))

		// try to parse data
		val locations = ArrayList<Location>()
		val connections = ArrayList<Connection>()
		try {
			val jsonRoot = JSONObject(dataPackContent)

			val jsonLocations = jsonRoot.getJSONObject("locations")
			jsonLocations.keys().forEach { id -> locations.add(Location.fromJson(id, jsonLocations.getJSONObject(id))) }

			val jsonConnections = jsonRoot.getJSONArray("connections")
			(0..jsonConnections.length() - 1).mapTo(connections) { c -> Connection.fromJson(jsonConnections.getJSONObject(c)) }

		} catch (e: JSONException) {
			e.printStackTrace()
			return finish()
		}

		// add to DB if parsing was successful
		val db = OfflineDatabase(this)
		db.updateLocations(locations, { done ->
			updateNotification(
					getString(R.string.saving_offline_data_notification_title, SAVE_PART_LOCATIONS, MAX_SAVE_PART),
					done, locations.size
			)
		})
		db.updateConnections(connections, { done ->
			updateNotification(
					getString(R.string.saving_offline_data_notification_title, SAVE_PART_CONNECTIONS, MAX_SAVE_PART),
					done, connections.size
			)
		})

		// save version
		setLongPref(LATEST_DATA_PACK_VERSION_KEY, serverDataPackVersion)

		nextStep()
	}

	private fun onSuccess() {
		setLongPref(LAST_SUCCESS, System.currentTimeMillis())
		nextStep()
	}

	private fun finish() {
		sendBroadcast(Intent(OfflineDatabase.STATE_UPDATED))
		stopForeground(true)
		clearNotification()
		stopSelf()
	}

	private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

	private fun updateNotification(message: String, done: Int = 0, max: Int = 1) {
		with(NotificationCompat.Builder(this)) {
			setContentTitle(message)
			setProgress(max, done, done == 0)
			setSmallIcon(R.drawable.ic_mapper_app_white)
			startForeground(NOTIFICATION_ID, build())
		}
	}

	private fun clearNotification() {
		notificationManager.cancel(NOTIFICATION_ID)
	}
}
