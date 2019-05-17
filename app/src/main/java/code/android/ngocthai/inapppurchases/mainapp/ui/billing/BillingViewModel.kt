package code.android.ngocthai.inapppurchases.mainapp.ui.billing

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.android.billingclient.api.*

class BillingViewModel(application: Application) : AndroidViewModel(application),
        PurchasesUpdatedListener, BillingClientStateListener,
        ConsumeResponseListener, SkuDetailsResponseListener,
        RewardResponseListener, AcknowledgePurchaseResponseListener {

    companion object {
        private val TAG = BillingViewModel::class.java.simpleName
    }

    var allowMultiplePurchase = false

    private lateinit var mBillingClient: BillingClient

    private val mBillingServiceDisconnected = MutableLiveData<Boolean>()
    private val mSetupBillingResult = MutableLiveData<BillingResult>()

    private val mSkuDetailList = hashSetOf<SkuDetails>()
    private val mSkuDetailsLiveData = MutableLiveData<List<SkuDetails>>()
    private val mSkuDetailBillingResult = MutableLiveData<BillingResult>()

    private val mPurchasesLiveData = MutableLiveData<List<Purchase>>()

    private val mStatusReward = MutableLiveData<Boolean>()
    private val mRewardBillingResult = MutableLiveData<BillingResult>()

    private val mPurchasesUpdateLiveData = MutableLiveData<List<Purchase>>()
    private val mPurchaseUpdateBillingResult = MutableLiveData<BillingResult>()

    private val mConsumePurchaseToken = MutableLiveData<String>()
    private val mConsumeBillingResult = MutableLiveData<BillingResult>()

    init {
        startDataSourceConnection(application)
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected()")
        mBillingServiceDisconnected.value = true
    }

    override fun onBillingSetupFinished(billingResult: BillingResult?) {
        Log.d(TAG, "onBillingSetupFinished(): responseCode:${billingResult?.responseCode} -- message:${billingResult?.debugMessage}")
        mSetupBillingResult.value = billingResult
    }

    override fun onConsumeResponse(billingResult: BillingResult?, purchaseToken: String?) {
        billingResult?.let {
            val responseCode = it.responseCode
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "onPurchases Updated consumeAsync, purchases token removed: $purchaseToken")
                    if (purchaseToken != null) {
                        mConsumePurchaseToken.value = purchaseToken
                    }
                    // Update to server
                }
                else -> {
                    Log.d(TAG, "onPurchases some troubles happened: $responseCode")
                    mConsumeBillingResult.value = it
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        billingResult?.let {
            val responseCode = it.responseCode
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "onPurchasesUpdate(): Ok")
                    purchases?.let { purchaseList ->
                        mPurchasesUpdateLiveData.value = purchaseList.toList()
                        if (allowMultiplePurchase) {
                            // Consume if item allow multiple purchase
                            purchaseList.first()?.let { purchases ->
                                consumePurchasesAsync(purchases)
                            }
                        } else {
                            // acknowledge purchase if item one-time purchase
                            handlePurchase(purchaseList.first())
                        }

                        // Update purchase server
                    }
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    Log.d(TAG, "onPurchasesUpdate(): ${it.debugMessage}")
                    queryPurchasesAsync()
                }
                else -> {
                    Log.d(TAG, "onPurchasesUpdate(): responseCode:$responseCode -- message:${it.debugMessage}")
                    mPurchaseUpdateBillingResult.value = it
                }
            }
        }
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult?, skuDetailsList: MutableList<SkuDetails>?) {
        billingResult?.let {
            val responseCode = it.responseCode
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "onSkuDetailResponse() success, list:$skuDetailsList")
                    skuDetailsList?.let { skuDetails ->
                        if (skuDetails.isNotEmpty()) {
                            mSkuDetailList.addAll(skuDetails)
                            mSkuDetailsLiveData.value = mSkuDetailList.toList()
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "onSkuDetailResponse() fail, responseCode:$responseCode -- message:${it.debugMessage}")
                    mSkuDetailBillingResult.value = it
                }
            }
        }
    }

    override fun onRewardResponse(billingResult: BillingResult?) {
        billingResult?.let {
            val responseCode = it.responseCode
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    // Enable the reward product, or make
                    // any necessary updates to the UI.
                    Log.d(TAG, "onRewardResponse(): OK")
                    mStatusReward.value = true
                }
                else -> {
                    Log.d(TAG, "onRewardResponse(): responseCode:${it.responseCode} -- message:${it.debugMessage}")
                    mRewardBillingResult.value = it
                }
            }
        }
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult?) {
        // TODO
    }

    private fun startDataSourceConnection(application: Application) {
        Log.d(TAG, "startDataSourceConnection()")
        instantiateAndConnectToPlayBillingService(application)
    }

    private fun endDataSourceConnection() {
        Log.d(TAG, "endDataSourceConnection()")
        mBillingClient.endConnection()
    }

    private fun instantiateAndConnectToPlayBillingService(application: Application) {
        mBillingClient = BillingClient.newBuilder(application)
                .enablePendingPurchases()
                .setListener(this)
                .setChildDirected(BillingClient.ChildDirected.CHILD_DIRECTED)
                .setUnderAgeOfConsent(BillingClient.UnderAgeOfConsent.UNDER_AGE_OF_CONSENT)
                .build()
        connectToPlayBillingService()
    }

    private fun connectToPlayBillingService(): Boolean {
        Log.d(TAG, "connectToPlayBillingService")
        if (!mBillingClient.isReady) {
            mBillingClient.startConnection(this)
            return true
        }
        return false
    }

    fun launchBillingFlow(activity: Activity, skuDetail: SkuDetails) {
        if (skuDetail.isRewarded) {
            launchBillingReward(skuDetail)
        } else {
            launchBillingInApp(activity, skuDetail)
        }
    }

    private fun launchBillingReward(skuDetail: SkuDetails) {
        Log.d(TAG, "launchBillingReward()")
        val params = RewardLoadParams.Builder()
                .setSkuDetails(skuDetail)
                .build()
        mBillingClient.loadRewardedSku(params) {
            if(it.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Reward success")
            }
        }
    }

    private fun launchBillingInApp(activity: Activity, skuDetails: SkuDetails) {
        Log.d(TAG, "launchBillingInApp()")
        val billingFlowParam = BillingFlowParams
                .newBuilder()
                .setSkuDetails(skuDetails)
                .build()
        mBillingClient.launchBillingFlow(activity, billingFlowParam)
    }

    fun queryPurchasesAsync() {
        val purchasesResult = java.util.HashSet<Purchase>()
        var result = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP)
        Log.d(TAG, "queryPurchasesAsync INAPP results: ${result?.purchasesList}")
        result?.purchasesList?.apply { purchasesResult.addAll(this) }
        if (isSubscriptionSupported()) {
            result = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS)
            result?.purchasesList?.apply { purchasesResult.addAll(this) }
            Log.d(TAG, "queryPurchasesAsync SUBS results: ${result?.purchasesList}")
        }
        mPurchasesLiveData.value = purchasesResult.toList()

        // set data to server for validate
    }

    fun querySkyDetailsAsync(skuType: String, skuList: List<String>) {
        val params = SkuDetailsParams.newBuilder()
                .setType(skuType)
                .setSkusList(skuList)
                .build()
        mBillingClient.querySkuDetailsAsync(params, this)
    }

    fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

            // Acknowledge the purchase if it hasn't already been acknowledged
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, this)
            }
        }
    }

    fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Grant the item to the user, and then acknowledge the purchase
            acknowledgePurchase(purchase)
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            // Here you can confirm to the user that they've started the pending
            // purchase, and to complete it, they should follow instructions that
            // are given to them. You can also choose to remind the user in the
            // future to complete the purchase if you detect that it is still
            // pending.
        }
    }

    fun clearInAppHistory() {
        mBillingClient.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList?.forEach {
            consumePurchasesAsync(it)
        }
    }

    fun clearSubsHistory() {
        mBillingClient.queryPurchases(BillingClient.SkuType.SUBS)?.purchasesList?.forEach {
            consumePurchasesAsync(it)
        }
    }

    fun clearHistory() {
        clearInAppHistory()
        clearSubsHistory()
    }

    fun consumePurchasesAsync(purchase: Purchase?) {
        Log.d(TAG, "consumePurchasesAsync(): $purchase")
        if (purchase != null) {
            val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .setDeveloperPayload(purchase.developerPayload)
                    .build()
            mBillingClient.consumeAsync(consumeParams, this)
        } else {
            Log.d(TAG, "consumePurchaseAsync(): purchase is null")
        }
    }

    fun getSetupBillingResult(): LiveData<BillingResult> {
        return mSetupBillingResult
    }

    fun getSkuDetailLiveData(): LiveData<List<SkuDetails>> {
        return mSkuDetailsLiveData
    }

    fun getSkuDetailBillingResult(): LiveData<BillingResult> {
        return mSkuDetailBillingResult
    }

    fun getPurchasesLiveData(): LiveData<List<Purchase>> {
        return mPurchasesLiveData
    }

    fun getRewardBillingResult(): LiveData<BillingResult> {
        return mRewardBillingResult
    }

    fun getStatusReward(): LiveData<Boolean> {
        return mStatusReward
    }

    fun getPurchasesUpdateLiveData(): LiveData<List<Purchase>> {
        return mPurchasesUpdateLiveData
    }

    fun getPurchasesUpdateBillingResult(): LiveData<BillingResult> {
        return mPurchaseUpdateBillingResult
    }


    fun getConsumePurchaseToken(): LiveData<String> {
        return mConsumePurchaseToken
    }

    fun getConsumeBillingResult(): LiveData<BillingResult> {
        return mConsumeBillingResult
    }

    override fun onCleared() {
        super.onCleared()
        endDataSourceConnection()
    }

    private fun isSubscriptionSupported(): Boolean {
        val billingResult = mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        return if (billingResult != null) {
            val responseCode = billingResult.responseCode
            if (responseCode == BillingClient.BillingResponseCode.OK) {
                true
            } else {
                Log.d(TAG, "isSubscriptionSupported(): error with responseCode:$responseCode")
                false
            }
        } else {
            false
        }
    }

    object ItemSku {
        const val UPDATE_PREMIUM = "update_premium"
        const val ITEM_HEART = "item_heart"
        const val ITEM_POWER = "item_power"
        const val REWARD_TEST = "android.test.reward"
        const val REWARD_GET_LIFE = "ads_get_life"
        const val GIFT_CODE_500K = "giftcode_500k"

        const val PREMIUM_WEEKLY = "premium_weekly"
        const val PREMIUM_MONTHLY = "premium_monthly"
        const val PREMIUM_YEARLY = "premium_yearly"

        val INAPP_SKUS = listOf(UPDATE_PREMIUM, ITEM_HEART, ITEM_POWER, REWARD_TEST, REWARD_GET_LIFE, GIFT_CODE_500K)
        val SUBS_SKUS = listOf(PREMIUM_WEEKLY, PREMIUM_MONTHLY, PREMIUM_YEARLY)
    }
}
