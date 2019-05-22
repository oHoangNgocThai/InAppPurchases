package code.android.ngocthai.inapppurchases.mainapp.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.extension.nonNullSingle
import code.android.ngocthai.inapppurchases.base.extension.observe
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity

class MainActivity : BaseActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override val layoutResource: Int
        get() = R.layout.activity_main

    private lateinit var mViewModel: MainViewModel

    override fun initComponent(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        mViewModel.getSkuDetailsLiveData()
                .nonNullSingle()
                .observe(this) {
                    Log.d(TAG, "SkuList:$it")
                }

        mViewModel.getSkuDetailError()
                .nonNullSingle()
                .observe(this) {
                    // Handle error
                }
    }
}
