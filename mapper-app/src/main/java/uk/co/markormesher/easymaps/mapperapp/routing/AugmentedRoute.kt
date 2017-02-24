package uk.co.markormesher.easymaps.mapperapp.routing

import android.content.Context
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.TravelMode
import uk.co.markormesher.easymaps.mapperapp.helpers.oneRandom
import java.util.*

class AugmentedRoute(val route: Route, val context: Context) {

	val stages = ArrayList<Stage>(route.locations.size)

	private val modeEndIndexes = IntArray(route.locations.size, { -1 })

	init {
		computeModeIndexes()
		for (i in 0..route.locations.size - 1) {
			val location = route.locations[i]

			val modeEndIndex = modeEndIndexes[i]
			val modeEndLocation = if (modeEndIndex >= 0) route.locations[modeEndIndex] else null

			val prevMode = if (i > 0) route.modes[i - 1] else null
			val nextMode = if (i < route.modes.size) route.modes[i] else null

			val instruction: String
			var isMajor = false

			if (prevMode == null) { // start of route
				if (nextMode == null) {
					throw IllegalStateException("Switching from $prevMode to $nextMode")

				} else if (nextMode == TravelMode.WALK) {
					instruction = context.getString(R.string.route_guidance_walk_to, modeEndLocation?.getDisplayTitle(context))
					isMajor = true

				} else if (nextMode.isTube()) {
					instruction = context.getString(R.string.route_guidance_tube_to, nextMode.displayName, modeEndLocation?.getDisplayTitle(context))
					isMajor = true

				} else {
					throw IllegalStateException("Switching from $prevMode to $nextMode")
				}

			} else if (nextMode == null) { // end of route
				instruction = context.resources.getStringArray(R.array.route_guidance_destination).oneRandom()
				isMajor = true

			} else if (prevMode == TravelMode.WALK) { // mid-route, transition from walk
				if (nextMode.isTube()) {
					instruction = context.getString(R.string.route_guidance_tube_to, nextMode.displayName, modeEndLocation?.getDisplayTitle(context))
					isMajor = true

				} else {
					throw IllegalStateException("Switching from $prevMode to $nextMode")
				}

			} else if (prevMode.isTube()) { // mid-route, transition from tube
				if (nextMode == TravelMode.WALK) { // switch to walking
					instruction = context.getString(R.string.route_guidance_walk_to, modeEndLocation?.getDisplayTitle(context))
					isMajor = true

				} else if (prevMode == nextMode) { // same tube line
					instruction = context.getString(R.string.route_guidance_continue_on, prevMode.displayName, modeEndLocation?.getDisplayTitle(context))

				} else if (nextMode.isTube()) { // different tube line
					instruction = context.getString(R.string.route_guidance_tube_to, nextMode.displayName, modeEndLocation?.getDisplayTitle(context))
					isMajor = true

				} else {
					throw IllegalStateException("Switching from $prevMode to $nextMode")
				}

			} else {
				throw IllegalStateException("Invalid route!")
			}

			stages.add(Stage(location, prevMode, nextMode, instruction, isMajor))
		}
	}

	private fun computeModeIndexes() {
		modeEndIndexes[route.locations.size - 1] = -1
		modeEndIndexes[route.locations.size - 2] = route.locations.size - 1
		if (route.locations.size > 2) {
			for (i in route.locations.size - 3 downTo 0) {
				val outgoingMode = route.modes[i]
				val nextOutgoingMode = route.modes[i + 1]
				if (outgoingMode != nextOutgoingMode) {
					modeEndIndexes[i] = i + 1
				} else {
					modeEndIndexes[i] = modeEndIndexes[i + 1]
				}
			}
		}

	}

	data class Stage(
			val location: Location,
			val prevMode: TravelMode?,
			val nextMode: TravelMode?,
			val instruction: String,
			val isMajor: Boolean)

}
