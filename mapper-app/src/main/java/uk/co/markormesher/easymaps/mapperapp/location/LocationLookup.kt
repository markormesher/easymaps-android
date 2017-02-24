package uk.co.markormesher.easymaps.mapperapp.location

import android.content.Context
import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.BLOOM_K
import uk.co.markormesher.easymaps.mapperapp.BLOOM_M
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.mapperapp.data.StringSetBloomFilter
import uk.co.markormesher.easymaps.mapperapp.helpers.getBloomFilterCache
import uk.co.markormesher.easymaps.sdk.WifiScanResult
import java.util.*

class LocationLookup(val context: Context): AnkoLogger {

	var running = false
	val bloomFilter: StringSetBloomFilter
	val db = OfflineDatabase(context)

	init {
		val bloomCache = context.getBloomFilterCache()
		if (bloomCache.isNullOrBlank()) {
			bloomFilter = StringSetBloomFilter(BLOOM_M, BLOOM_K)
		} else {
			bloomFilter = StringSetBloomFilter.import(bloomCache)
		}
		running = true
	}

	fun teardown() {
		running = false
		db.close()
	}

	fun lookupById(id: String) = db.getLocation(id)

	fun lookupByMajority(scanResults: Set<WifiScanResult>): Location? {
		if (!running) {
			return null
		}

		val locationIdCounts = HashMap<String, Int>()
		var totalLocationIds = 0
		scanResults
				.filter { r -> bloomFilter.mightContain(r.mac) }
				.forEach { r ->
					val locationId = db.getLocationIdFromLabel(r.mac)
					if (locationId != null) {
						++totalLocationIds
						locationIdCounts[locationId] = (locationIdCounts[locationId] ?: 0) + 1
					}
				}

		var result: Location? = null
		locationIdCounts.forEach { id, count ->
			if (count > totalLocationIds / 2) {
				result = lookupById(id)
				return@forEach
			}
		}

		return result
	}

}
