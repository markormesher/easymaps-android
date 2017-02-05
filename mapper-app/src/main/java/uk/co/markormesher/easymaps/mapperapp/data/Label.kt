package uk.co.markormesher.easymaps.mapperapp.data

import android.content.ContentValues
import android.database.Cursor
import java.util.regex.Pattern

data class Label(val macAddress: String, val locationId: String) {

	fun toContentValues(): ContentValues {
		val cv = ContentValues()
		cv.put(LabelSchema.macAddress, macAddress)
		cv.put(LabelSchema.locationId, locationId)
		return cv
	}

	companion object {
		val linePattern: Pattern by lazy { Pattern.compile("\"(.*)\" = \"(.*)\"") }

		fun fromCursor(cursor: Cursor): Label {
			return Label(
					macAddress = cursor.getString(cursor.getColumnIndexOrThrow(LabelSchema.macAddress)),
					locationId = cursor.getString(cursor.getColumnIndexOrThrow(LabelSchema.locationId))
			)
		}

		fun fromLine(line: String): Label? {
			val matcher = linePattern.matcher(line.trim())
			if (matcher.find() && matcher.groupCount() == 2) {
				return Label(matcher.group(1), matcher.group(2))
			}
			return null
		}
	}

}

object LabelSchema {

	val _tableName = "Label"
	val macAddress = "macAddress"
	val locationId = "locationId"

	object v3 {
		val createTable = "CREATE TABLE $_tableName (" +
				"$macAddress TEXT," +
				"$locationId TEXT" +
				");"
	}

}
