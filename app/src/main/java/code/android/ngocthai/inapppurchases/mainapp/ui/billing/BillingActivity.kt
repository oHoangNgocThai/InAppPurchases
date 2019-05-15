package code.android.ngocthai.inapppurchases.mainapp.ui.billing

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.extension.nonNullSingle
import code.android.ngocthai.inapppurchases.base.extension.observe
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity
import code.android.ngocthai.inapppurchases.mainapp.itemproduct.ProductAdapter
import code.android.ngocthai.inapppurchases.mainapp.util.BillingResponseCode
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

        mViewModel.allowMultiplePurchase = true

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
                    Log.d(TAG, "getSetupConnectionResponse(): $it")
                    handleConnection(it)
                }

        // Get SkuDetails
        mViewModel.getSkuDetailLiveData()
                .nonNullSingle()
                .observe(this) {
                    swipeRefreshProduct.isRefreshing = false
                    Log.d(TAG, "getSkuDetailLiveData(): $it")
                    mProductAdapter.updateData(it)
                }

        mViewModel.getSkuDetailResponseCode()
                .nonNullSingle()
                .observe(this) {
                    // Handle get skuDetail if error
                    Log.d(TAG, "getSkuDetailResponseCode(): $it")
                    swipeRefreshProduct.isRefreshing = false
                }

        // Get purchases
        mViewModel.queryPurchasesAsync()
        mViewModel.getPurchasesLiveData()
                .nonNullSingle()
                .observe(this) {
                    Log.d(TAG, "getPurchasesLiveData(): $it")
                }

        // Purchases update
        mViewModel.getPurchasesUpdateLiveData()
                .nonNullSingle()
                .observe(this) {
                    Log.d(TAG, "getPurchasesUpdateLiveData(): $it")
                }

        mViewModel.getPurchasesUpdateResponseCode()
                .nonNullSingle()
                .observe(this) {
                    Log.d(TAG, "getPurchasesUpdateResponseCode(): $it")
                    handlePurchasesUpdate(it)
                }

        // Reward
        mViewModel.getStatusReward()
                .nonNullSingle()
                .observe(this) {
                    Log.d(TAG, "getStatusReward(): $it")
                }

        mViewModel.getRewardResponseCode()
                .nonNullSingle()
                .observe(this) {
                    handleRewardResponse(it)
                }

        // Consume
        mViewModel.getConsumePurchaseToken()
                .nonNullSingle()
                .observe(this) {
                    Log.d(TAG, "getConsumePurchaseToken(): $it")
                    // Update ui
                }

        mViewModel.getConsumeResponseCode()
                .nonNullSingle()
                .observe(this) {
                    Log.d(TAG, "getConsumePurchaseToken(): $it")
                    handleConsumePurchase(it)
                }

    }

    override fun onItemClick(item: SkuDetails) {
        mViewModel.launchBillingFlow(this, item)
    }

    private fun handleConsumePurchase(responseCode: Int) {

    }

    private fun handleConnection(responseCode: Int) {
        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                mViewModel.querySkyDetailsAsync(BillingClient.SkuType.INAPP, BillingViewModel.ItemSku.INAPP_SKUS)
                mViewModel.querySkyDetailsAsync(BillingClient.SkuType.SUBS, BillingViewModel.ItemSku.SUBS_SKUS)
            }
            BillingClient.BillingResponse.BILLING_UNAVAILABLE -> {
                Log.d(TAG, "handleConnection(): responseCode:$responseCode--message:${BillingResponseCode.BILLING_UNAVAILABLE.message}")
                Toast.makeText(applicationContext, BillingResponseCode.BILLING_UNAVAILABLE.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePurchasesUpdate(responseCode: Int) {
        when (responseCode) {
            BillingClient.BillingResponse.USER_CANCELED -> {
                Toast.makeText(applicationContext, "handlePurchasesUpdate(): responseCode:$responseCode -- message:${BillingResponseCode.USER_CANCELED.message}", Toast.LENGTH_LONG).show()
            }
            BillingClient.BillingResponse.DEVELOPER_ERROR -> {
                Toast.makeText(applicationContext, "handlePurchasesUpdate(): responseCode:$responseCode -- message:${BillingResponseCode.DEVELOPER_ERROR.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleRewardResponse(responseCode: Int) {
        when (responseCode) {

        }
    }
}
