package uk.co.markormesher.easymaps.mapperapp.activities

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.adapters.LocationListAdapter
import uk.co.markormesher.easymaps.sdk.BaseActivity

class MainActivity : BaseActivity() {

	val locationListAdapter by lazy { LocationListAdapter(this) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val gridLayoutManager = GridLayoutManager(this, 3) // TODO: change based on screen width
		gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
			override fun getSpanSize(position: Int): Int = if (position == 0) 3 else 1
		}
		location_grid.layoutManager = gridLayoutManager
		location_grid.adapter = locationListAdapter
	}
}

