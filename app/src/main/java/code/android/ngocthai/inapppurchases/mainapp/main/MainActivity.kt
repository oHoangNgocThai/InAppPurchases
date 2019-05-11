package code.android.ngocthai.inapppurchases.mainapp.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity
import code.android.ngocthai.inapppurchases.mainapp.main.adapter.ProductAdapter
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), PurchasesUpdatedListener, ProductAdapter.ProductListener {

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private val skuList = listOf("thaihn_update_normal", "thaihn_update_premium", "inapp_purchases_item_two", "inapp_purchases_item_one", "inapp_purchases_item_three")
    }

    override val layoutResource: Int
        get() = R.layout.activity_main

    private lateinit var mViewModel: MainActivityViewModel

    private lateinit var billingClient: BillingClient
    private val mProductAdapter = ProductAdapter(this)

    override fun initComponent(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        setupBillingClient()

        buttonLoadProduct.setOnClickListener {
            loadProduct()
        }

        buttonClearHistory.setOnClickListener {
            clearHistory()
        }

        recyclerProduct.apply {
            adapter = mProductAdapter
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: responseCode:$responseCode -- purchase: $purchases")
        when(responseCode) {
            BillingClient.BillingResponse.OK -> {
                purchases?.let {
                    // Handle response success
                    allowMultiplePurchases(purchases)
                }
            }
            BillingClient.BillingResponse.USER_CANCELED -> {
                // Handle response cancel
            }
        }

    }

    override fun onItemClick(item: SkuDetails) {
        val billingFlowParam = BillingFlowParams
                .newBuilder()
                .setSkuDetails(item)
                .build()
        billingClient.launchBillingFlow(this, billingFlowParam)
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
                    Log.d(TAG, "Billing response ok")
                    mProductAdapter.updateData(skuDetailsList)
                } else {
                    Log.d(TAG, "Can't query")
                }
            }
        } else {
            Log.d(TAG, "Billing Client not ready")
        }
    }

    private fun allowMultiplePurchases(purchases: MutableList<Purchase>?) {
        val purchase = purchases?.first()
        if (purchase != null) {
            billingClient.consumeAsync(purchase.purchaseToken) { responseCode, purchaseToken ->
                if (responseCode == BillingClient.BillingResponse.OK && purchaseToken != null) {
                    Log.d(TAG, "AllowMultiplePurchases success, responseCode: $responseCode")
                } else {
                    Log.d(TAG, "Can't allowMultiplePurchases, responseCode: $responseCode")
                }
            }
        }
    }

    private fun clearHistory() {
        billingClient.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList?.forEach {
            billingClient.consumeAsync(it.purchaseToken) { responseCode, purchaseToken ->
                if (responseCode == BillingClient.BillingResponse.OK && purchaseToken != null) {
                    Log.d(TAG, "onPurchases Updated consumeAsync, purchases token removed: $purchaseToken")
                } else {
                    Log.d(TAG, "onPurchases some troubles happened: $responseCode")
                }
            }
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
