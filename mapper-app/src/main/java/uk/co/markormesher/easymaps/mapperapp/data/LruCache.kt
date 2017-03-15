package uk.co.markormesher.easymaps.mapperapp.data

import java.util.*

class LruCache<K, V>(val capacity: Int): LinkedHashMap<K, V>(16, 0.75F, true) {

	override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean = size >= capacity
}
