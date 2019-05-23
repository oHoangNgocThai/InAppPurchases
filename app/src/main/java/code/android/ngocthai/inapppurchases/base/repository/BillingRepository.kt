package code.android.ngocthai.inapppurchases.base.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import code.android.ngocthai.inapppurchases.base.entity.AugmentedSkuDetails
import com.android.billingclient.api.*

class BillingRepository private constructor(
        private val application: Application
) : PurchasesUpdatedListener, BillingClientStateListener, SkuDetailsResponseListener {

    private lateinit var mBillingClient: BillingClient

    var mSkuListInApp = listOf<String>()
    var mSkuListSubs = listOf<String>()

    private val mPurchases = MutableLiveData<List<Purchase>>()

    private val mAugmentedSkuDetailsEntity = hashSetOf<AugmentedSkuDetails>()
    private val mAugmentedSkuDetails = MutableLiveData<List<AugmentedSkuDetails>>()

    override fun onBillingServiceDisconnected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult?, skuDetailsList: MutableList<SkuDetails>?) {
        Log.d(TAG, "onSkuDetailsResponse(): result:$billingResult -- SkuDetails List:$skuDetailsList")
        billingResult?.let { result ->
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    skuDetailsList?.let {
                        if (skuDetailsList.isNotEmpty()) {
                            // Handle SkuDetails receive
                            handleSkuDetails(it)
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "onSkuDetailsResponse(): error:${result.debugMessage}")
                }
            }
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult?) {
        billingResult?.let {
            when (it.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "onBillingSetupFinished(): Successfully")
                    if (mSkuListInApp.isNotEmpty()) {
                        querySkuDetailsAsync(BillingClient.SkuType.INAPP, mSkuListInApp)
                    }
                    if (mSkuListSubs.isNotEmpty()) {
                        querySkuDetailsAsync(BillingClient.SkuType.SUBS, mSkuListSubs)
                    }
                    queryPurchasesAsync()
                }
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                    Log.d(TAG, "onBillingSetupFinished(): ${it.debugMessage}")
                }
                else -> {
                    Log.d(TAG, "onBillingSetupFinished(): ${it.debugMessage}")
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun startDataSourceConnection() {
        Log.d(TAG, "startDataSourceConnection()")
        initBillingClient()
        connectToGooglePlayBillingService()
    }

    fun endDataSourceConnection() {
        Log.d(TAG, "endDataSourceConnection()")
        mBillingClient.endConnection()
    }

    private fun initBillingClient() {
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

    fun queryPurchasesAsync() {
        Log.d(TAG, "queryPurchasesAsync()")
        val purchasesResult = hashSetOf<Purchase>()
        var result = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP)
        Log.d(TAG, "queryPurchasesAsync(): INAPP result:${result.purchasesList.size}")
        result?.purchasesList?.apply { purchasesResult.addAll(this) }
        if (isSubscriptionSupported()) {
            result = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS)
            result?.purchasesList?.apply { purchasesResult.addAll(this) }
            Log.d(TAG, "queryPurchasesAsync(): SUBS results:${result?.purchasesList?.size}")
        }
        handlePurchases(purchasesResult)
    }

    fun querySkuDetailsAsync(skuType: String, skuList: List<String>) {
        val params = SkuDetailsParams.newBuilder()
                .setType(skuType)
                .setSkusList(skuList)
                .build()
        mBillingClient.querySkuDetailsAsync(params, this)
    }

    /**
     * Handle purchases when get from service
     */
    private fun handlePurchases(purchases: Set<Purchase>) {
        Log.d(TAG, "handlePurchases()")
        // TODO: Validate purchase, save to server or local database
        mPurchases.value = purchases.toList()
    }

    private fun handleSkuDetails(skuDetailsList: MutableList<SkuDetails>) {
        Log.d(TAG, "handleSkuDetails(): $skuDetailsList")

        val augmentedList = arrayListOf<AugmentedSkuDetails>()
        skuDetailsList.forEach {
            it.apply {
                val originalJson = toString().substring("SkuDetails: ".length)
                val augmented = AugmentedSkuDetails(false, sku, type, price, title, description, originalJson)
                augmentedList.add(augmented)
            }
        }

        mAugmentedSkuDetailsEntity.addAll(augmentedList)
        mAugmentedSkuDetails.value = mAugmentedSkuDetailsEntity.toList()
    }

    private fun isSubscriptionSupported(): Boolean {
        val billingResult = mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        var result = false
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                result = true
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                // Reconnect to service
                connectToGooglePlayBillingService()
            }
            else -> {
                Log.d(TAG, "isSubscriptionSupported(): Error:${billingResult.debugMessage}")
            }
        }
        return result
    }

    fun getPurchases(): LiveData<List<Purchase>> {
        return mPurchases
    }

    fun getAugmentedSkuDetails(): MutableLiveData<List<AugmentedSkuDetails>> {
        return mAugmentedSkuDetails
    }

    companion object {
        private val TAG = BillingRepository::class.java.simpleName

        @Volatile
        private var INSTANCE: BillingRepository? = null

        fun getInstance(application: Application): BillingRepository =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: BillingRepository(application).also {
                        INSTANCE = it
                    }
                }
    }

    object PurchaseConfig {

        const val INAPP_ITEM_1 = "inapp_item_1"
        const val INAPP_ITEM_2 = "inapp_item_2"
        const val INAPP_ITEM_3 = "inapp_item_3"
        const val INAPP_ITEM_4 = "inapp_item_4"
        const val INAPP_ITEM_5 = "inapp_item_5"
        const val INAPP_ITEM_6 = "inapp_item_6"
        const val INAPP_ITEM_7 = "inapp_item_7"
        const val INAPP_ITEM_8 = "inapp_item_8"
        const val INAPP_ITEM_9 = "inapp_item_9"
        const val INAPP_ITEM_10 = "inapp_item_10"

        const val SUBS_MONTHLY = "subs_item_monthly"
        const val SUBS_YEARLY = "subs_item_yearly"

        val INAPP_SKUS = listOf(INAPP_ITEM_1, INAPP_ITEM_2, INAPP_ITEM_3, INAPP_ITEM_4, INAPP_ITEM_5, INAPP_ITEM_6,
                INAPP_ITEM_7, INAPP_ITEM_8, INAPP_ITEM_9, INAPP_ITEM_10)
        val SUBS_SKUS = listOf(SUBS_MONTHLY, SUBS_YEARLY)
    }
}
