package code.android.ngocthai.inapppurchases.mainapp.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity

class MainActivity : BaseActivity() {

    override val layoutResource: Int
        get() = R.layout.activity_main

    private lateinit var mViewModel: MainActivityViewModel

    override fun initComponent(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

}
