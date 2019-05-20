package code.android.ngocthai.inapppurchases.mainapp

import android.app.Application
import com.google.gson.Gson

class MyApplication : Application() {

    var gSon: Gson? = null

    companion object {
        private var mSelf: MyApplication? = null
        fun self(): MyApplication {
            return mSelf!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        mSelf = this
        gSon = Gson()
    }
}
