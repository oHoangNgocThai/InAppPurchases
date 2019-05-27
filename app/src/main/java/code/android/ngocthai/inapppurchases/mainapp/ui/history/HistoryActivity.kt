package code.android.ngocthai.inapppurchases.mainapp.ui.history

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.extension.nonNullSingle
import code.android.ngocthai.inapppurchases.base.extension.observe
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : BaseActivity() {

    companion object {
        private val TAG = HistoryActivity::class.java.simpleName
    }

    override val layoutResource: Int
        get() = R.layout.activity_history

    private lateinit var mViewModel: HistoryViewModel

    private val mHistoryAdapter = HistoryAdapter()

    override fun initComponent(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(this).get(HistoryViewModel::class.java)

        supportActionBar?.title = "History Purchase"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerRecord.apply {
            adapter = mHistoryAdapter
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        }

        mViewModel.getPurchaseHistoryRecord()
                .nonNullSingle()
                .observe(this) {
                    Log.d(TAG, "Purchase record $it")
                    mHistoryAdapter.submitList(it)
                }
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
