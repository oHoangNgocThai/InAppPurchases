package code.android.ngocthai.inapppurchases.base.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity


abstract class BaseActivity : AppCompatActivity() {

    abstract val layoutResource: Int

    abstract fun initComponent(savedInstanceState: Bundle?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResource)
        initComponent(savedInstanceState)
    }
}
