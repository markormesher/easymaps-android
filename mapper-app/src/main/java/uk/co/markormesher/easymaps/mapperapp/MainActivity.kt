package uk.co.markormesher.easymaps.mapperapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		setSupportActionBar(toolbar)
		supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_menu_white_24dp))
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setDisplayShowTitleEnabled(false)
		toolbar.setNavigationOnClickListener { drawer_layout.openDrawer(Gravity.START) }
	}
}

