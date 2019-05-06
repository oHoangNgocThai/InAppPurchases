package code.android.ngocthai.inapppurchases.base.extension

import android.app.Activity
import android.os.IBinder
import android.support.v4.app.Fragment
import android.view.View
import android.view.inputmethod.InputMethodManager

fun Fragment.dismissKeyboard(view: View?) {
    view?.let {
        activity?.dismissKeyboard(it.windowToken)
    }
}

private fun Activity.dismissKeyboard(windowToken: IBinder) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}
