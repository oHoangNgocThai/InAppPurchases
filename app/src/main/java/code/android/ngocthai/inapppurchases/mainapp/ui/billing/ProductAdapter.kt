package code.android.ngocthai.inapppurchases.mainapp.ui.billing

import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.entity.AugmentedSkuDetails
import code.android.ngocthai.inapppurchases.base.ui.common.DataBoundAdapter
import kotlinx.android.synthetic.main.item_product.view.*

class ProductAdapter(
        private val listener: ProductListener
) : DataBoundAdapter<AugmentedSkuDetails>() {

    interface ProductListener {
        fun onBuyClick(item: AugmentedSkuDetails)
    }

    override fun inflateView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
    }

    override fun bind(rootView: View, item: AugmentedSkuDetails) {
        rootView.textTitle.text = item.description
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

    fun updateData(newList: List<AugmentedSkuDetails>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun getAllData(): MutableList<AugmentedSkuDetails> {
        return items
    }
}
