package code.android.ngocthai.inapppurchases.mainapp.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity
import code.android.ngocthai.inapppurchases.mainapp.main.adapter.PriceAdapter
import com.android.billingclient.api.*

class MainActivity : BaseActivity(), PurchasesUpdatedListener {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override val layoutResource: Int
        get() = R.layout.activity_main

    private lateinit var mViewModel: MainActivityViewModel

    private lateinit var billingClient: BillingClient
    private val mPriceAdapter = PriceAdapter()

    override fun initComponent(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        billingClient = BillingClient
                .newBuilder(this)
                .setListener(this)
                .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Try to restart connection
            }

            override fun onBillingSetupFinished(responseCode: Int) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    // Query purchases in here
                }
            }
        })


        val skuList = arrayListOf<String>()
        skuList.add("free")
        skuList.add("premium")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(params.build()) { responseCode, skuDetailsList ->
            if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                for (skudetail in skuDetailsList) {
                    val sku = skudetail.sku
                    Log.d(TAG, "sku: $sku")
                }
            }
        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {

    }
}
