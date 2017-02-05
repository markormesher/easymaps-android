package uk.co.markormesher.easymaps.mapperapp.activities

import android.os.Bundle
import android.widget.TextView
import uk.co.markormesher.easymaps.sdk.BaseActivity

class RoutePlanningActivity: BaseActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val extras = intent.extras

		val tv = TextView(this)
		tv.text = "We're going to... ${extras.getString("DESTINATION", "Literally no idea")}"

		setContentView(tv)
	}
}
