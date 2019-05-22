package code.android.ngocthai.inapppurchases.mainapp.ui.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.util.Log
import com.android.billingclient.api.*

abstract class BaseBillingViewModel(application: Application) : AndroidViewModel(application),
        PurchasesUpdatedListener, BillingClientStateListener,
        SkuDetailsResponseListener {

    companion object {
        private val TAG = BaseBillingViewModel::class.java.simpleName
    }

    private lateinit var mBillingClient: BillingClient

    abstract val skuListSubs: List<String>
    abstract val skuListInApp: List<String>

    init {
        initBillingClient(application)
        connectToGooglePlayBillingService()
    }

    abstract fun billingServiceDisconnected()

    abstract fun billingSetupFinished(billingResult: BillingResult?)

    abstract fun purchasesUpdate(billingResult: BillingResult?, purchases: MutableList<Purchase>?)

    abstract fun skuDetailsResponse(billingResult: BillingResult?, skuDetailsList: MutableList<SkuDetails>?)

    override fun onBillingSetupFinished(billingResult: BillingResult?) {
        billingSetupFinished(billingResult)
        // Query SkuDetails available
        if (skuListInApp.isNotEmpty()) {
            querySkuDetailsAsync(BillingClient.SkuType.INAPP, skuListInApp)
        } else {
            Log.w(TAG, "SkuList InApp is empty")
        }
        if (skuListSubs.isNotEmpty()) {
            querySkuDetailsAsync(BillingClient.SkuType.SUBS, skuListSubs)
        } else {
            Log.w(TAG, "SkuList Subs is empty")
        }
    }

    override fun onBillingServiceDisconnected() {
        billingServiceDisconnected()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        purchasesUpdate(billingResult, purchases)
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult?, skuDetailsList: MutableList<SkuDetails>?) {
        skuDetailsResponse(billingResult, skuDetailsList)
    }

    private fun initBillingClient(application: Application) {
        Log.d(TAG, "initBillingClient()")
        mBillingClient = BillingClient.newBuilder(application)
                .enablePendingPurchases()
                .setListener(this)
                .setChildDirected(BillingClient.ChildDirected.CHILD_DIRECTED)
                .setUnderAgeOfConsent(BillingClient.UnderAgeOfConsent.UNDER_AGE_OF_CONSENT)
                .build()
    }

    private fun connectToGooglePlayBillingService(): Boolean {
        Log.d(TAG, "connectToGooglePlayBillingService()")
        return if (!mBillingClient.isReady) {
            mBillingClient.startConnection(this)
            true
        } else {
            false
        }
    }

    fun querySkuDetailsAsync(skuType: String, skuList: List<String>) {
        val params = SkuDetailsParams.newBuilder()
                .setType(skuType)
                .setSkusList(skuList)
                .build()
        mBillingClient.querySkuDetailsAsync(params, this)
    }
}
