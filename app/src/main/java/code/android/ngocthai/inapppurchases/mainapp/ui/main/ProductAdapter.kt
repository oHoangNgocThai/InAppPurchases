package code.android.ngocthai.inapppurchases.mainapp.ui.main

import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.entity.AugmentedSkuDetails
import code.android.ngocthai.inapppurchases.base.ui.common.ListBoundAdapter
import kotlinx.android.synthetic.main.item_product.view.*

class ProductAdapter(
        private val listener: ProductListener
) : ListBoundAdapter<AugmentedSkuDetails>(SkuDetailsDiffCallback()) {

    interface ProductListener {
        fun onBuyClick(item: AugmentedSkuDetails)
    }

    override fun inflateView(parent: ViewGroup, viewType: Int?): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
    }

    override fun bind(rootView: View, item: AugmentedSkuDetails) {
        rootView.textTitle.text = item.title?.substring(0, item.title.indexOf("("))
        rootView.textPrice.text = item.price

        if (item.canPurchase) {
            rootView.buttonBuy.setBackgroundColor(ContextCompat.getColor(rootView.context, R.color.colorButtonBuyActive))
        } else {
            rootView.buttonBuy.setBackgroundColor(ContextCompat.getColor(rootView.context, R.color.colorButtonBuyInActive))
        }

        rootView.buttonBuy.setOnClickListener {
            listener.onBuyClick(item)
        }
    }

    class SkuDetailsDiffCallback : DiffUtil.ItemCallback<AugmentedSkuDetails>() {
        override fun areContentsTheSame(oldItem: AugmentedSkuDetails, newItem: AugmentedSkuDetails): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: AugmentedSkuDetails, newItem: AugmentedSkuDetails): Boolean {
            return oldItem.sku == newItem.sku
        }
    }
}
