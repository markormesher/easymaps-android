package uk.co.markormesher.easymaps.mapperapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

val DB_NAME = "OfflineData"
val DB_VERSION = 1

class OfflineDatabase(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

	override fun onCreate(db: SQLiteDatabase?) = onUpgrade(db, 0, DB_VERSION)

	override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
		if (oldVersion >= newVersion) return
		when (oldVersion) {
			0 -> upgradeToV1(db)
		}
		onUpgrade(db, oldVersion + 1, newVersion)
	}

	private fun upgradeToV1(db: SQLiteDatabase?) {
		db?.execSQL(LocationSchema.v1._createTable)
		db?.execSQL(ConnectionSchema.v1._createTable)
	}

	fun updateLocations(locations: List<Location>) {
		val db = writableDatabase ?: throw SQLiteException("Could not acquire writable database")
		db.delete(LocationSchema._tableName, null, null)
		locations.forEach { l -> db.insert(LocationSchema._tableName, null, l.toContentValues()) }
	}

	fun updateConnections(connections: List<Connection>) {
		val db = writableDatabase ?: throw SQLiteException("Could not acquire writable database")
		db.delete(ConnectionSchema._tableName, null, null)
		connections.forEach { c -> db.insert(ConnectionSchema._tableName, null, c.toContentValues()) }
	}

	fun getAttractions(): List<Location> {
		val db = readableDatabase ?: throw SQLiteException("Could not acquire readable database")
		val cursor = db.rawQuery(
				"SELECT * FROM ${LocationSchema._tableName} WHERE ${LocationSchema.type} = ?;",
				arrayOf(LocationType.ATTRACTION.id.toString())
		)
		val output = ArrayList<Location>()
		if (cursor.moveToFirst()) {
			do {
				output.add(Location.fromCursor(cursor))
			} while (cursor.moveToNext())
		}
		cursor.close()
		return output
	}

}
