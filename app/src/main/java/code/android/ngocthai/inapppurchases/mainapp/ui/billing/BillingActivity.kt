package code.android.ngocthai.inapppurchases.mainapp.ui.billing

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.extension.nonNullSingle
import code.android.ngocthai.inapppurchases.base.extension.observe
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity
import code.android.ngocthai.inapppurchases.mainapp.itemproduct.ProductAdapter
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import kotlinx.android.synthetic.main.activity_product.*

class BillingActivity : BaseActivity(), ProductAdapter.ProductListener {

    companion object {
        private val TAG = BillingActivity::class.java.simpleName
    }

    private lateinit var mViewModel: BillingViewModel

    private val mProductAdapter = ProductAdapter(this)

    override val layoutResource: Int
        get() = R.layout.activity_billing

    override fun initComponent(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(this).get(BillingViewModel::class.java)

        recyclerProduct.apply {
            adapter = mProductAdapter
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        }

        buttonClearHistory.setOnClickListener {
            mViewModel.clearHistory()
        }

        swipeRefreshProduct.setOnRefreshListener {
            mViewModel.querySkyDetailsAsync(BillingClient.SkuType.INAPP, BillingViewModel.ItemSku.INAPP_SKUS)
            mViewModel.querySkyDetailsAsync(BillingClient.SkuType.SUBS, BillingViewModel.ItemSku.SUBS_SKUS)
        }

        mViewModel.getSetupConnectionResponse()
                .nonNullSingle()
                .observe(this) {
                    handleConnection(it)
                }

        mViewModel.getSkuDetailLiveData()
                .nonNullSingle()
                .observe(this) {
                    swipeRefreshProduct.isRefreshing = false
                    Log.d(TAG, "SkuDetails: $it")
                    mProductAdapter.updateData(it)
                }

        mViewModel.getSkuDetailResponseCode()
                .nonNullSingle()
                .observe(this) {
                    // Handle get skuDetail if error
                    swipeRefreshProduct.isRefreshing = false
                }

        // Get purchases
        mViewModel.queryPurchasesAsync()
        mViewModel.getPurchasesLiveData()
                .nonNullSingle()
                .observe(this) {
                    Log.d(TAG, "PurChases: $it")
                }

    }

    override fun onItemClick(item: SkuDetails) {
        mViewModel.lauchBillingFlow(this, item)
    }

    @SuppressLint("SwitchIntDef")
    private fun handleConnection(responseCode: Int) {
        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                mViewModel.querySkyDetailsAsync(BillingClient.SkuType.INAPP, BillingViewModel.ItemSku.INAPP_SKUS)
                mViewModel.querySkyDetailsAsync(BillingClient.SkuType.SUBS, BillingViewModel.ItemSku.SUBS_SKUS)
            }
            BillingClient.BillingResponse.BILLING_UNAVAILABLE -> {

            }
            else -> {

            }
        }
    }
}
