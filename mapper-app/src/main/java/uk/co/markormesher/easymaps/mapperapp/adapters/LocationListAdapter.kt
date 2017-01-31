package uk.co.markormesher.easymaps.mapperapp.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item_location.view.*
import kotlinx.android.synthetic.main.list_item_location_intro.view.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.helpers.CircleCropTransformation
import uk.co.markormesher.easymaps.mapperapp.helpers.getTintedDrawable
import java.util.*

class LocationListAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	val TYPE_INTRO = 1
	val TYPE_LOCATION = 2

	val layoutInflater by lazy { LayoutInflater.from(context)!! }

	val locations = ArrayList<Location>()

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
		when (viewType) {
			TYPE_INTRO -> return IntroViewHolder(layoutInflater.inflate(R.layout.list_item_location_intro, parent, false))
			else -> return LocationViewHolder(layoutInflater.inflate(R.layout.list_item_location, parent, false))
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (getItemViewType(position)) {
			TYPE_INTRO -> {
				with(holder as IntroViewHolder) {
					val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
					val softTime = if (hour >= 20) {
						"tonight"
					} else if (hour >= 17) {
						"this evening"
					} else if (hour >= 12) {
						"this afternoon"
					} else {
						"this morning"
					}
					title.text = context.getString(R.string.select_location, softTime)
				}
			}

			else -> {
				val location = locations[position - 1]
				with(holder as LocationViewHolder) {
					title.text = location.title
					Picasso
							.with(context)
							.load(location.image)
							.transform(CircleCropTransformation())
							.placeholder(context.getTintedDrawable(R.drawable.ic_photo_camera_white_36dp, R.color.light_grey))
							.into(image)
				}
			}
		}
	}

	override fun getItemCount(): Int = locations.size + 1

	override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_INTRO else TYPE_LOCATION

	class IntroViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val title = v.title!!
	}

	class LocationViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val title = v.location_title!!
		val image = v.location_image!!
	}

}
