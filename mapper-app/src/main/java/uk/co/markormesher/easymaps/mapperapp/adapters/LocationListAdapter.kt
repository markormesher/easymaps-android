package uk.co.markormesher.easymaps.mapperapp.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item_attraction.view.*
import kotlinx.android.synthetic.main.list_item_attractions_intro.view.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.helpers.CircleCropTransformation
import uk.co.markormesher.easymaps.mapperapp.helpers.getTintedDrawable
import java.util.*

class LocationListAdapter(val context: Context, val clickListener: OnClickListener? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	companion object {
		private val SEARCH_BG = "https://raw.githubusercontent.com/markormesher/easymaps-data-packs/master/london-images/search-bg.png"

		val TYPE_INTRO = 1
		val TYPE_SEARCH = 2
		val TYPE_ATTRACTION = 3
	}

	private val layoutInflater by lazy { LayoutInflater.from(context)!! }

	val attractions = ArrayList<Location>()

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
		when (viewType) {
			TYPE_INTRO -> return IntroViewHolder(layoutInflater.inflate(R.layout.list_item_attractions_intro, parent, false))
			else -> return AttractionViewHolder(layoutInflater.inflate(R.layout.list_item_attraction, parent, false))
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (getItemViewType(position)) {
			TYPE_INTRO -> with(holder as IntroViewHolder) {
				holder.rootView.setOnClickListener { clickListener?.onLocationClick(TYPE_INTRO) }
				val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
				val softTime = if (hour >= 20) {
					context.getString(R.string.select_attraction_suffix_night)
				} else if (hour >= 17) {
					context.getString(R.string.select_attraction_suffix_evening)
				} else if (hour >= 12) {
					context.getString(R.string.select_attraction_suffix_afternoon)
				} else {
					context.getString(R.string.select_attraction_suffix_morning)
				}
				title.text = context.getString(R.string.select_attraction, softTime)
			}

			TYPE_SEARCH -> with(holder as AttractionViewHolder) {
				holder.rootView.setOnClickListener { clickListener?.onLocationClick(TYPE_SEARCH) }
				title.text = context.getString(R.string.attraction_list_search)
				icon.setImageResource(R.drawable.ic_search_white_48dp)
				Picasso
						.with(context)
						.load(SEARCH_BG)
						.transform(CircleCropTransformation())
						.into(image)
			}

			TYPE_ATTRACTION -> with(holder as AttractionViewHolder) {
				val attraction = attractions[position - 2]
				holder.rootView.setOnClickListener { clickListener?.onLocationClick(TYPE_ATTRACTION, attraction) }
				title.text = attraction.title
				icon.setImageDrawable(null)
				Picasso
						.with(context)
						.load(attraction.image)
						.transform(CircleCropTransformation())
						.placeholder(context.getTintedDrawable(R.drawable.ic_photo_camera_white_36dp, R.color.light_grey))
						.into(image)
			}
		}
	}

	// plus one for intro, plus one for search
	override fun getItemCount(): Int = attractions.size + 2

	override fun getItemViewType(position: Int): Int = when (position) {
		0 -> TYPE_INTRO
		1 -> TYPE_SEARCH
		else -> TYPE_ATTRACTION
	}

	class IntroViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val rootView = v
		val title = v.title!!
	}

	class AttractionViewHolder(v: View): RecyclerView.ViewHolder(v) {
		val rootView = v
		val title = v.location_title!!
		val image = v.location_image!!
		val icon = v.icon!!
	}

	interface OnClickListener {
		fun onLocationClick(type: Int, location: Location? = null)
	}

}
