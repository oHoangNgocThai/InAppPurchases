package code.android.ngocthai.inapppurchases.mainapp.ui.history

import android.support.v7.util.DiffUtil
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.common.ListBoundAdapter
import com.android.billingclient.api.PurchaseHistoryRecord
import kotlinx.android.synthetic.main.item_history.view.*

class HistoryAdapter : ListBoundAdapter<PurchaseHistoryRecord>(HistoryRecordDiffCallback()) {

    override fun inflateView(parent: ViewGroup, viewType: Int?): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
    }

    override fun bind(rootView: View, item: PurchaseHistoryRecord) {
        rootView.textTitle.text = item.sku
        rootView.textTime.text = "Time purchase: ${convertDate(item.purchaseTime)}"
    }

    private fun convertDate(timeMilliseconds: Long): String {
        return DateFormat.format("dd/MM/yyyy HH:mm:ss", timeMilliseconds).toString()
    }

    class HistoryRecordDiffCallback : DiffUtil.ItemCallback<PurchaseHistoryRecord>() {
        override fun areContentsTheSame(oldItem: PurchaseHistoryRecord, newItem: PurchaseHistoryRecord): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: PurchaseHistoryRecord, newItem: PurchaseHistoryRecord): Boolean {
            return oldItem.sku == newItem.sku
        }
    }
}
