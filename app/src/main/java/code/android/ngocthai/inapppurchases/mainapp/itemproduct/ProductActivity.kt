package code.android.ngocthai.inapppurchases.mainapp.itemproduct

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_product.*

class ProductActivity : BaseActivity(), PurchasesUpdatedListener, ProductAdapter.ProductListener {

    companion object {
        private val TAG = ProductActivity::class.java.simpleName

        private val skuList = listOf("thaihn_update_normal", "thaihn_update_premium", "inapp_purchases_item_two", "inapp_purchases_item_one", "inapp_purchases_item_three")
    }

    private lateinit var billingClient: BillingClient
    private val mProductAdapter = ProductAdapter(this)

    override val layoutResource: Int
        get() = R.layout.activity_product

    override fun initComponent(savedInstanceState: Bundle?) {
        setupBillingClient()

        recyclerProduct.apply {
            adapter = mProductAdapter
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        }

        swipeRefreshProduct.setOnRefreshListener {
            queryProduct()
        }
    }

    override fun onItemClick(item: SkuDetails) {
        launchBillingFlow(item)
    }

    @SuppressLint("SwitchIntDef")
    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        textResult.text = "onPurchasesUpdated: responseCode:$responseCode -- purchase: $purchases"
        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                purchases?.let {
                    // Handle response success
//                    allowMultiplePurchases(purchases)
//                    textResult.text = "Billing Ok. Product $purchases"
                }
            }
            BillingClient.BillingResponse.USER_CANCELED -> {
                // Handle response cancel
//                textResult.text = "Cancel billing"
            }
        }
    }

    // Query product
    private fun queryProduct() {
        if (billingClient.isReady) {
            val params = SkuDetailsParams
                    .newBuilder()
                    .setSkusList(skuList)
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
            billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                swipeRefreshProduct.isRefreshing = false
                Log.d(TAG, "querySkuDetailsAsync: responseCode:$responseCode -- skuDetailList: $skuDetailsList")
                if (responseCode == BillingClient.BillingResponse.OK) {
                    textResult.text = "Load product success"
                    mProductAdapter.updateData(skuDetailsList)
                } else {
                    textResult.text = "Can't load product. responseCode:$responseCode"
                }
            }
        } else {
            swipeRefreshProduct.isRefreshing = false
            textResult.text = "Billing Client not ready"
        }
    }

    private fun launchBillingFlow(item: SkuDetails) {
        val billingFlowParam = BillingFlowParams
                .newBuilder()
                .setSkuDetails(item)
                .build()
        billingClient.launchBillingFlow(this, billingFlowParam)
    }

    private fun allowMultiplePurchases(purchases: MutableList<Purchase>?) {
        val purchase = purchases?.first()
        if (purchase != null) {
            billingClient.consumeAsync(purchase.purchaseToken) { responseCode, purchaseToken ->
                if (responseCode == BillingClient.BillingResponse.OK && purchaseToken != null) {
                    Toast.makeText(applicationContext, "AllowMultiplePurchases success, responseCode: $responseCode", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "AllowMultiplePurchases success, responseCode: $responseCode")
                } else {
                    Toast.makeText(applicationContext, "Can't allowMultiplePurchases, responseCode: $responseCode", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Can't allowMultiplePurchases, responseCode: $responseCode")
                }
            }
        }
    }


    // Setup connect to Billing service
    private fun setupBillingClient() {
        billingClient = BillingClient
                .newBuilder(this)
                .setListener(this)
                .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Try to restart connection
                Toast.makeText(applicationContext, "onBillingServiceDisconnected", Toast.LENGTH_SHORT).show()
            }

            override fun onBillingSetupFinished(responseCode: Int) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    // Query purchases in here
                    Toast.makeText(applicationContext, "onBillingSetupFinished: response OK", Toast.LENGTH_SHORT).show()
                    queryProduct()

                } else {
                    Toast.makeText(applicationContext, "onBillingSetupFinished: response fail: $responseCode", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun clearHistory() {
        billingClient.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList?.forEach {
            billingClient.consumeAsync(it.purchaseToken) { responseCode, purchaseToken ->
                if (responseCode == BillingClient.BillingResponse.OK && purchaseToken != null) {
                    Toast.makeText(applicationContext, "onPurchases Updated consumeAsync, purchases token removed: $purchaseToken", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "onPurchases some troubles happened: $responseCode", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
