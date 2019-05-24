package code.android.ngocthai.inapppurchases.mainapp.ui.main

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import code.android.ngocthai.inapppurchases.base.entity.AugmentedSkuDetails
import code.android.ngocthai.inapppurchases.base.repository.BillingRepository

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

    private val mBillingRepository: BillingRepository

    private var mAugmentedSkuDetailsLiveData: LiveData<List<AugmentedSkuDetails>>

    private var mConsumePurchaseToken = MutableLiveData<String>()
    private var mNonConsumePurchaseToken = MutableLiveData<String>()

    init {
        mBillingRepository = BillingRepository.getInstance(application)

        // setup SkuList
        mBillingRepository.mSkuListInApp = BillingRepository.PurchaseConfig.INAPP_SKUS
        mBillingRepository.mSkuListSubs = BillingRepository.PurchaseConfig.SUBS_SKUS

        // Start connection
        mBillingRepository.startDataSourceConnection()

        mAugmentedSkuDetailsLiveData = mBillingRepository.getAugmentedSkuDetails()
        mConsumePurchaseToken = mBillingRepository.getConsumePurchaseToken()
        mNonConsumePurchaseToken = mBillingRepository.getNonConsumePurchaseToken()
    }

    fun launchBilling(activity: Activity, augmentedSkuDetails: AugmentedSkuDetails) {
        mBillingRepository.launchBillingFlow(activity, augmentedSkuDetails)
    }

    fun getSkuDetails(): LiveData<List<AugmentedSkuDetails>> = mAugmentedSkuDetailsLiveData

    fun getConsumePurchaseToken(): LiveData<String> = mConsumePurchaseToken

    fun getNonConsumePurchaseToken(): LiveData<String> = mNonConsumePurchaseToken

}
