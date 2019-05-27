package code.android.ngocthai.inapppurchases.mainapp.ui.history

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import code.android.ngocthai.inapppurchases.base.repository.BillingRepository
import com.android.billingclient.api.PurchaseHistoryRecord

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val TAG = HistoryViewModel::class.java.simpleName
    }

    private val mBillingRepository: BillingRepository

    private var mPurchaseHistoryRecordLiveData: LiveData<List<PurchaseHistoryRecord>>

    init {
        mBillingRepository = BillingRepository.getInstance(application)

        mBillingRepository.startDataSourceConnection()

        mPurchaseHistoryRecordLiveData = mBillingRepository.getPurchaseHistoryRecord()
    }

    fun clearHistory() {
        mBillingRepository.clearHistory()
    }

    fun getPurchaseHistoryRecord(): LiveData<List<PurchaseHistoryRecord>> = mPurchaseHistoryRecordLiveData

    override fun onCleared() {
        super.onCleared()
        mBillingRepository.endDataSourceConnection()
    }
}
