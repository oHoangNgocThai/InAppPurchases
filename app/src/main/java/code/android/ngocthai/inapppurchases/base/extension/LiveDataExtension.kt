package code.android.ngocthai.inapppurchases.base.extension

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import code.android.ngocthai.inapppurchases.base.ui.NonNullMediatorLiveData
import code.android.ngocthai.inapppurchases.base.ui.NonNullSingleMediatorLiveData


fun <T> LiveData<T>.nonNull(): NonNullMediatorLiveData<T> {
    val mediator: NonNullMediatorLiveData<T> = NonNullMediatorLiveData()
    mediator.addSource(this) { t ->
        t?.let {
            mediator.value = it
        }
    }

    return mediator
}

fun <T> NonNullMediatorLiveData<T>.observe(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, Observer {
        it?.let(observer)
    })
}

fun <T> LiveData<T>.nonNullSingle(): NonNullSingleMediatorLiveData<T> {
    val mediator: NonNullSingleMediatorLiveData<T> = NonNullSingleMediatorLiveData()
    mediator.addSource(this) { t ->
        t?.let {
            mediator.value = it
        }
    }

    return mediator
}

fun <T> NonNullSingleMediatorLiveData<T>.observe(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, Observer {
        it?.let(observer)
    })
}
