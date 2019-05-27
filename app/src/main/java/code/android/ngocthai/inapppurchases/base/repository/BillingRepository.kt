package code.android.ngocthai.inapppurchases.base.repository

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import code.android.ngocthai.inapppurchases.base.entity.AugmentedSkuDetails
import code.android.ngocthai.inapppurchases.base.repository.BillingRepository.RetryPolicies.connectionRetryPolicy
import code.android.ngocthai.inapppurchases.base.repository.BillingRepository.RetryPolicies.resetConnectionRetryPolicyCounter
import code.android.ngocthai.inapppurchases.base.repository.local.LocalBillingDb
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

class BillingRepository private constructor(
        private val application: Application
) : PurchasesUpdatedListener, BillingClientStateListener, SkuDetailsResponseListener,
        PurchaseHistoryResponseListener, ConsumeResponseListener {

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

    private lateinit var mBillingClient: BillingClient

    private lateinit var mLocalCacheBillingClient: LocalBillingDb

    private val mAugmentedSkuDetails: LiveData<List<AugmentedSkuDetails>> by lazy {
        if (mLocalCacheBillingClient == null) {
            mLocalCacheBillingClient = LocalBillingDb.getInstance(application.applicationContext)
        }
        mLocalCacheBillingClient.skuDetailsDao().getSkuDetails()
    }

    private val mLoadRewardResponse = MutableLiveData<BillingResult>()

    private val mPurchaseHistoryRecord = MutableLiveData<List<PurchaseHistoryRecord>>()
    private val mPurchaseHistoryRecordEntity = hashSetOf<PurchaseHistoryRecord>()

    var mSkuListInApp = listOf<String>()
    var mSkuListSubs = listOf<String>()

    private val mConsumePurchaseToken = MutableLiveData<String>()
    private val mNonConsumePurchaseToken = MutableLiveData<String>()

    override fun onBillingServiceDisconnected() {
        // Reconnect to Service
        Log.d(TAG, "onBillingServiceDisconnected()")
        connectionRetryPolicy { connectToGooglePlayBillingService() }
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult?, skuDetailsList: MutableList<SkuDetails>?) {
        billingResult?.let { result ->
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "onSkuDetailsResponse(): OK")
                    skuDetailsList?.let {
                        skuDetailsList.forEach {
                            CoroutineScope(Job() + Dispatchers.IO).launch {
                                mLocalCacheBillingClient.skuDetailsDao().insertOrUpdate(it)
                            }
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "onSkuDetailsResponse(): responseCode: ${billingResult.responseCode} --- message:${result.debugMessage}")
                }
            }
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult?) {
        billingResult?.let {
            when (it.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "onBillingSetupFinished(): Successfully")
                    resetConnectionRetryPolicyCounter()
                    if (mSkuListInApp.isNotEmpty()) {
                        querySkuDetailsAsync(BillingClient.SkuType.INAPP, mSkuListInApp)
                    }
                    if (mSkuListSubs.isNotEmpty()) {
                        querySkuDetailsAsync(BillingClient.SkuType.SUBS, mSkuListSubs)
                    }
                    queryPurchasesAsync()
                    queryPurchaseHistoryAsync()
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
        billingResult?.let {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    // will handle server verification, consumables, and updating the local cache
                    purchases?.apply { handlePurchases(this.toSet()) }
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    // item already owned? call queryPurchasesAsync to verify and process all such items
                    Log.d(TAG, "onPurchasesUpdated(): ${billingResult.debugMessage}")
                }
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    Log.d(TAG, "onPurchasesUpdated(): Service disconnected")
                    connectToGooglePlayBillingService()
                }
                else -> {
                    Log.d(TAG, "onPurchasesUpdated(): ${billingResult.debugMessage}")
                }
            }
        }
    }

    override fun onPurchaseHistoryResponse(billingResult: BillingResult?, purchaseHistoryRecordList: MutableList<PurchaseHistoryRecord>?) {
        billingResult?.let {
            when (it.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "onPurchaseHistoryResponse(): OK - result:$purchaseHistoryRecordList")
                    purchaseHistoryRecordList?.let { purchaseHistory ->
                        mPurchaseHistoryRecordEntity.addAll(purchaseHistory)
                        mPurchaseHistoryRecord.value = mPurchaseHistoryRecordEntity.toList()
                    }
                }
                else -> {
                    Log.d(TAG, "onPurchaseHistoryResponse(): ${billingResult.debugMessage}")
                }
            }
        }
    }

    override fun onConsumeResponse(billingResult: BillingResult?, purchaseToken: String?) {
        billingResult?.let {
            when (it.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "onConsumeResponse(): OK")
                    // Update database or ui
                    purchaseToken?.let { token ->
                        mConsumePurchaseToken.value = token
                    }
                    queryPurchaseHistoryAsync()
                }
                else -> {
                    Log.d(TAG, "onConsumeResponse(): ${billingResult.debugMessage}")
                }
            }
        }
    }

    fun startDataSourceConnection() {
        Log.d(TAG, "startDataSourceConnection()")
        initBillingClient()
        connectToGooglePlayBillingService()
        mLocalCacheBillingClient = LocalBillingDb.getInstance(application)
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
                .setChildDirected(BillingClient.ChildDirected.CHILD_DIRECTED) /*For use Reward Product*/
                .setUnderAgeOfConsent(BillingClient.UnderAgeOfConsent.UNDER_AGE_OF_CONSENT) /*For use Reward Product*/
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

    fun queryPurchaseHistoryAsync() {
        mPurchaseHistoryRecordEntity.clear()
        mBillingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, this)
        mBillingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS, this)
    }

    fun querySkuDetailsAsync(skuType: String, skuList: List<String>) {
        val params = SkuDetailsParams.newBuilder()
                .setType(skuType)
                .setSkusList(skuList)
                .build()
        mBillingClient.querySkuDetailsAsync(params, this)
    }

    fun launchBillingFlow(activity: Activity, augmentedSkuDetails: AugmentedSkuDetails) {
        val skuDetails = SkuDetails(augmentedSkuDetails.originalJson)
        if (skuDetails.isRewarded) {
            launchBillingReward(skuDetails)
        } else {
            launchBillingFlow(activity, SkuDetails(augmentedSkuDetails.originalJson))
        }
    }

    fun launchBillingFlow(activity: Activity, skuDetails: SkuDetails) {
        Log.d(TAG, "launchBillingFlow()")
        val params = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
        mBillingClient.launchBillingFlow(activity, params)
    }

    fun launchBillingReward(skuDetail: SkuDetails) {
        Log.d(TAG, "launchBillingReward()")
        val params = RewardLoadParams.Builder()
                .setSkuDetails(skuDetail)
                .build()
        mBillingClient.loadRewardedSku(params) {
            mLoadRewardResponse.value = it
        }
    }

    private fun handlePurchases(purchases: Set<Purchase>) {
        Log.d(TAG, "handlePurchases()")
        // TODO: Validate purchase, save to server or local database

        val validPurchases = HashSet<Purchase>(purchases.size)
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Valid purchases
                if (isSignatureValid(purchase)) {
                    validPurchases.add(purchase)
                }
            } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                Log.d(TAG, "handlePurchases(): Receive a pending purchase of SKU: $purchase.sku")
                // handle pending purchases, e.g. confirm with users about the pending
                // purchases, prompt them to complete it, etc.
            }

            val (consumables, nonConsumables) = validPurchases.partition {
                PurchaseConfig.CONSUMABLE_SKUS.contains(it.sku)
            }

            Log.d(TAG, "handlePurchases(): Consumables content:$consumables")
            Log.d(TAG, "handlePurchases(): non-consumables content:$nonConsumables")

            handleConsumablePurchasesAsync(consumables)
            acknowledgeNonConsumablePurchaseAsync(nonConsumables)
        }
    }

    fun handleConsumablePurchasesAsync(consumables: List<Purchase>) {
        Log.d(TAG, "handleConsumablePurchasesAsync()")
        consumables.forEach { purchase ->
            Log.d(TAG, "handleConsumablePurchasesAsync() foreach it is $purchase")
            consumePurchase(purchase)
        }
    }

    fun consumePurchase(purchase: Purchase) {
        val params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        mBillingClient.consumeAsync(params, this)
    }

    fun clearHistory() {
        // TODO: Fix
        Log.d(TAG, "clearHistory()")
        mBillingClient.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList?.forEach {
            Log.d(TAG, "clearHistory(): INAPP purchase:$it")
            consumePurchase(it)
        }
        mBillingClient.queryPurchases(BillingClient.SkuType.SUBS)?.purchasesList?.forEach {
            Log.d(TAG, "clearHistory(): SUBS purchase:$it")
            consumePurchase(it)
        }
    }

    fun acknowledgeNonConsumablePurchaseAsync(nonConsumable: List<Purchase>) {
        Log.d(TAG, "acknowledgeNonConsumablePurchaseAsync()")
        nonConsumable.forEach { purchase ->
            val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .setDeveloperPayload(purchase.developerPayload)
                    .build()

            mBillingClient.acknowledgePurchase(params) { billingResult ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        // handle purchase non-consume
                        mNonConsumePurchaseToken.value = purchase.sku
                    }
                    else -> {
                        Log.d(TAG, "onAcknowledgePurchaseResponse() response is : ${billingResult.debugMessage}")
                    }
                }
            }

            CoroutineScope(Job() + Dispatchers.IO).launch {
                mLocalCacheBillingClient.skuDetailsDao().update(purchase.sku, false)
            }
        }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(
                Security.BASE_64_ENCODE_PUBLIC_KEY, purchase.originalJson, purchase.signature
        )
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

    fun getConsumePurchaseToken(): MutableLiveData<String> = mConsumePurchaseToken

    fun getNonConsumePurchaseToken(): MutableLiveData<String> = mNonConsumePurchaseToken

    fun getAugmentedSkuDetails(): LiveData<List<AugmentedSkuDetails>> = mAugmentedSkuDetails

    fun getPurchaseHistoryRecord(): LiveData<List<PurchaseHistoryRecord>> = mPurchaseHistoryRecord

    fun getLoadRewardResponse(): LiveData<BillingResult> = mLoadRewardResponse

    private object RetryPolicies {
        private val maxRetry = 5
        private var retryCounter = AtomicInteger(1)
        private val baseDelayMillis = 500
        private val taskDelay = 2000L

        fun resetConnectionRetryPolicyCounter() {
            retryCounter.set(1)
        }

        /**
         * This works because it actually only makes one call. Then it waits for success or failure.
         * onSuccess it makes no more calls and resets the retryCounter to 1. onFailure another
         * call is made, until too many failures cause retryCounter to reach maxRetry and the
         * policy stops trying. This is a safe algorithm: the initial calls to
         * connectToPlayBillingService from instantiateAndConnectToPlayBillingService is always
         * independent of the RetryPolicies. And so the Retry Policy exists only to help and never
         * to hurt.
         */
        fun connectionRetryPolicy(block: () -> Unit) {
            Log.d(TAG, "connectionRetryPolicy")
            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                val counter = retryCounter.getAndIncrement()
                if (counter < maxRetry) {
                    val waitTime: Long = (2f.pow(counter) * baseDelayMillis).toLong()
                    delay(waitTime)
                    block()
                }
            }

        }

        /**
         * All this is doing is check that billingClient is connected and if it's not, request
         * connection, wait x number of seconds and then proceed with the actual task.
         */
        fun taskExecutionRetryPolicy(billingClient: BillingClient, listener: BillingRepository, task: () -> Unit) {
            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                if (!billingClient.isReady) {
                    Log.d(TAG, "taskExecutionRetryPolicy billing not ready")
                    billingClient.startConnection(listener)
                    delay(taskDelay)
                }
                task()
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
        const val REWARD_AD = "android.test.reward"

        const val SUBS_MONTHLY = "subs_item_monthly"
        const val SUBS_YEARLY = "subs_item_yearly"

        val INAPP_SKUS = listOf(INAPP_ITEM_1, INAPP_ITEM_2, INAPP_ITEM_3, INAPP_ITEM_4, INAPP_ITEM_5, INAPP_ITEM_6,
                INAPP_ITEM_7, INAPP_ITEM_8, INAPP_ITEM_9, INAPP_ITEM_10, REWARD_AD)
        val SUBS_SKUS = listOf(SUBS_MONTHLY, SUBS_YEARLY)
        val CONSUMABLE_SKUS = listOf(INAPP_ITEM_1, INAPP_ITEM_2, INAPP_ITEM_3, INAPP_ITEM_4, INAPP_ITEM_5)
    }
}
