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
        ConsumeResponseListener, SkuDetailsResponseListener {


    companion object {
        private val TAG = BillingViewModel::class.java.simpleName
    }

    private lateinit var mBillingClient: BillingClient

    private val setupConnectionResponse = MutableLiveData<Int>()
    private val billingServiceDisconnected = MutableLiveData<Boolean>()

    private val mSkuDetailList = hashSetOf<SkuDetails>()
    private val mSkuDetailsLiveData = MutableLiveData<List<SkuDetails>>()
    private val mSkuDetailResponseCode = MutableLiveData<Int>()

    private val mPurchasesLiveData = MutableLiveData<List<Purchase>>()

    private val mStatusReward = MutableLiveData<Boolean>()
    private val mRewardResponseCode = MutableLiveData<Int>()

    init {
        startDataSourceConnection(application)
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected()")
        billingServiceDisconnected.value = true
    }

    override fun onBillingSetupFinished(responseCode: Int) {
        Log.d(TAG, "onBillingSetupFinished(): responseCode:$responseCode")
        setupConnectionResponse.value = responseCode
    }

    override fun onConsumeResponse(responseCode: Int, purchaseToken: String?) {
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {

    }

    override fun onSkuDetailsResponse(responseCode: Int, skuDetailsList: MutableList<SkuDetails>?) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            Log.d(TAG, "onSkuDetailResponse() success, list:$skuDetailsList")
        } else {
            Log.d(TAG, "onSkuDetailResponse() fail, responseCode:$responseCode")
            mSkuDetailResponseCode.value = responseCode
        }

        skuDetailsList?.let {
            if (it.isNotEmpty()) {
                mSkuDetailList.addAll(it)
                mSkuDetailsLiveData.value = mSkuDetailList.toList()
            }
        }
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
                .setListener(this)
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

    fun lauchBillingFlow(activity: Activity, skuDetail: SkuDetails) {
        if (skuDetail.isRewarded) {
            launchBillingReward(skuDetail)
        } else {
            launchBillingInApp(activity, skuDetail)
        }

    }

    private fun launchBillingReward(skuDetails: SkuDetails) {
        val params = RewardLoadParams.Builder()
                .setSkuDetails(skuDetails)
                .build()

        mBillingClient.loadRewardedSku(params) { responseCode ->
            Log.d(TAG, "lauchBillingReward responseCode:$responseCode")
            mRewardResponseCode.value = responseCode
            if (responseCode == BillingClient.BillingResponse.OK) {
                // Enable the reward product, or make
                // any necessary updates to the UI.
                mStatusReward.value = true
            } else {
                mStatusReward
            }
        }
    }

    private fun launchBillingInApp(activity: Activity, skuDetails: SkuDetails) {
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

    fun clearHistory() {
        mBillingClient.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList?.forEach {
            mBillingClient.consumeAsync(it.purchaseToken) { responseCode, purchaseToken ->
                if (responseCode == BillingClient.BillingResponse.OK && purchaseToken != null) {
                    Log.d(TAG, "onPurchases Updated consumeAsync, purchases token removed: $purchaseToken")
                } else {
                    Log.d(TAG, "onPurchases some troubles happened: $responseCode")
                }
            }
        }
    }

    fun getSetupConnectionResponse(): LiveData<Int> {
        return setupConnectionResponse
    }

    fun getSkuDetailLiveData(): LiveData<List<SkuDetails>> {
        return mSkuDetailsLiveData
    }

    fun getSkuDetailResponseCode(): LiveData<Int> {
        return mSkuDetailResponseCode
    }

    fun getPurchasesLiveData(): LiveData<List<Purchase>> {
        return mPurchasesLiveData
    }

    fun getRewardResponseCode(): LiveData<Int> {
        return mRewardResponseCode
    }

    override fun onCleared() {
        super.onCleared()
        endDataSourceConnection()
    }

    private fun isSubscriptionSupported(): Boolean {
        val responseCode = mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        if (responseCode != BillingClient.BillingResponse.OK) {
            Log.d(TAG, "isSubscriptionSupported() got an error response: $responseCode")
        }
        return responseCode == BillingClient.BillingResponse.OK
    }

    object ItemSku {
        const val UPDATE_PREMIUM = "update_premium"
        const val ITEM_HEART = "item_heart"
        const val ITEM_POWER = "item_power"
        const val REWARD_TEST = "android.test.reward"

        const val PREMIUM_WEEKLY = "premium_weekly"
        const val PREMIUM_MONTHLY = "premium_monthly"
        const val PREMIUM_YEARLY = "premium_yearly"

        val INAPP_SKUS = listOf(UPDATE_PREMIUM, ITEM_HEART, ITEM_POWER, REWARD_TEST)
        val SUBS_SKUS = listOf(PREMIUM_WEEKLY, PREMIUM_MONTHLY, PREMIUM_YEARLY)
    }
}
