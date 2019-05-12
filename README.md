# InAppPurchases

# Overview

Để sử dụng chức năng thanh toán, phải thông qua các bước như sau:

* Có một ứng dụng được đăng lên **Google Play Console** với quyền BILLING. Ứng dụng phải ở trạng thái Publish hoặc là được publish qua **Open track**, **Closed track**, **Internal test track**.

* 


# Upload app to Google Play Console

## Build Signed Apk

* Để build signed Apk, trước hết hãy chắc chắn bạn đang ở Build Variant là release với các cài đặt tương ứng cho phiên bản này.
* Để thực hiện hãy truy cập Build -> Generate Signed Bundle/APK.
* Nếu là ứng dụng mới đưa lên chợ, chúng ta sẽ cần phải tạo 1 keystore mới. Lưu ý rằng khi đã tạo rồi mà để mất file này thì sẽ không thể update được ứng dụng cho phiên bản tiếp theo.
![](https://cdn-images-1.medium.com/max/800/0*uDKK6bSyT9c-v6gh)

*   Hoàn thiện keystore password rồi finish là đợi một lúc để Android Studio build.

## Upload app

* Sau khi đã có file Apk, việc tiếp theo là truy cập vào Google Play Console để setting upload ứng dụng. Để làm được việc này bạn cần phải mua lấy một tài khoản google develop. Có thể tham khảo ở [đây](https://support.magplus.com/hc/en-us/articles/204270878-Android-Setting-up-Your-Google-Play-Developer-Account)

* Truy cập vào [https://play.google.com/apps/publish/](https://play.google.com/apps/publish/) -> **All Application** -> **Create Application** -> **Enter title and create**.

* Hiện tại ứng dụng đang ở trạng thái draf, muốn test được chúng ta phải publish app. Mà không cần đẩy lên hẳn chợ, chỉ cần sử dụng Internal test track là đủ. Để vào đó cấu hình hãy truy cập đường dẫn **App Releases** -> **Internal test track** -> **Create Release** -> **Upload signed apk** -> **Save** - **Review**.  

* Còn bước cuối cùng để publish app ở dạng test, bạn cần phải hoàn thành một số mục như **Store listing**, **Content rating**, **Pricing & distribution** thì mới có thể hiện lên được button **START ROLLOUT TO BETA**. Tiếp đó đợi để có thể được review và publish app.
 
# Google Play Billing

## Use Google Play Billing

* Trước hết phải thêm dependencies của Google Play Billing như sau:

```
dependencies {
    ...
    implementation 'com.android.billingclient:billing:1.2.2'
}
```

* Thêm quyền billing cho ứng dụng bên trong AndroidManifest.xml 

```
<uses-permission android:name="com.android.vending.BILLING" />
```

* Gọi đến phương thức **newBuilder()** để tạo ra một instance của **BillingClient**, tiếp đến bạn cũng phải gọi đến phương thức **setListener** để lắng nghe được sự kiện của **PurchasesUpdatedListener** để nhận được những cập nhật purchases bởi ứng dụng của bạn.

* Thiết lập một kết nối đối với Google Play. Sử dụng phương thức **startConnection()** để bắt đầu kết nối và dữ liệu sẽ nhận về thông qua **BillingClientStateListener**.

* Override phương thức **onBillingServiceDisconnected()** và xử lý khi bị mất liên kết với Google Play. Ví dụ như kết nối có thể bị mất khi Google Play Store Service cập nhật bên trong background. Chính vì vậy nên cần phải gọi **startConnection()** để tạo lại một connection trước khi thực hiện lại 1 yêu cầu.

```
lateinit private var billingClient: BillingClient
...
billingClient = BillingClient.newBuilder(context).setListener(this).build()
billingClient.startConnection(object : BillingClientStateListener {
   override fun onBillingSetupFinished(@BillingResponse billingResponseCode: Int) {
       if (billingResponseCode == BillingResponse.OK) {
           // The BillingClient is ready. You can query purchases here.
       }
   }
   override fun onBillingServiceDisconnected() {
       // Try to restart the connection on the next request to
       // Google Play by calling the startConnection() method.
   }
})
```

## Item Product

### Lấy ra các thông tin in-app detail

* Duy nhất ID của sản phẩm mà bạn đã tạo khi định cấu hình bên trong ứng dụng. Nó được sử dụng để truy vấn một cách không đồng bộ từ Google Play chi tiết của ứng dụng, hãy sử dụng phương thức **querySkuDetailsAsync()**. Khi sử dụng phương thức này, hãy truyền một instance của **SkuDetailsParams** chỉ định danh sách các chuỗi ID sản phẩm và **SkuType**. SkuType có thể là **SkuType.INAPP** đối với sản phẩm một lần hoặc được thưởng hoặc **SkyType.SUBS** đối với trường hợp đăng ký.

* Để xử lý kết quả bạn phải implement interface **SkuDetailsResponseListener** sau đó override lại phương thức **onSkuDetailsResponse()** để tạo thông báo khi truy vấn kết thúc.

```
val skuList = listOf("thaihn_update_normal", "thaihn_update_premium")
val params = SkuDetailsParams
    .newBuilder()
    .setSkusList(skuList)
    .setType(BillingClient.SkuType.INAPP)
    .build()
    
billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
    if (responseCode == BillingClient.BillingResponse.OK) {
        // Handle response
    }
```

* Sau khi nhận được danh sách SkuDetail, thự hiện hiển thị các item đó ra dạng danh sách với các thôn tin như title, price. 

### Cho phép mua sản phẩm trong ứng dụng

* Một số điện thoại Android có thể không hỗ trợ một sản phẩm nhất định, ví dụ như các sản phẩm subscriptions. Do đó trước mỗi phiên thanh toán, bạn cần gọi phương thức **isFeatureSupported()** để có thể kiểm tra được điều kiện. Để xem được danh sách các loại sản phẩm, tìm hiểu thêm về [BillingClient.FeatureType](https://developer.android.com/reference/com/android/billingclient/api/BillingClient.FeatureType).

* Sau khi bắt sự kiện click vào 1 item nào đó để tiến hành mua sản phẩm, chúng ta sử dụng phương thức **launchBillingFlow()** để hiển thị flow thanh toán.

```
val billingFlowParam = BillingFlowParams
    .newBuilder()
    .setSkuDetails(item)
    .build()
billingClient.launchBillingFlow(this, billingFlowParam)
```

* Bạn nên sử dụng **PurchasesUpdatedListener** để lắng nghe nếu như có sự thay đổi khi mua hàng thành công. Nếu như bạn muốn người dùng có thể mua tiếp sản phẩm vừa mua, chỉ cần clear nó sau mỗi lần mua.

```
override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
    when(responseCode) {
            BillingClient.BillingResponse.OK -> {
                purchases?.let {
                    // Handle response success
                    allowMultiplePurchases(purchases)
                }
            }
            BillingClient.BillingResponse.USER_CANCELED -> {
                // Handle response cancel
            }
        }
}

private fun allowMultiplePurchases(purchases: MutableList<Purchase>?) {
        val purchase = purchases?.first()
        if (purchase != null) {
            billingClient.consumeAsync(purchase.purchaseToken) { responseCode, purchaseToken ->
                if (responseCode == BillingClient.BillingResponse.OK && purchaseToken != null) {
                    Log.d(TAG, "AllowMultiplePurchases success, responseCode: $responseCode")
                } else {
                    Log.d(TAG, "Can't allowMultiplePurchases, responseCode: $responseCode")
                }
            }
        }
    }
```

* Khi có sự thay đổi, thông thường phải mất 15 đến 20p mới có thể cập nhật được vì Google Play có cơ chế cache lại dữ liệu. Vì vậy nếu muốn cập nhật ngay tại app khi request, chúng ta có 2 cách sau:

    * Nếu bạn clear cache của ứng dụng Google Play trên thiết bị, sẽ có thể cập nhật được ngay. Nhưng rõ ràng cách này không ổn lắm. 
    * Vậy nên cách thứ 2 là clear history purchases của Google Play nếu cần, sử dụng phương thức **queryPurchases()**
    
    ```
    billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList
        .forEach {
            billingClient.consumeAsync(it.purchaseToken) { responseCode, purchaseToken ->
                if (responseCode == BillingClient.BillingResponse.OK && purchaseToken != null) {
                    println("onPurchases Updated consumeAsync, purchases token removed: $purchaseToken")
                } else {
                    println("onPurchases some troubles happened: $responseCode")
            }
        }
    }
    ```

### Test billing

* Để có thể test được, bạn cần thêm các tài khoản test vào phần internal test track. Chỉ có những email trong đó mới có thể tìm thấy ứng dụng. Nhưng trước hết bạn cần vào Developer Console App Releases → Alpha → Manage testers → Tìm Opt-in URL và gửi email test để họ chấp nhận.

* Sau khi thêm tài khoản tester, email này có thể thoải mái mua hàng mà không mất tiền.



## Subscription


