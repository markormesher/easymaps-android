package uk.co.markormesher.easymaps.sdk

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import uk.co.chrisjenx.calligraphy.R

open class BaseActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// note: the actual fonts are in the app projects, not the SDK one, because
		// assets can't be access from outside the SDK.
		CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/Lato-Regular.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build())
	}

	override fun attachBaseContext(newBase: Context?) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
	}
}