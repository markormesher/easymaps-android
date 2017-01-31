package uk.co.markormesher.easymaps.mapperapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

val DB_NAME = "OfflineData"
val DB_VERSION = 1

class OfflineDatabase(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

	override fun onCreate(db: SQLiteDatabase?) = onUpgrade(db, 0, DB_VERSION)

	override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
		if (oldVersion >= newVersion) return

		when (oldVersion) {
			0 -> upgradeToV1(db)
		}

		// keep running upgrades until the current version is reached
		onUpgrade(db, oldVersion + 1, newVersion)
	}

	private fun upgradeToV1(db: SQLiteDatabase?) {
		db?.execSQL(LocationSchema.v1._createTable)
		db?.execSQL(ConnectionSchema.v1._createTable)
	}

	fun clearLocations() {
		val db = writableDatabase ?: throw SQLiteException("Could not acquire writable database")
		db.delete(LocationSchema._tableName, null, null)
	}

	fun addLocations(locations: List<Location>) {
		val db = writableDatabase ?: throw SQLiteException("Could not acquire writable database")
		locations.forEach { l -> db.insert(LocationSchema._tableName, null, l.toContentValues()) }
	}

	fun clearConnections() {
		val db = writableDatabase ?: throw SQLiteException("Could not acquire writable database")
		db.delete(ConnectionSchema._tableName, null, null)
	}

	fun addConnections(connections: List<Connection>) {
		val db = writableDatabase ?: throw SQLiteException("Could not acquire writable database")
		connections.forEach { c -> db.insert(ConnectionSchema._tableName, null, c.toContentValues()) }
	}

}
