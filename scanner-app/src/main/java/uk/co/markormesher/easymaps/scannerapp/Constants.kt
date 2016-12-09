package uk.co.markormesher.easymaps.scannerapp

val LOG_TAG = "__EASY"
val UPLOAD_INTERVAL = 2 * 60 * 60 * 1000L // 2 hours
val FILE_LIMIT = 1024 * 512 // 512 kb
val SSID_FILTER = "virgin media"
val UPLOAD_URL = "http://easymaps.markormesher.co.uk/scan-logs/upload"
val WITHDRAW_URL = "http://easymaps.markormesher.co.uk/withdrawal/register"
val CONTACT_EMAIL = "me@markormesher.co.uk"
val SUPER_USER_PIN = "150995"
val DEFAULT_NETWORK = "london"
val VALID_NETWORKS = arrayListOf("london", "london_clean")
val NO_NETWORK = "---none---"
val SCAN_INTERVALS = arrayOf(20, 15, 10, 5) // seconds
val DEFAULT_SCAN_INTERVAL_OPTION = 0
