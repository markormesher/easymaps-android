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

class LocationListAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	val TYPE_INTRO = 1
	val TYPE_LOCATION = 2

	val layoutInflater by lazy { LayoutInflater.from(context)!! }

	val locations = arrayOf(
			Location("Tube Station", "http://www.robwaller.org/blog/uploaded_images/tubenoriver-761093.jpg"),
			Location("The London Eye", "http://londonairportransfers.com/wp-content/uploads/2016/01/London-Eye-information.jpg"),
			Location("Big Ben", "https://cdn.getyourguide.com/niwziy2l9cvz/33fpoAXMDSoIYaWYygG4KE/4a121da4da937e3a9f0d4538c8606bac/london-bigben-1500x850.jpg"),
			Location("The Royal Observatory", "http://cdn.londonandpartners.com/asset/5b30f693aedad7721ae7d10a09ce921f.jpg"),
			Location("Sky Garden", "http://crazycowevents.com/wp-content/uploads/2015/06/Sky-Garden-Summer.jpg"),
			Location("Buckingham Palace", "http://cdn.londonandpartners.com/asset/buckingham-palace-tour-summer-opening-2015-ad00c5354eb7aff837932abb96167006.jpg"),
			Location("Trafalgar Square", "https://www.london.gov.uk/sites/default/files/styles/gla_2_1_large/public/business-t-square-5649.jpg?itok=w2EyyvyJ"),
			Location("Tower Bridge", "http://cdn.londonandpartners.com/asset/a9e3135930f74120799649654fec1a9f.jpg"),
			Location("Marble Arch", "https://i.ytimg.com/vi/JNl5JtcwRBA/maxresdefault.jpg"),
			Location("Hyde Park", "http://www.qualitycrown.com/assets/Uploads/italian-garden-hyde-park-london.jpg"),
			Location("Admiral's Nose", "http://www.aviewoncities.com/img/london/kveen2456s.jpg"),
			Location("Parliament Hill", "https://media-cdn.tripadvisor.com/media/photo-s/07/fc/17/f7/parliament-hill-fields.jpg"),
			Location("Somerset House", "http://cdn.londonandpartners.com/asset/c5441e5d79e0668e4db47cc9966cedc9.jpg")
	)

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
					name.text = location.name
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

	class IntroViewHolder(v: View) : RecyclerView.ViewHolder(v) {
		val title = v.title!!
	}

	class LocationViewHolder(v: View) : RecyclerView.ViewHolder(v) {
		val name = v.location_name!!
		val image = v.location_image!!
	}

}