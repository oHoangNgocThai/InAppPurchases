package code.android.ngocthai.inapppurchases.base.repository.local

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import code.android.ngocthai.inapppurchases.base.entity.AugmentedSkuDetails
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails

@Dao
interface AugmentedSkuDetailDao {

    @Query("SELECT * FROM AugmentedSkuDetails WHERE type = '${BillingClient.SkuType.SUBS}'")
    fun getSubscriptionSkuDetails(): LiveData<List<AugmentedSkuDetails>>

    @Query("SELECT * FROM AugmentedSkuDetails WHERE type = '${BillingClient.SkuType.INAPP}'")
    fun getInAppSkuDetails(): LiveData<List<AugmentedSkuDetails>>

    @Query("SELECT * FROM AugmentedSkuDetails")
    fun getSkuDetails(): LiveData<List<AugmentedSkuDetails>>

    @Transaction
    fun insertOrUpdate(skuDetails: SkuDetails) = skuDetails.apply {
        val result = getSkuDetailsById(sku)
        val canPurchase = result?.canPurchase ?: true
        val originalJson = toString().substring("SkuDetails: ".length)
        val detail = AugmentedSkuDetails(canPurchase, sku, type, price, title, description, originalJson)
        insert(detail)
    }

    @Transaction
    fun insertOrUpdate(sku: String, canPurchase: Boolean) {
        val result = getSkuDetailsById(sku)
        if (result != null) {
            update(sku, canPurchase)
        } else {
            insert(AugmentedSkuDetails(canPurchase, sku, null, null, null, null, null))
        }
    }

    @Query("SELECT * FROM AugmentedSkuDetails WHERE sku = :sku")
    fun getSkuDetailsById(sku: String): AugmentedSkuDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(augmentedSkuDetails: AugmentedSkuDetails)

    @Query("UPDATE AugmentedSkuDetails SET canPurchase = :canPurchase WHERE sku = :sku")
    fun update(sku: String, canPurchase: Boolean)
}
