package code.android.ngocthai.inapppurchases.base.ui.common

import android.support.v7.recyclerview.extensions.AsyncDifferConfig
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.view.View
import android.view.ViewGroup
import java.util.concurrent.Executors

abstract class ListBoundAdapter<T>(diffCallback: DiffUtil.ItemCallback<T>) : ListAdapter<T, DataBoundViewHolder>(
    AsyncDifferConfig.Builder<T>(diffCallback)
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
        .build()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder {
        return DataBoundViewHolder(inflateView(parent, viewType))
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder, position: Int) {
        bind(holder.itemView, getItem(position))
    }

    protected abstract fun inflateView(parent: ViewGroup, viewType: Int? = 0): View

    protected abstract fun bind(rootView: View, item: T)

    // For implement
//    override fun inflateView(parent: ViewGroup, viewType: Int?): View {
//        return LayoutInflater.from(parent.context).inflate(R.layout.item_main_more, parent, false)
//    }
//
//    override fun bind(rootView: View, item: User) {
//        rootView.linearIconParent.gone()
//    }
//
//    class UsersDiffCallback : DiffUtil.ItemCallback<User>() {
//        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
//            return oldItem == newItem
//        }
//
//        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
//            return oldItem.id == newItem.id
//        }
//    }
}
