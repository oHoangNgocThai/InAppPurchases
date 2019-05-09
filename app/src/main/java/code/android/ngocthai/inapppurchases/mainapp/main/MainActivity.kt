package code.android.ngocthai.inapppurchases.mainapp.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity
import code.android.ngocthai.inapppurchases.mainapp.main.adapter.PriceAdapter
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), PurchasesUpdatedListener {

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private val skuList = listOf("thaihn_update_normal", "thaihn_update_premium")
    }

    override val layoutResource: Int
        get() = R.layout.activity_main

    private lateinit var mViewModel: MainActivityViewModel

    private lateinit var billingClient: BillingClient
    private val mPriceAdapter = PriceAdapter()

    override fun initComponent(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        setupBillingClient()

        buttonLoadProduct.setOnClickListener {
            loadProduct()
        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: responseCode:$responseCode -- purchase: $purchases")
    }

    private fun loadProduct() {
        if (billingClient.isReady) {
            val params = SkuDetailsParams
                    .newBuilder()
                    .setSkusList(skuList)
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
            billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                Log.d(TAG, "querySkuDetailsAsync: responseCode:$responseCode -- skuDetailList: $skuDetailsList")
                if (responseCode == BillingClient.BillingResponse.OK) {
                    for (skudetail in skuDetailsList) {
                        val sku = skudetail.sku
                        Log.d(TAG, "sku: $sku")
                    }
                } else {
                    Log.d(TAG, "Can't query")
                }
            }
        } else {
            Log.d(TAG, "Billing Client not ready")
        }
    }

    private fun setupBillingClient() {
        billingClient = BillingClient
                .newBuilder(this)
                .setListener(this)
                .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Try to restart connection
                Log.d(TAG, "onBillingServiceDisconnected")
            }

            override fun onBillingSetupFinished(responseCode: Int) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    // Query purchases in here
                    Log.d(TAG, "onBillingSetupFinished: response ok")
                } else {
                    Log.d(TAG, "onBillingSetupFinished: response fail: $responseCode")
                }
            }
        })
    }
}
