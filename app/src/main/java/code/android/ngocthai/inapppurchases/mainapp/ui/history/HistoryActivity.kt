package code.android.ngocthai.inapppurchases.mainapp.ui.history

import android.os.Bundle
import android.view.MenuItem
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity

class HistoryActivity : BaseActivity() {

    override val layoutResource: Int
        get() = R.layout.activity_history

    override fun initComponent(savedInstanceState: Bundle?) {
        supportActionBar?.title = "History Purchase"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }
}
