package uk.co.markormesher.easymaps.mapperapp.location

import android.content.Context
import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.BLOOM_K
import uk.co.markormesher.easymaps.mapperapp.BLOOM_M
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.mapperapp.data.StringSetBloomFilter
import uk.co.markormesher.easymaps.mapperapp.helpers.getBloomFilterCache
import uk.co.markormesher.easymaps.sdk.WifiScanResult

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

	fun lookup(scanResults: Set<WifiScanResult>) {
		if (!running) {
			return
		}

		scanResults.forEach { result ->
		}
	}

}
