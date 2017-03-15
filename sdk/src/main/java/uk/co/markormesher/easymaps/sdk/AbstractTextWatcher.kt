package uk.co.markormesher.easymaps.sdk

import android.text.Editable
import android.text.TextWatcher

open class AbstractTextWatcher: TextWatcher {

	override fun afterTextChanged(str: Editable?) {
	}

	override fun beforeTextChanged(str: CharSequence?, start: Int, count: Int, after: Int) {
	}

	override fun onTextChanged(str: CharSequence?, start: Int, before: Int, count: Int) {
	}

}
