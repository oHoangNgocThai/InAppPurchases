package code.android.ngocthai.inapppurchases.mainapp.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.common.DataBoundAdapter
import com.android.billingclient.api.SkuDetails
import kotlinx.android.synthetic.main.item_price.view.*

class PriceAdapter : DataBoundAdapter<SkuDetails>() {

    override fun inflateView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_price, parent, false)
    }

    override fun bind(rootView: View, item: SkuDetails) {
        rootView.textTitle.text = item.description
    }
}
