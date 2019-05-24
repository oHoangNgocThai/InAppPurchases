package code.android.ngocthai.inapppurchases.base.repository.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import code.android.ngocthai.inapppurchases.base.entity.AugmentedSkuDetails

@Database(
        entities = [
            AugmentedSkuDetails::class
        ],
        version = 1,
        exportSchema = false
)

abstract class LocalBillingDb : RoomDatabase() {

    abstract fun skuDetailsDao(): AugmentedSkuDetailDao

    companion object {

        @Volatile
        private var INSTANCE: LocalBillingDb? = null

        private val DATABASE_NAME = "purchase_db"

        fun getInstance(context: Context): LocalBillingDb =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context.applicationContext).also {
                        INSTANCE = it
                    }
                }

        private fun buildDatabase(appContext: Context): LocalBillingDb {
            return Room.databaseBuilder(appContext, LocalBillingDb::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}
