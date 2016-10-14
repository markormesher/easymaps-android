package uk.co.markormesher.prjandroid.sdk

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*

@Throws(IOException::class)
private fun Context.writeDeviceID(): String {
	val deviceIdFile = File(getDir("device-id", Context.MODE_PRIVATE), "device-id.txt")

	val deviceID = UUID.randomUUID().toString().toLowerCase()

	val outputStream = FileOutputStream(deviceIdFile)
	outputStream.write(deviceID.toByteArray())
	outputStream.close()

	return deviceID
}

fun Context.readDeviceID(): String {
	try {
		val deviceIdFile = File(getDir("device-id", Context.MODE_PRIVATE), "device-id.txt")

		if (!deviceIdFile.exists()) return writeDeviceID()

		val f = RandomAccessFile(deviceIdFile, "r")
		val bytes = ByteArray(f.length().toInt())
		f.readFully(bytes)
		f.close()

		return String(bytes)
	} catch (e: IOException) {
		return "unknown"
	}
}