# InAppPurchases

# Use Google Play Billing

## Thêm Google Play Billing vào trong ứng dụng

### Cập nhật dependencies trong ứng dụng

```
dependencies {
    ...
    implementation 'com.android.billingclient:billing:1.2.2'
}
```

### Kết nối với Google Play

> Trước khi bạn có thể tạo được yêu cầu Google Play Billing, trước hết bạn phải thiết lập kết nối đối với Google Play.

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

### Lấy ra các thông tin in-app detail

* Duy nhất ID của sản phẩm mà bạn đã tạo khi định cấu hình bên trong ứng dụng. Nó được sử dụng để truy vấn một cách không đồng bộ từ Google Play chi tiết của ứng dụng, hãy sử dụng phương thức **querySkuDetailsAsync()**. Khi sử dụng phương thức này, hãy truyền một instance của **SkuDetailsParams** chỉ định danh sách các chuỗi ID sản phẩm và **SkuType**. SkuType có thể là **SkuType.INAPP** đối với sản phẩm một lần hoặc được thưởng hoặc **SkyType.SUBS** đối với trường hợp đăng ký.

* Để xử lý kết quả bạn phải implement interface **SkuDetailsResponseListener** sau đó override lại phương thức **onSkuDetailsResponse()** để tạo thông báo khi truy vấn kết thúc.

```

```

# Use Google Play Billing with AIDL

