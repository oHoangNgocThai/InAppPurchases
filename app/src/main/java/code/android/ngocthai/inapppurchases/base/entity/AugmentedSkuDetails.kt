package code.android.ngocthai.inapppurchases.base.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class AugmentedSkuDetails(

        val canPurchase: Boolean, /* Not in SkuDetails; it's the augmentation */

        @PrimaryKey val sku: String,

        val type: String?,

        val price: String?,

        val title: String?,

        val description: String?,

        val originalJson: String?

)
