package code.android.ngocthai.inapppurchases.base.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "skudetails")
data class ProductEntity(

        val canPurchase: Boolean, /* Not in SkuDetails; it's the augmentation */
        @PrimaryKey val productId: String,

        val type: String?,
        val price: String?,
        val priceAmountMicros: Long?,
        val priceCurrencyCode: String?,
        val title: String?,
        val description: String?,
        val skuDetailsToken: String,
        val originalJson: String?

)
