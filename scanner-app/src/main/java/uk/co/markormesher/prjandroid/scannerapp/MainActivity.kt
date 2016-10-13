package uk.co.markormesher.prjandroid.scannerapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.markormesher.prjandroid.sdk.makeHtml

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// TODO: bind to service

		text1.text = makeHtml(R.string.intro_text)
		text2.text = makeHtml(R.string.intro_collection_text)
		text3.text = makeHtml(R.string.intro_usage_text)
		text4.text = makeHtml(R.string.intro_ps_text)
		updateStatusMessage()

		// TODO: button click listener
	}

	fun updateStatusMessage() {
		// TODO: get status from service binding and update periodically
		statusMessage.text = makeHtml(R.string.scan_status_stopped)
	}
}