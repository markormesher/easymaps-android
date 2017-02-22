package uk.co.markormesher.easymaps.mapperapp.helpers

import android.util.SparseIntArray
import java.util.*

operator fun SparseIntArray.set(i: Int, v: Int) = put(i, v)

operator fun SparseIntArray.get(i: Int) = get(i, -1)

fun <T> Array<T>.oneRandom() = this[Random().nextInt(this.size)]
