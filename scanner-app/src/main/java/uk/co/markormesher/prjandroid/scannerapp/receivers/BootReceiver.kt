package uk.co.markormesher.prjandroid.scannerapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import uk.co.markormesher.prjandroid.scannerapp.services.setupAlarmForBackgroundUploaderService

class BootReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		context.setupAlarmForBackgroundUploaderService()
	}
}