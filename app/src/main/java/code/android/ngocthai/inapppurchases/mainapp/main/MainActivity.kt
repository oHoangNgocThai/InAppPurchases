package code.android.ngocthai.inapppurchases.mainapp.main

import android.content.Intent
import android.os.Bundle
import code.android.ngocthai.inapppurchases.R
import code.android.ngocthai.inapppurchases.base.ui.BaseActivity
import code.android.ngocthai.inapppurchases.mainapp.itemproduct.ProductActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override val layoutResource: Int
        get() = R.layout.activity_main


    override fun initComponent(savedInstanceState: Bundle?) {
        buttonItemProduct.setOnClickListener {
            startActivity(Intent(this, ProductActivity::class.java))
        }

        buttonReward.setOnClickListener {

        }

        buttonSubs.setOnClickListener {

        }
    }

}
