package uk.co.markormesher.easymaps.mapperapp.data

import android.content.ContentValues
import android.database.Cursor
import org.json.JSONObject

data class Location(val id: String, val title: String, val image: String?, val type: LocationType) {

	fun toContentValues(): ContentValues {
		val cv = ContentValues()
		cv.put(LocationSchema.id, id)
		cv.put(LocationSchema.title, title)
		cv.put(LocationSchema.image, image)
		cv.put(LocationSchema.type, type.id)
		return cv
	}

	companion object {
		fun fromCursor(cursor: Cursor): Location {
			return Location(
					id = cursor.getString(cursor.getColumnIndexOrThrow(LocationSchema.id)),
					title = cursor.getString(cursor.getColumnIndexOrThrow(LocationSchema.title)),
					image = cursor.getString(cursor.getColumnIndexOrThrow(LocationSchema.image)),
					type = LocationType.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(LocationSchema.type)))
			)
		}

		fun fromJson(id: String, json: JSONObject): Location {
			return Location(
					id = id,
					title = json.getString("title"),
					image = if (json.has("image")) json.getString("image") else null,
					type = LocationType.fromString(json.getString("type"))
			)
		}
	}

}

enum class LocationType(val id: Int) {

	ATTRACTION(0), STATION(1);

	companion object {
		fun fromId(id: Int): LocationType = when (id) {
			0 -> ATTRACTION
			1 -> STATION
			else -> throw IllegalArgumentException()
		}

		fun fromString(str: String): LocationType = when(str) {
			"attraction" -> ATTRACTION
			"station" -> STATION
			else -> STATION
		}
	}
}

object LocationSchema {

	val _tableName = "Location"
	val id = "id"
	val title = "title"
	val image = "image"
	val type = "type"

	object v1 {
		val _createTable = "CREATE TABLE $_tableName (" +
				"$id TEXT PRIMARY KEY," +
				"$title TEXT," +
				"$image TEXT," +
				"$type INTEGER" +
				");"
	}
}
