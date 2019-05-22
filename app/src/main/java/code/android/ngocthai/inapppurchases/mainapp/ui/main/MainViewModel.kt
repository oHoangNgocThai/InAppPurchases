package code.android.ngocthai.inapppurchases.mainapp.ui.main

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails

class MainViewModel(application: Application) : BaseBillingViewModel(application) {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

    private val mSkuDetailsEntity = hashSetOf<SkuDetails>()
    private val mSkuDetailsLiveData = MutableLiveData<List<SkuDetails>>()
    private val mSkuDetailsError = MutableLiveData<BillingResult>()

    override val skuListInApp: List<String>
        get() = PurchaseConfig.INAPP_SKUS

    override val skuListSubs: List<String>
        get() = arrayListOf()

    override fun billingServiceDisconnected() {
        Log.d(TAG, "billingServiceDisconnected()")
    }

    override fun billingSetupFinished(billingResult: BillingResult?) {
        Log.d(TAG, "billingSetupFinished(): result:$billingResult")
    }

    override fun purchasesUpdate(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        Log.d(TAG, "purchasesUpdate(): result:$billingResult -- purchases:$purchases")
    }

    override fun skuDetailsResponse(billingResult: BillingResult?, skuDetailsList: MutableList<SkuDetails>?) {
        Log.d(TAG, "skuDetailsResponse(): result:$billingResult --- skuDetailsList: $skuDetailsList")
        billingResult?.let {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "skuDetailsResponse(): ${billingResult?.debugMessage}")
                    skuDetailsList?.let {
                        mSkuDetailsEntity.addAll(it)
                        mSkuDetailsLiveData.value = mSkuDetailsEntity.toList()
                    }
                }
                else -> {
                    Log.d(TAG, "skuDetailsResponse(): ${billingResult.debugMessage}")
                    mSkuDetailsError.value = billingResult
                }
            }
        }
    }

    fun getSkuDetailsLiveData(): LiveData<List<SkuDetails>> = mSkuDetailsLiveData

    fun getSkuDetailError(): LiveData<BillingResult> = mSkuDetailsError
}
