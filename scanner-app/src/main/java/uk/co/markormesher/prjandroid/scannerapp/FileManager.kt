package uk.co.markormesher.prjandroid.scannerapp

import android.content.Context
import android.util.Log
import uk.co.markormesher.prjandroid.sdk.WifiScanResult
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

private var activeFile: File? = null

fun Context.writeScanResultsToFile(results: List<WifiScanResult>) {
	val sb = StringBuilder()
	with(sb) {
		append("[")
		append(System.currentTimeMillis())
		results.forEach { append(",").append(it.mac) }
		append("]")
	}
	appendToActiveFile(sb.toString())
}

fun Context.closeScanResultsFile() {
	createNewActiveFile()
}

private fun Context.getActiveFile(): File {
	if (activeFile == null) createNewActiveFile()
	return activeFile!!
}

private fun Context.createNewActiveFile() {
	activeFile = File(getDir("scanlogs", Context.MODE_PRIVATE), "${System.currentTimeMillis()}.txt")
}

private fun Context.checkActiveFileSize() {
	if (getActiveFile().length() >= FILE_LIMIT) createNewActiveFile()
}

fun Context.appendToActiveFile(data: String) {
	BufferedWriter(FileWriter(getActiveFile(), true))
			.append(data)
			.append("\n")
			.close()
	checkActiveFileSize()
	if (BuildConfig.DEBUG_MODE) Log.d(LOG_TAG, "Wrote to file: $data")
}