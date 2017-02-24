package uk.co.markormesher.easymaps.mapperapp.data

import java.util.*

class StringSetBloomFilter(val m: Int, val k: Int) {

	private val salts = Array(k, { i -> "salt$k" })

	private val bitSet = BitSet(m)

	fun export(): String {
		with(StringBuilder()) {
			append(m).append(" ")
			append(k).append(" ")
			(0..m - 1).filter { i -> bitSet[i] }.forEach { i -> append(i).append(" ") }
			return toString()
		}
	}

	companion object {
		fun import(str: String): StringSetBloomFilter {
			with(Scanner(str)) {
				val m = nextInt()
				val k = nextInt()
				val filter = StringSetBloomFilter(m, k)
				while (hasNextInt()) {
					filter.bitSet[nextInt()] = true
				}
				return filter
			}
		}
	}

	fun insert(str: String) = (0..k - 1).forEach { i -> bitSet.set(getHash(str, i)) }

	fun mightContain(str: String) = (0..k - 1).all { i -> bitSet[getHash(str, i)] }

	private fun getHash(str: String, k: Int) = "${salts[k]}$str".hashCode().and(0x7fffffff).mod(m)

}
