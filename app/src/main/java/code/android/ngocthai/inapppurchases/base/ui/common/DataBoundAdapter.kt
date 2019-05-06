package code.android.ngocthai.inapppurchases.base.ui.common

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

abstract class DataBoundAdapter<T> : RecyclerView.Adapter<DataBoundViewHolder>() {

    protected var items: MutableList<T> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder {
        return DataBoundViewHolder(inflateView(parent))
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder, position: Int) {
        bind(holder.itemView, items[position])
    }

    protected abstract fun inflateView(parent: ViewGroup): View

    protected abstract fun bind(rootView: View, item: T)

    override fun getItemCount(): Int {
        return items.size
    }

    // For implement
//    override fun inflateView(parent: ViewGroup): View {
//        return LayoutInflater.from(parent.context).inflate(R.layout.item_main_more, parent, false)
//    }
//
//    override fun bind(itemView: View, item: User) {
//
//    }
}
