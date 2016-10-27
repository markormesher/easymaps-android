package uk.co.markormesher.easymaps.mapperapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		navigation_drawer.adapter = NavAdapter(this)
		navigation_drawer.setOnItemClickListener { adapterView, view, position, id ->
			drawer_layout.closeDrawers()
			when (view.id) {
				NAV_HOME -> Toast.makeText(this, "Home!", Toast.LENGTH_SHORT).show()
				NAV_ABOUT -> Toast.makeText(this, "About!", Toast.LENGTH_SHORT).show()
				NAV_SETTINGS -> Toast.makeText(this, "Settings!", Toast.LENGTH_SHORT).show()
			}
		}
	}
}

