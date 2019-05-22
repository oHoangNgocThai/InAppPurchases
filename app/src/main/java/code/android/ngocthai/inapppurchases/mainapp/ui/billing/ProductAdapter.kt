package code.android.ngocthai.inapppurchases.mainapp.ui.billing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.common.DataBoundAdapter
import com.android.billingclient.api.SkuDetails
import kotlinx.android.synthetic.main.item_product.view.*

class ProductAdapter(
        private val listener: ProductListener
) : DataBoundAdapter<SkuDetails>() {

    interface ProductListener {
        fun onItemClick(item: SkuDetails)

        fun onBuyClick(item: SkuDetails)
    }

    override fun inflateView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
    }

    override fun bind(rootView: View, item: SkuDetails) {
        rootView.textTitle.text = item.description
        rootView.textPrice.text = item.price

        rootView.setOnClickListener {
            listener.onBuyClick(item)
        }

        rootView.setOnClickListener {
            listener.onItemClick(item)
        }
    }

    fun updateData(newList: List<SkuDetails>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
