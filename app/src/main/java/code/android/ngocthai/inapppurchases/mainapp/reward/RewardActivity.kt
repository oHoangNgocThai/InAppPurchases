package code.android.ngocthai.inapppurchases.mainapp.reward

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity
import code.android.ngocthai.inapppurchases.mainapp.itemproduct.ProductAdapter
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_product.*

class RewardActivity : BaseActivity(), PurchasesUpdatedListener, ProductAdapter.ProductListener {

    companion object {
        private val TAG = RewardActivity::class.java.simpleName

        private val skuList = listOf("ads_get_life", "android.test.reward")
    }

    override val layoutResource: Int
        get() = R.layout.activity_reward

    private lateinit var billingClient: BillingClient
    private val mProductAdapter = ProductAdapter(this)

    override fun initComponent(savedInstanceState: Bundle?) {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupBillingClient()

        recyclerProduct.apply {
            adapter = mProductAdapter
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
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
                    allowMultiplePurchases(purchases)
//                    textResult.text = "Billing Ok. Product $purchases"
                }
            }
            BillingClient.BillingResponse.USER_CANCELED -> {
                // Handle response cancel
//                textResult.text = "Cancel billing"
            }
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> {
                Toast.makeText(applicationContext, "Your already owned", Toast.LENGTH_SHORT).show()
            }
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

    private fun launchBillingFlow(item: SkuDetails) {
        if (item.isRewarded) {
            val params = RewardLoadParams.Builder()
                    .setSkuDetails(item)
                    .build()

            billingClient.loadRewardedSku(params) { responseCode ->
                Log.d(TAG, "response code:$responseCode")
                if (responseCode == BillingClient.BillingResponse.OK) {
                    // Enable the reward product, or make
                    // any necessary updates to the UI.
                    textResult.text = "Ad success"
                }
            }
        }
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
}
