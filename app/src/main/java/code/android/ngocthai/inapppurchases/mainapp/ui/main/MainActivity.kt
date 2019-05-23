package code.android.ngocthai.inapppurchases.mainapp.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.entity.AugmentedSkuDetails
import code.android.ngocthai.inapppurchases.base.extension.nonNullSingle
import code.android.ngocthai.inapppurchases.base.extension.observe
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity
import code.android.ngocthai.inapppurchases.mainapp.ui.billing.ProductAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() , ProductAdapter.ProductListener{

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override val layoutResource: Int
        get() = R.layout.activity_main

    private lateinit var mViewModel: MainViewModel

    private val mProductAdapter = ProductAdapter(this)

    override fun initComponent(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        recyclerSkuDetails.apply {
            adapter = mProductAdapter
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        }

        mViewModel.getSkuDetails()
                .nonNullSingle()
                .observe(this) {
                    Log.d(TAG, "AugmentedSkuDetails: $it")

                    mProductAdapter.updateData(it)
                }


    }

    override fun onBuyClick(item: AugmentedSkuDetails) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemClick(item: AugmentedSkuDetails) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
