# InAppPurchases

# Overview

* Google Play Billing là một dịch vụ bán nội dung số trên Android. Có thể vó dụ như những ứng dụng, nhạc, phim, sách, tin tức, ...

* Inapp-Billing là một dịch vụ cho phép bán nội dung số trong ứng dụng Android. Ví dụ các item trong game như máu, coin, ...

## In-app product types

Google Play Billing có thể sử dụng để bán một số loại sản phẩm như sau:

* One-time products: Một sản phẩm yêu cầu duy nhất một khoản phí, không định kì đối với hình thức thanh toán của người dùng. Ví dụ như là level trong game, mua tài khoản premium và một gói media nào đó đều thuộc loại one-time product. Google Play Console sử dụng **managed products** thay vì one-time product, và Google Play Billing gọi nó là **INAPP**.

* Reward products: Là một sản phẩm yêu cầu người dùng phải xem quảng cáo video. Có thể hiểu như tiền trong game và hoàn thành nhanh chóng các nhiệm vụ hay mua thêm mạng chơi đều thuộc về loại này. Google Play Console gọi đây là **rewarded products** và Google Play Billing xếp chúng vào loại **INAPP**.

* Subscription: Một sản phẩm yêu cầu một khoản phí định kì cho hình thức thanh toán của người dùng. Tạp chí trực tuyến, dịch vụ nghe nhạc hay xem truyền hình đều thuộc về loại này. Google Play Billing gọi nó thuộc dạng **SUBS**.

## Purchase token and order IDs

* Google Play Billing theo dõi các sản phẩm và giao dịch bằng cách sử dụng purchase token và order ID.

    * Purchase token là một mã đại diện cho quyền của người mua đối với sản phẩm trên Google Play. Nó chỉ ra rằng người dùng đã trả tiền cho một sản phẩm, được đại diển bởi **SKU**.

    * Order ID là một chuỗi string đại diện cho giao dịch tài chính trên Google Play. Chuỗi này gồm trong biên nhận được gửi qua email cho người mua và nhà phát triển bên thứ 3 sử dụng order ID để quản lý tiền hoàn lại trong phần **Order Management** của Google Play Console.

* Đối với các sản phẩm one-time product hoặc là reward product, mỗi lần mua sẽ tạo ra một Purchase token và Order ID mới.

* Đối với sản phẩm subscription, giao dịch mua lần đầu sẽ tạo purchase token và order ID cho đơn hàng. Mỗi kỳ thanh toán tiếp theo, purchase token vẫn giữ nguyên mà chỉ cấp mới Order ID cho đơn hàng. Nếu muốn nâng cấp, hạ xuống và đăng ký lại thì đều tạo ra ID và Token mới.

## In-app product configuration

**Managed Product** và **Subscription** có một số tùy chọn cấu hình phổ biến trong Google Play Console quan trọng sau:

* Title: Mô tả ngắn về sản phẩm trong ứng dụng.

* Description: Mô tả chi tiết hơn về sản phẩm trong ứng dụng.
    
* Product ID: Là ID duy nhất tương ứng với sản phẩm. Product ID cũng được gọi là **SKUs** trong thư viện Google Play Billing.
    
* Price / Default Price: Số tiền người dùng sẽ trả cho sản phẩm trong ứng dụng.

    * Đối với **one-time product** thì giá đại diện cho số tiền mà người dùng phải trả cho mỗi lần mua sản phẩm.
    * Đối với **reward product** thì không có giá mặc định và không đại diện cho giá mà quảng cáo người dùng xem.
    * Đối với **subscription** thì giá được đại diện cho một chu kì thanh toán thông thường(cũng có thể có giá dùng thử và miễn phí bao nhiêu ngày).
    
Một ứng dụng có thể có sẵn nhiều sản phẩm trong ứng dụng để mua, mỗi sản phẩm có ID và giá khác nhau. Google Play Console cung cấp các mẫu giá được sử dụng để dễ dàng định cấu hình 1 số sản phẩm có cùng mức giá.

Bạn cũng có thể tạo Promo code để người dùng sử dụng nhận one-time product miễn phí. Người dùng có thể nhập mã khuyến mại trong ứng dụng hoặc trong cửa hàng Google Play để nhận sản phầm one-time miễn phí.

## Subscription product configuration

* Billing period: Tần suất mà người dùng bị tính phí trong khi đăng ký của họ đang hoạt động. Bạn có thể lựa chọn hàng tuần, hàng tháng, 3 tháng, 6 tháng và hàng năm.

* Free trial period: Một khoảng thời gian mà người dùng có thể truy cập vào một subscription mà không được lập hóa đơn. Thời gian này dùng để lôi kéo nguời dùng. Sau đó đến thời hạn tài khoản sẽ tự bị trừ tiền nếu chưa hủy.

* Introduce price: Giá của thuê bao subs qua một số thời hạn thanh toán ban đầu, giới thiệu trực tuyến. Giá giới thiệu phải thấp hơn giá bình thường để thu hút người dùng.

* Grace period: Thời gian xử lý này sẽ giúp cho người dùng có thêm thời gian để khắc phục vấn đề thanh toán của họ. Giả sử như thử tín dụng bị từ chối trong lúc thanh toán.

* Account hold: Trạng thái subs có thể nhập khi người dùng không cập nhật hình thức thanh toán của họ trong thời gian Grace period. Việc giữ tài khoản kéo dài 30 ngày trong thời gian mà nội dung đăng ký không có sẵn cho người dùng.

Bạn có thể tham khảo thêm việc tạo subs tại [đây](https://support.google.com/googleplay/android-developer/answer/140504)


## Step flow implement

Để thực hiên việc thêm thanh toán vào trong ứng dụng của bạn sẽ trả qua một số bước cơ bản sau đây.

#### Step 1: Create file Apk

* Build App phiên bản release, trong đó có chứa quyền BILLING.

#### Step 2: Upload app to Google Play Console

* Bạn cần có một tải khoản Google develop, sau đó đưa ứng dụng của bạn lên trên đó để quản lý, có thể đẩy bản release hoặc chỉ là test. 

* Ứng dụng phải được publish qua các phiên bản Alpha, Beta hoặc là Internal test

#### Step 3: Prepare product and subscription

* Tạo các sản phẩm của **managed product**, **subscription**, **reward product** muốn đưa vào trong ứng dụng.

* Hướng dẫn chi tiết tạo các sản phẩm ở [đây](https://developer.android.com/google/play/billing/billing_overview#next-steps)

#### Step 4: Implement code

* Thực hiện connect với Google Play Billing, hiển thị các item product và thực hiện việc hiển thị Billing Flow.

#### Step 5: Test Play Billing

* Thực hiện thêm tài khoản google test và test trên các thiết bị để tránh bị mất tiền khi test.


# Upload app to Google Play Console

## Build Signed Apk

* Để build signed Apk, trước hết hãy chắc chắn bạn đang ở Build Variant là release với các cài đặt tương ứng cho phiên bản này.

* Chắc rằng ứng dụng của bạn đã có quyền `<uses-permission android:name="com.android.vending.BILLING" />` để yêu cầu quyền thanh toán.

* Để thực hiện hãy truy cập Build -> Generate Signed Bundle/APK.

* Nếu là ứng dụng mới đưa lên chợ, chúng ta sẽ cần phải tạo 1 keystore mới. Lưu ý rằng khi đã tạo rồi mà để mất file này thì sẽ không thể update được ứng dụng cho phiên bản tiếp theo.

![](https://cdn-images-1.medium.com/max/800/0*uDKK6bSyT9c-v6gh)

*   Hoàn thiện keystore password rồi finish là đợi một lúc để Android Studio build.

## Upload app

* Sau khi đã có file Apk, việc tiếp theo là truy cập vào Google Play Console để setting upload ứng dụng. Để làm được việc này bạn cần phải mua lấy một tài khoản google develop. Có thể tham khảo ở [đây](https://support.magplus.com/hc/en-us/articles/204270878-Android-Setting-up-Your-Google-Play-Developer-Account)

* Truy cập vào [https://play.google.com/apps/publish/](https://play.google.com/apps/publish/) -> **All Application** -> **Create Application** -> **Enter title and create**.

* Hiện tại ứng dụng đang ở trạng thái draf, muốn test được chúng ta phải publish app. Mà không cần đẩy lên hẳn chợ, chỉ cần sử dụng Internal test track là đủ. Để vào đó cấu hình hãy truy cập đường dẫn **App Releases** -> **Internal test track** -> **Create Release** -> **Upload signed apk** -> **Save** - **Review**.  

* Còn bước cuối cùng để publish app ở dạng test, bạn cần phải hoàn thành một số mục như **Store listing**, **Content rating**, **Pricing & distribution** thì mới có thể hiện lên được button **START ROLLOUT TO BETA**. Tiếp đó đợi để có thể được review và publish app.


# Implement Google Play Billing

## Connect to Google Play Billing

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

## Query SkuDetail product

* Muốn lấy được các sản phẩm mong muốn, trước hết phải xác định xem cần những item nào và Product ID của nó. Lưu ý thông thường để không phải cập nhật ứng dụng, những ID này nên được lưu ở server và tải về.

```
val skuList = listOf("thaihn_update_normal", "thaihn_update_premium")
```

* Bởi vì có các loại product type khác nhau như **INAPP** và **SUBS** nhưng lại trả về cùng một đối tượng **SkuDetails**, vì vậy muốn lấy ra loại nào, chúng ta sẽ sử dung hàm **setType()** tương ứng khi tạo **kuDetailsParams**.

```
val params = SkuDetailsParams
    .newBuilder()
    .setSkusList(skuList)
    .setType(BillingClient.SkuType.INAPP)
//  .setType(BillingClient.SkuType.SUBS)
    .build()
```

* Sau đó lấy ra thông tin các SkuDetails thông qua phương thức **querySkuDetailsAsync()** của BillingClient. Kết quả sẽ được lắng nghe bởi **SkuDetailsResponseListener** và trả về qua phương thức **onSkuDetailsResponse()**. 

```
mBillingClient.querySkuDetailsAsync(params, this)

override fun onSkuDetailsResponse(@BillingClient.BillingResponse responseCode: Int, skuDetailsList: MutableList<SkuDetails>?) {
        // do something
    }
```

* Dựa vào response code để xử lý kết quả nhận được. Tham khảo ResponseCode ở [đây](https://developer.android.com/reference/com/android/billingclient/api/BillingClient.BillingResponse)

```
when(responseCode) {
    BillingClient.BillingResponse.OK-> {
        Log.d(TAG, "onSkuDetailResponse() success, list:$skuDetailsList")
        skuDetailsList?.let {
            if (it.isNotEmpty()) {
                mSkuDetailList.addAll(it)
                // Update ui or do something
            }
        }
    }
    else -> {
        Log.d(TAG, "onSkuDetailResponse() fail, responseCode:$responseCode")
    }
}
```
> Thông thường chúng ta nên lưu lại dữ liệu đã thanh toán ở local hoặc là server của mình, nên việc chuyển đối tượng SkuDetails sang một đối tượng có thêm các thuộc tính cần thiết.

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

## Subscription

* Trước hết phải tạo subscription ở trên Google Play Console, chi tiết tại [đây](https://support.google.com/googleplay/android-developer/answer/140504?hl=en)



# Test your app

* Khi app được publish ở dạng **Internal test**, chỉ những email nào nằm trong danh sách mới tìm thấy và phải join vào trương trình test bằng cách vào Developer Console App Releases → Alpha → Manage testers → Tìm Opt-in URL và gửi email test để họ chấp nhận.


