package uk.co.markormesher.easymaps.mapperapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

val DB_NAME = "OfflineData"
val DB_VERSION = 3

class OfflineDatabase(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

	companion object {
		val STATE_UPDATED = "data.OfflineDatabase:STATE_UPDATED"
	}

	override fun onCreate(db: SQLiteDatabase?) = onUpgrade(db, 0, DB_VERSION)

	override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
		if (oldVersion >= newVersion) return
		when (oldVersion) {
			0 -> upgradeToV1(db)
			1 -> upgradeToV2(db)
			2 -> upgradeToV3(db)
		}
		onUpgrade(db, oldVersion + 1, newVersion)
	}

	private fun upgradeToV1(db: SQLiteDatabase?) {
		db?.execSQL(LocationSchema.v1.createTable)
		db?.execSQL(ConnectionSchema.v1.createTable)
	}

	private fun upgradeToV2(db: SQLiteDatabase?) {
		db?.execSQL(LocationSchema.v2.addLat)
		db?.execSQL(LocationSchema.v2.addLon)
	}

	private fun upgradeToV3(db: SQLiteDatabase?) {
		db?.execSQL(LabelSchema.v3.createTable)
	}

	fun updateLabels(labels: List<Label>, statusCallback: (qtyDone: Int) -> Unit) {
		val db = writableDatabase ?: throw SQLiteException("Could not acquire writable database")
		db.delete(LabelSchema._tableName, null, null)
		labels.forEachIndexed { i, l ->
			db.insert(LabelSchema._tableName, null, l.toContentValues())
			statusCallback(i + 1)
		}
	}

	fun updateLocations(locations: List<Location>, statusCallback: (qtyDone: Int) -> Unit) {
		val db = writableDatabase ?: throw SQLiteException("Could not acquire writable database")
		db.delete(LocationSchema._tableName, null, null)
		locations.forEachIndexed { i, l ->
			db.insert(LocationSchema._tableName, null, l.toContentValues())
			statusCallback(i + 1)
		}
	}

	fun updateConnections(connections: List<Connection>, statusCallback: (qtyDone: Int) -> Unit) {
		val db = writableDatabase ?: throw SQLiteException("Could not acquire writable database")
		db.delete(ConnectionSchema._tableName, null, null)
		connections.forEachIndexed { i, c ->
			db.insert(ConnectionSchema._tableName, null, c.toContentValues())
			statusCallback(i + 1)
		}
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
