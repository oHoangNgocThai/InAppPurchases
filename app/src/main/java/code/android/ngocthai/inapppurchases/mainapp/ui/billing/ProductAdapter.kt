package code.android.ngocthai.inapppurchases.mainapp.ui.billing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.entity.AugmentedSkuDetails
import code.android.ngocthai.inapppurchases.base.ui.common.DataBoundAdapter
import com.android.billingclient.api.SkuDetails
import kotlinx.android.synthetic.main.item_product.view.*

class ProductAdapter(
        private val listener: ProductListener
) : DataBoundAdapter<AugmentedSkuDetails>() {

    interface ProductListener {
        fun onItemClick(item: AugmentedSkuDetails)

        fun onBuyClick(item: AugmentedSkuDetails)
    }

    override fun inflateView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
    }

    override fun bind(rootView: View, item: AugmentedSkuDetails) {
        rootView.textTitle.text = item.description
        rootView.textPrice.text = item.price

        rootView.setOnClickListener {
            listener.onBuyClick(item)
        }

        rootView.setOnClickListener {
            listener.onItemClick(item)
        }
    }

    fun updateData(newList: List<AugmentedSkuDetails>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
