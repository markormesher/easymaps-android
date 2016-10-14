package uk.co.markormesher.easymaps.scannerapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import uk.co.markormesher.easymaps.scannerapp.services.setupAlarmForBackgroundUploaderService

class BootReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		context.setupAlarmForBackgroundUploaderService()
	}
}