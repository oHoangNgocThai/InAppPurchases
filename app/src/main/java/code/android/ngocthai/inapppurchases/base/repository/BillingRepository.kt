package code.android.ngocthai.inapppurchases.base.repository

import android.app.Application
import android.util.Log
import code.android.ngocthai.inapppurchases.base.repository.BillingRepository.ItemSku.INAPP_SKUS
import code.android.ngocthai.inapppurchases.base.repository.BillingRepository.ItemSku.SUBS_SKUS
import code.android.ngocthai.inapppurchases.base.repository.BillingRepository.RetryPolicies.connectionRetryPolicy
import code.android.ngocthai.inapppurchases.base.repository.BillingRepository.RetryPolicies.resetConnectionRetryPolicyCounter
import code.android.ngocthai.inapppurchases.base.repository.BillingRepository.RetryPolicies.taskExecutionRetryPolicy
import code.android.ngocthai.inapppurchases.mainapp.util.BillingResponseCode
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

class BillingRepository private constructor(val application: Application) :
        PurchasesUpdatedListener, BillingClientStateListener,
        ConsumeResponseListener, SkuDetailsResponseListener {

    lateinit private var mBillingClient: BillingClient

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected")
        connectionRetryPolicy { connectToPlayBillingService() }
    }

    override fun onBillingSetupFinished(responseCode: Int) {
        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                Log.d(TAG, "onBillingSetupFinished successfully")
                resetConnectionRetryPolicyCounter()
                querySkuDetailsAsync(BillingClient.SkuType.INAPP, ItemSku.INAPP_SKUS)
                querySkuDetailsAsync(BillingClient.SkuType.SUBS, ItemSku.SUBS_SKUS)
                queryPurchasesAsync()
            }
            BillingClient.BillingResponse.BILLING_UNAVAILABLE -> {
                Log.d(TAG, "onBillingSetupFinished but billing is not available on this device")
            }
            else -> {
                Log.d(TAG, "onBillingSetupFinished with failure response code: $responseCode")
            }
        }
    }

    override fun onConsumeResponse(responseCode: Int, purchaseToken: String?) {
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
    }

    override fun onSkuDetailsResponse(responseCode: Int, skuDetailsList: MutableList<SkuDetails>?) {
    }

    private fun connectToPlayBillingService(): Boolean {
        Log.d(TAG, "connectToPlayBillingService")
        if (!mBillingClient.isReady) {
            mBillingClient.startConnection(this)
            return true
        }
        return false
    }

    fun startDataSourceConnection() {
        Log.d(TAG, "startDataSourceConnection")
        instantiateAndroidConnectToPlayBillingService()
        // Create data source in here
    }

    private fun instantiateAndroidConnectToPlayBillingService() {
        mBillingClient = BillingClient.newBuilder(application.applicationContext)
                .setListener(this)
                .build()
        connectToPlayBillingService()
    }

    fun queryPurchasesAsync() {
        fun task() {
            Log.d(TAG, "queryPurchasesAsync called")
            val purchasesResult = HashSet<Purchase>()
            var result = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP)
            result?.purchasesList?.apply {
                purchasesResult.addAll(this)
            }
            Log.d(TAG, "queryPurchasesAsync INAPP result: ${result?.purchasesList}")

            if (isSubscriptionSupported()) {
                result = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS)
                result?.purchasesList?.apply {
                    purchasesResult.addAll(this)
                }
                Log.d(TAG, "queryPurchasesAsunc SUBS result: ${result?.purchasesList}")
            }
        }

        taskExecutionRetryPolicy(mBillingClient, this) { task() }
    }

    private fun querySkuDetailsAsync(@BillingClient.SkuType skuType: String, skuList: List<String>) {
        val params = SkuDetailsParams.newBuilder()
                .setType(skuType)
                .setSkusList(skuList)
                .build()
        taskExecutionRetryPolicy(mBillingClient, this) {
            Log.d(TAG, "querySkuDetailsAsync for $skuType")
            mBillingClient.querySkuDetailsAsync(params, this)
        }
    }

    private fun isSubscriptionSupported(): Boolean {
        val responseCode = mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        return if (responseCode != BillingResponseCode.OK.code) {
            Log.d(TAG, "isSubscriptionSupported() got an error response $responseCode")
            false
        } else {
            true
        }
    }

    companion object {
        private val TAG = BillingRepository::class.java.simpleName

        @Volatile
        private var INSTANCE: BillingRepository? = null

        fun getInstance(application: Application): BillingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingRepository(application).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * This private object class shows an example retry policies. You may choose to replace it with
     * your own policies.
     */
    object RetryPolicies {

        private val TAG = RetryPolicies::class.java.simpleName

        private val maxRetry = 5
        private var retryCounter = AtomicInteger(1)
        private val baseDelayMillis = 500
        private val taskDelay = 2000L

        fun resetConnectionRetryPolicyCounter() {
            retryCounter.set(1)
        }

        /**
         * This works because it actually makes one call. Then it waits for success or failure.
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

    /**
     * [INAPP_SKUS], [SUBS_SKUS], [CONSUMABLE_SKUS]:
     *
     * Where you define these lists is quite truly up to you. If you don't need customization, then
     * it makes since to define and hardcode them here, as I am doing. Keep simple things simple.
     * But there are use cases where you may need customization:
     *
     * - If you don't want to update your APK (or Bundle) each time you change your SKUs, then you
     *   may want to load these lists from your secure server.
     *
     * - If your design is such that users can buy different items from different Activities or
     * Fragments, then you may want to define a list for each of those subsets. I only have two
     * subsets: INAPP_SKUS and SUBS_SKUS
     */
    private object ItemSku {
        const val UPDATE_PREMIUM = "update_premium"
        const val ITEM_HEART = "item_heart"
        const val ITEM_POWER = "item_power"

        const val PREMIUM_WEEKLY = "premium_weekly"
        const val PREMIUM_MONTHLY = "premium_monthly"
        const val PREMIUM_YEARLY = "premium_yearly"

        val INAPP_SKUS = listOf(UPDATE_PREMIUM, ITEM_HEART, ITEM_POWER)
        val SUBS_SKUS = listOf(PREMIUM_WEEKLY, PREMIUM_MONTHLY, PREMIUM_YEARLY)
    }
}
