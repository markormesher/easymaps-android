package uk.co.markormesher.easymaps.scannerapp

val LOG_TAG = "__EASY"

val FILE_LIMIT = 1024 * 512 // 512 kb
val SSID_FILTER = "virgin media"

val UPLOAD_URL = "http://easymaps.markormesher.co.uk/scan-logs/upload"
val WITHDRAW_URL = "http://easymaps.markormesher.co.uk/withdrawal/register"
val WINNER_CHECK_URL = "http://easymaps.markormesher.co.uk/winners"
val CONTACT_EMAIL = "me@markormesher.co.uk"

val SUPER_USER_PIN = "150995"
val VALID_NETWORKS = arrayListOf("london", "london_clean")
val DEFAULT_NETWORK = "london"
val NO_NETWORK = "---none---"

val MIN_SCAN_INTERVAL = 5
val MAX_SCAN_INTERVAL = 20
val DEFAULT_SCAN_INTERVAL = 20
val UPLOAD_INTERVAL = 2 * 60 * 60 * 1000L // 2 hours

val WINNER_CHECK_SALT = "uS6eL4N*B+|*m+f>Jp+rj 6!kp39w3+r*V]q 8z=Nr`uA`R>:M\$B|*PK~0+ngW8v"
