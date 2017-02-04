package uk.co.markormesher.easymaps.mapperapp.data

import android.content.ContentValues
import android.database.Cursor
import org.json.JSONObject

data class Connection(val from: String, val to: String, val mode: String, val cost: Int) {

	fun toContentValues(): ContentValues {
		val cv = ContentValues()
		cv.put(ConnectionSchema.from, from)
		cv.put(ConnectionSchema.to, to)
		cv.put(ConnectionSchema.mode, mode)
		cv.put(ConnectionSchema.cost, cost)
		return cv
	}

	companion object {
		fun fromCursor(cursor: Cursor): Connection {
			return Connection(
					from = cursor.getString(cursor.getColumnIndexOrThrow(ConnectionSchema.from)),
					to = cursor.getString(cursor.getColumnIndexOrThrow(ConnectionSchema.to)),
					mode = cursor.getString(cursor.getColumnIndexOrThrow(ConnectionSchema.mode)),
					cost = cursor.getInt(cursor.getColumnIndexOrThrow(ConnectionSchema.cost))
			)
		}

		fun fromJson(json: JSONObject): Connection {
			return Connection(
					from = json.getString("from"),
					to = json.getString("to"),
					mode = json.getString("mode"),
					cost = json.getInt("cost")
			)
		}
	}

}

object ConnectionSchema {

	val _tableName = "Connection"
	val from = "from_node"
	val to = "to_node"
	val mode = "mode"
	val cost = "cost"

	object v1 {
		val createTable = "CREATE TABLE $_tableName (" +
				"$from TEXT," +
				"$to TEXT," +
				"$mode TEXT," +
				"$cost INTEGER" +
				");"
	}
}
