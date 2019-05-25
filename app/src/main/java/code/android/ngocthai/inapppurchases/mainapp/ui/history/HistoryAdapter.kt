package code.android.ngocthai.inapppurchases.mainapp.ui.history

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.common.DataBoundAdapter
import com.android.billingclient.api.PurchaseHistoryRecord
import kotlinx.android.synthetic.main.item_history.view.*

class HistoryAdapter : DataBoundAdapter<PurchaseHistoryRecord>() {

    override fun inflateView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
    }

    override fun bind(rootView: View, item: PurchaseHistoryRecord) {
        rootView.textTitle.text = item.sku
        rootView.textTime.text = "Time purchase: ${convertDate(item.purchaseTime)}"
    }

    private fun convertDate(timeMilliseconde: Long): String {
        return DateFormat.format("dd/MM/yyyy HH:mm:ss", timeMilliseconde).toString()
    }

    fun updateAllData(newList: List<PurchaseHistoryRecord>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
