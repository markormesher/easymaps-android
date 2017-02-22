package uk.co.markormesher.easymaps.mapperapp.helpers

import android.util.SparseIntArray

operator fun SparseIntArray.set(i: Int, v: Int) = put(i, v)

operator fun SparseIntArray.get(i: Int) = get(i, -1)
