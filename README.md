# InAppPurchases

## Overview

* Google Play Billing là một dịch vụ bán nội dung số trên Android. Có thể vó dụ như những ứng dụng, nhạc, phim, sách, tin tức, ...

* Inapp-Billing là một dịch vụ cho phép bán nội dung số trong ứng dụng Android. Ví dụ các item trong game như máu, coin, ...

#### In-app product types

Google Play Billing có thể sử dụng để bán một số loại sản phẩm như sau:

* One-time products: Một sản phẩm yêu cầu duy nhất một khoản phí, không định kì đối với hình thức thanh toán của người dùng. Ví dụ như là level trong game, mua tài khoản premium và một gói media nào đó đều thuộc loại one-time product. Google Play Console sử dụng **managed products** thay vì one-time product, và Google Play Billing gọi nó là **INAPP**.

* Reward products: Là một sản phẩm yêu cầu người dùng phải xem quảng cáo video. Có thể hiểu như tiền trong game và hoàn thành nhanh chóng các nhiệm vụ hay mua thêm mạng chơi đều thuộc về loại này. Google Play Console gọi đây là **rewarded products** và Google Play Billing xếp chúng vào loại **INAPP**.

* Subscription: Một sản phẩm yêu cầu một khoản phí định kì cho hình thức thanh toán của người dùng. Tạp chí trực tuyến, dịch vụ nghe nhạc hay xem truyền hình đều thuộc về loại này. Google Play Billing gọi nó thuộc dạng **SUBS**.

#### Purchase token and order IDs

* Google Play Billing theo dõi các sản phẩm và giao dịch bằng cách sử dụng purchase token và order ID.

    * Purchase token là một mã đại diện cho quyền của người mua đối với sản phẩm trên Google Play. Nó chỉ ra rằng người dùng đã trả tiền cho một sản phẩm, được đại diển bởi **SKU**.

    * Order ID là một chuỗi string đại diện cho giao dịch tài chính trên Google Play. Chuỗi này gồm trong biên nhận được gửi qua email cho người mua và nhà phát triển bên thứ 3 sử dụng order ID để quản lý tiền hoàn lại trong phần **Order Management** của Google Play Console.

* Đối với các sản phẩm one-time product hoặc là reward product, mỗi lần mua sẽ tạo ra một Purchase token và Order ID mới.

* Đối với sản phẩm subscription, giao dịch mua lần đầu sẽ tạo purchase token và order ID cho đơn hàng. Mỗi kỳ thanh toán tiếp theo, purchase token vẫn giữ nguyên mà chỉ cấp mới Order ID cho đơn hàng. Nếu muốn nâng cấp, hạ xuống và đăng ký lại thì đều tạo ra ID và Token mới.

#### In-app product configuration

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

#### Subscription product configuration

* Billing period: Tần suất mà người dùng bị tính phí trong khi đăng ký của họ đang hoạt động. Bạn có thể lựa chọn hàng tuần, hàng tháng, 3 tháng, 6 tháng và hàng năm.

* Free trial period: Một khoảng thời gian mà người dùng có thể truy cập vào một subscription mà không được lập hóa đơn. Thời gian này dùng để lôi kéo nguời dùng. Sau đó đến thời hạn tài khoản sẽ tự bị trừ tiền nếu chưa hủy.

* Introduce price: Giá của thuê bao subs qua một số thời hạn thanh toán ban đầu, giới thiệu trực tuyến. Giá giới thiệu phải thấp hơn giá bình thường để thu hút người dùng.

* Grace period: Thời gian xử lý này sẽ giúp cho người dùng có thêm thời gian để khắc phục vấn đề thanh toán của họ. Giả sử như thử tín dụng bị từ chối trong lúc thanh toán.

* Account hold: Trạng thái subs có thể nhập khi người dùng không cập nhật hình thức thanh toán của họ trong thời gian Grace period. Việc giữ tài khoản kéo dài 30 ngày trong thời gian mà nội dung đăng ký không có sẵn cho người dùng.

Bạn có thể tham khảo thêm việc tạo subs tại [đây](https://support.google.com/googleplay/android-developer/answer/140504)


#### Step flow implement

Để thực hiên việc thêm thanh toán vào trong ứng dụng của bạn sẽ trả qua một số bước cơ bản sau đây.

###### Step 1: Implement Google Play Console Library

* Thực hiện connect với Google Play Billing, hiển thị các item product và thực hiện việc hiển thị Billing Flow.

###### Step 2: Create file Apk

* Build App phiên bản release, trong đó có chứa quyền BILLING.

###### Step 3: Upload app to Google Play Console

* Bạn cần có một tải khoản Google develop, sau đó đưa ứng dụng của bạn lên trên đó để quản lý, có thể đẩy bản release hoặc chỉ là test. 

* Ứng dụng phải được publish qua các phiên bản Alpha, Beta hoặc là Internal test

###### Step 4: Prepare product and subscription

* Tạo các sản phẩm của **managed product**, **subscription**, **reward product** muốn đưa vào trong ứng dụng.

###### Step 5: Test Play Billing

* Thực hiện thêm tài khoản google test và test trên các thiết bị để tránh bị mất tiền khi test.

## Step 1: Implement Google Play Billing Library

### Connect to Google Play Billing

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

### Query SkuDetail product

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

https://developer.android.com/google/play/billing/billing_library_overview#Query-recent

### Enable the purchase

* Một số điện thoại Android có thể không hỗ trợ một sản phẩm nhất định, ví dụ như các sản phẩm subscriptions. Do đó trước mỗi phiên thanh toán, bạn cần gọi phương thức **isFeatureSupported()** để có thể kiểm tra được điều kiện. Để xem được danh sách các loại sản phẩm, tìm hiểu thêm về [BillingClient.FeatureType](https://developer.android.com/reference/com/android/billingclient/api/BillingClient.FeatureType).

```
private fun isSubscriptionSupported(): Boolean {
    val responseCode = mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
    if (responseCode != BillingClient.BillingResponse.OK) {
        Log.d(TAG, "isSubscriptionSupported() got an error response: $responseCode")
    }
    return responseCode == BillingClient.BillingResponse.OK
}
```

* Sau khi bắt sự kiện click vào 1 item nào đó để tiến hành mua sản phẩm, chúng ta sử dụng phương thức **launchBillingFlow()** để hiển thị flow thanh toán với param là **BillingFlowParams**.

```
val billingFlowParam = BillingFlowParams
    .newBuilder()
    .setSkuDetails(item)
    .build()
billingClient.launchBillingFlow(this, billingFlowParam)
```

* Sự kiện khi thanh toán thành công hoặc thất bại sẽ được trả về thông qua **PurchasesUpdatedListener** với phương thức **onPurchasesUpdated()**. Xử lý dữ liệu trả về thông qua responseCode, nếu response là OK thì purchaseList trả về sẽ là thông tin đơn hàng mà bạn vừa thanh toán.

```
override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
    when(responseCode) {
        BillingClient.BillingResponse.OK -> {
            purchases?.let {
                // Handle response success
            }
        }
        BillingClient.BillingResponse.USER_CANCELED -> {
            // Handle response cancel
        }
    }
}
```

* Theo mặc định thì những sản phẩn one-time sẽ không được mua lại, nếu click vào mua thì sẽ trả về responseCode là 7. Chính vì vậy để làm cho những sản phẩm này được đánh dấu là dùng hết rồi và có thể mua lại thì nên sử dụng phương thức **consumeAsync()** với tham số là purchaseToken lấy được từ các đơn hàng thanh toán rồi.

```
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
> Bạn cũng có thể Clear Cache của ứng dụng Google Play Store để xóa đi dữ liệu cache về thanh toán. Nhưng cách này có thể dẫn đến nhiều hậu quả khác nhau.

### Acknowledge a purchase

* Google Play hỗ trợ mua sản phẩm cả bên trong và bên ngoài của ứng dụng. Để đảm bảo trải nghiệm mua hàng nhất quán, bạn phải thừa nhận rằng tất cả các giao dịch đều ở trạng thái SUCCESS nhận được thông qua thư viện Google Play Billing.

* Nếu bạn không xác nhận mua hàng trong vòng 3 ngày, người dùng sẽ tự động nhận được tiền hoàn lại và Google Play sẽ hủy bỏ giao dịch mua. Đối với các sản phẩm đang chờ xử lý, số 3 ngày này không dành cho các purchase ở trạng thái PENDING mà chỉ bắt đầu khi giao dịch đã mua chuyển qua trạng thái SUCCESS.

* Bạn có thể xác nhận mua hàng bằng cách sử dụng một trong các phương pháp sau:

    * Đối với các sản phẩm có thể tiêu thụ được, hãy sử dụng phương thức **consumerAsync()** tìm thấy trong Client API.
    * Đối với các sản phẩm không được tiêu thụ, hãy sử dụng phương thức **acknowledgePurchase()** tìm thấy trong Client API.
    * Phương thức **acknowledge()** cũng có sẵn trong Server API.
    
```
fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

            // Acknowledge the purchase if it hasn't already been acknowledged
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, this)
            }
        }
    }
```

* Đối với các giao dịch thực hiện bởi tester, acknowledge window sẽ ngắn hơn, thay vì 3 ngày thì chỉ còn lại 5 phút. Các giao dịch không được xác nhận sẽ được hoàn lại và thu hồi.

### Support pending transaction

* Trong thực tế khi sử dụng các giải pháp của Google Play Billing, bạn phải hỗ trợ mua hàng khi cần thêm hành động trước khi cấp quyền. Ví dụ người dùng có thể chọn mua sản phẩm trong ứng dụng của bạn tại của hàng thực tế bằng tiền mặt. Điều này nghĩa là giao dịch được hoàn thành bên ngoài ứng dụng. Trong trường hợp này chỉ nên cấp quyền sau khi người dùng đã hoàn thành giao dịch.

* Để bật chức năng pending purchase, gọi đến phương thức **enablePendingPurchases()** khi khởi tạo ứng dụng. Nếu không gọi đến phương thức này, bạn không thể khởi tạo thư viện Google Play Billing.

```
mBillingClient = BillingClient.newBuilder(application)
                .enablePendingPurchases()
                .setListener(this)
                .build()
```

* Sử dụng phương thức **Purchase.getState()** để xác định xem trạng thái mua là **PURCHASED** hay **PENDING**. Lưu ý bạn chỉ nên cấp quyền khi đơn hàng ở trạng thái **PURCHASED**:

    * Khi khởi động ứng dụng, hãy gọi phương thức **BillingClient.queryPurchase()** để truy xuất danh sách các sản phẩm chưa được liên kết với người dùng và sau đó lấy ra trạng thái state trên mỗi đối tượng mua hàng.
    * Implement phương thức **onPurchasesUpdated()** để xử lý các thay đổi đối với đối tượng mua hàng.
    
```
fun handlePurchase(purchase: Purchase) {
    if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
        // Grant the item to the user, and then acknowledge the purchase
        acknowledgePurchase(purchase)
    } else if(purchase.purchaseState == Purchase.PurchaseState.PENDING) {
        // Here you can confirm to the user that they've started the pending
        // purchase, and to complete it, they should follow instructions that
        // are given to them. You can also choose to remind the user in the
        // future to complete the purchase if you detect that it is still
        // pending.
    }
}
```

* Giao dịch đang chờ xử lý có thể được kiểm tra bằng bằng cách sử dụng license tester. Ngoài 2 thẻ tín dụng thử nghiệm, người kiểm tra giấy phép có quyền truy cập vào 2 công cụ kiểm tra cho các hình thức thanh toán bị trì hoãn tự động hoàn thành hoặc hủy sau vài phút.

* Không nên cấp quyền hoặc thừa nhận việc mua ngay sau khi sử dụng 2 công cụ này. Khi mua bằng cách sử dụng công cụ kiểm tra tự động hoàn tất, bạn nên xác minh rằng ứng dụng của bạn cấp quyền và thừa nhận giao dịch sau khi mua hoàn tất.

### Attach developer payload

* Bạn có thể đính kèm một chuỗi string hoặc là develop payload để mua hàng. Tuy nhiên bạn chỉ có thể đính kèm developer payload khi giao dịch mua được thừa nhận hoặc tiêu thụ rồi. Điều này không giống như developer palyoad trong **AIDL**, nơi mà payload có thể được chỉ định khi khởi chạy luồng mua.

* Đối với consumable products, phương thức consumeAsync() yêu cầu ConsumeParams chứa develop payload như sau: 

```
val consumeParams =
    ConsumeParams.newBuilder()
        .setPurchaseToken(/* token */)
        .setDeveloperPayload(/* payload */)
        .build()

client.consumeAsync(consumeParams, listener)
```

* Đối với product aren't consumed, phương thức acknowledgePruchase() nhận params là **AcknowledgePurchaseParams** bao gồm developer payload như sau: 

```
val acknowledgePurchaseParams =
    AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(/* token */)
        .setDeveloperPayload(/* payload */)
        .build()

client.acknowledgePurchase(acknowledgePurchaseParams, listener)
```

* Bạn chỉ có thể consume hoặc acknowledge với purchase ở trạng thái **PURCHASED**.

### Verify a purchase

Việc verify một giao dịch có thể được thực hiển ở cả server và device. 

* [Verify from server](https://developer.android.com/google/play/billing/billing_library_overview#Verify-purchase): Để tránh việc để người khác giả mạo response rồi trả về thanh toán trên device hãy thực hiện như sau.

    * Sử dụng Google Play Developer API và phương thức GET để lấy ra được danh sách các hóa đơn của bạn. 
    * Google Play sẽ trả về thông tin chi tiết của hóa đơn
    * Server backend xác minh xem Order ID là duy nhất và chưa từng xuất hiện trước đó.
    * Server backend dựa vào tài khoản người dùng đã nhận được và tiến hành xác minh lại các thông tin cần thiết.
    * Nếu bạn đang sử dụng subscription để nâng cấp tài khoản hoặc hạ cấp tài khoản, hãy chú ý đến trường **linkedPurchaseToken** vì nó miêu tả các thông tin cần thiết.
    * Sản phẩm trong ứng dụng được cung cấp cho người dùng.
    
* [Verify from device](https://developer.android.com/google/play/billing/billing_library_overview#Verify-purchase-device):

    * Nếu bạn không có server, bạn vẫn có thể xác thực chi tiết mua hàng thông qua ứng dụng Android.
    * Để đảm bảo an toàn, Google Play sẽ ký chuỗi JSON phải hồi cho giao dịch mua. Chuỗi này được mã hóa bí mật với cặp hóa RSA cho mỗi ứng dụng và trả về qua phương thức **getOrigenJson()** trong Purchase.
    * Để lấy được cặp khóa này, truy cập vào Play Console -> Services & API.
    * Khi ứng dụng nhận được phản hồi đã ký này, bạn có thể sử dụng public key của cặp khóa RSA để xác minh chữ ký.
    * Hình thức xác minh này không thực sự an toàn vì logic của ứng dụng có thể bị dịch ngược và thay đổi. 
    * Bạn nên làm xáo trộn khóa công khai Google Play và mã thanh toán Google Play để kẻ tấn công khó có thể thiết kế lại các giao thức bảo mật và các thành phần khác. Tối thiểu bạn nên chạy một công cụ mã hóa như **Proguard**:
    
```
-keep class com.android.vending.billing.**
```


## Step 2: Create file Apk

* Để build signed Apk, trước hết hãy chắc chắn bạn đang ở Build Variant là release với các cài đặt tương ứng cho phiên bản này.

* Chắc rằng ứng dụng của bạn đã có quyền `<uses-permission android:name="com.android.vending.BILLING" />` để yêu cầu quyền thanh toán.

* Các chức năng thanh toán được hoàn thành trước khi đẩy lên Google Play Console để dựa vào đó để test.

* Để thực hiện hãy vào Build -> Generate Signed Bundle/APK.

* Nếu là ứng dụng mới đưa lên chợ, chúng ta sẽ cần phải tạo 1 keystore mới. Lưu ý rằng khi đã tạo rồi mà để mất file này thì sẽ không thể update được ứng dụng cho phiên bản tiếp theo. Nếu đã có rồi thì bỏ qua bước này.

![](https://cdn-images-1.medium.com/max/800/0*uDKK6bSyT9c-v6gh)

*   Hoàn thiện keystore password rồi finish là đợi một lúc để Android Studio build.

## Step 3: Upload app to Google Play Console

* Sau khi đã có file Apk, việc tiếp theo là truy cập vào Google Play Console để setting upload ứng dụng. Để làm được việc này bạn cần phải mua lấy một tài khoản google develop. Có thể tham khảo ở [đây](https://support.magplus.com/hc/en-us/articles/204270878-Android-Setting-up-Your-Google-Play-Developer-Account)

* Truy cập vào [https://play.google.com/apps/publish/](https://play.google.com/apps/publish/) -> **All Application** -> **Create Application** -> **Enter title and create**.

* Hiện tại ứng dụng đang ở trạng thái draf, muốn test được chúng ta phải publish app. Mà không cần đẩy lên hẳn chợ, chỉ cần sử dụng Internal test track là đủ. Để vào đó cấu hình hãy truy cập đường dẫn **App Releases** -> **Internal test track** -> **Create Release** -> **Upload signed apk** -> **Save** - **Review**.  

* Còn bước cuối cùng để publish app ở dạng test, bạn cần phải hoàn thành một số mục như **Store listing**, **Content rating**, **Pricing & distribution** thì mới có thể hiện lên được button **START ROLLOUT TO BETA**. Tiếp đó đợi để có thể được review và publish app.

## Step 4: Prepare product and subscription

### Add one-time product

* Google Play Billing hỗ trợ các loại sản phẩm one-time sau:

    * Các loại sản phẩm không tiêu thụ là các sản phẩm mang lại hiệu quả vĩnh viễn và mua 1 lần. Chẳng hạn như update premium.
    * Các loại sản phẩm có thể tiêu thụ là các sản phẩm cung cấp lợi ích tạm thời và có thể được mua lại nhiều lần. Chẳng hạn như tiền trong trò chơi. Để cung cấp sản phẩm nhiều lần, bạn phải gửi yêu cầu **consume** lên server Google Play.
    
* Để chỉ ra rằng sản phẩm đã được tiêu thụ, hãy gọi phương thức **consumeAsync()** của BillingClient và sau đó nhận lại kết quả thông qua **ConsumeResponseListener**.

* Vì yêu cầu tiêu thụ này đôi khi có thể không thành công, bạn phải kiểm tra lại ở backend server của mình để tránh việc không đồng bộ dữ liệu.

* Tạo các item one-time product trên Google Play Console phải chú ý các Product ID không sửa đổi được và phải trùng với ID mà client dùng để lấy dữ liệu. Hướng dẫn tạo one-time product chi tiết tại [đây](https://support.google.com/googleplay/android-developer/answer/1153481).

* Ngoài ra có thể tạo được mã khuyện mại **Promotion Code** để người dùng nhập và nhận được các phần thưởng, để tạo được làm theo hướng dẫn dưới [đây](https://support.google.com/googleplay/android-developer/answer/6321495?hl=en&ref_topic=7071529). 

    * Bạn có thể phân phối thẻ với promo code tại sự kiện và người dùng nhập nó để mở khóa vật phẩm hoặc gì đó trong trò chơi. 
    * Bạn có thể cung cấp promo code để chia sẻ chúng với bạn bè và người thân. 

* Người dùng có thể đổi các mã khuyến mại theo các cách sau:

    * Nhập thủ công mã trong ứng dụng Google Play Store.
    * Nhấp vào mũi tên bên cạnh hình thức thanh toán trong màn hình mua hàng trên Google Play và đổi quà.

* Khi tạo mã khuyến mại, có 2 sự lựa chọn là sử dụng trong app(sẽ phải có một managed product trước đó để sử dụng mã) hoặc là mã thanh toán bên ngoài của ứng dụng.

### Add rewarded product

* Một phương pháp mở khóa các sản phẩm và đem lại lợi ích cho người dùng là tạo ra các sản phẩm có thương hoặc các mặt hàng mà người sử dụng nhận được sau khi xem quảng cáo video. Thông qua cách này người dùng sẽ nhận được các vật phẩm mà không cần phải thanh toán trực tiếp. 

* Rewarded product có dạng INAPP của SkuType, để đảm bảo người dùng có thể nhận nhiều lần phần thưởng, sản phẩm này phải được tiêu thụ(consume).

* Để giúp tạo điều kiện tuân thủ các nghĩa vụ pháp lý liên quan đến trẻ em và người dùng chưa đủ tuổi, khi bạn tạo ứng dụng của mình hãy xem xét liệu cá yêu cầu quảng cáo có được coi là hướng đến trẻ em hay liệu chúng hướng đến người dùng chưa đủ tuổi chấp nhận. Nếu có hạn chế này thì cần sử dụng các phương thức **setChildDirected()** và **setUnderAgeOfConsent()** để thực hiện.

```
mBillingClient = BillingClient.newBuilder(application)
    .enablePendingPurchases()
    .setListener(this)
    .setChildDirected(BillingClient.ChildDirected.CHILD_DIRECTED)
    .setUnderAgeOfConsent(BillingClient.UnderAgeOfConsent.UNDER_AGE_OF_CONSENT)
    .build()
```

* Để hiển thị các rewarded product, chúng ta cũng sử dụng như lấy các SkuDetails với dạng INAPP.

* Để hiển thị video quảng cáo đối với một SkuDetails được chọn, chúng ta sử dụng phương thức **loadRewardedSku()**. Phương thức này sẽ cần truyền vào **RewardLoadParams** và trả về dữ liệu thông qua **RewardResponseListener**.

* RewardResponseListener được thông báo khi video tải xong. Người dùng cũng được thông báo nếu video không khả dụng hoặc nếu có lỗi khác. Dựa vào responseCode để xử lý các trường hợp cần thiết.

```
if (skuDetails.isRewarded()) {
    val params = RewardLoadParams.Builder()
            .setSkuDetails(skuDetails)
            .build()
    mBillingClient.loadRewardedSku(params.build(),
            object : RewardResponseListener {
        override fun onRewardResponse(@BillingResponse responseCode : Int) {
            if (responseCode == BillingResponse.OK) {
                // Enable the reward product, or make
                // any necessary updates to the UI.
            }
        }
    })
}
```

* Nếu thư viện Google Play Billing tải thành công video được liên kết với 1 sản phẩm có thưởng và RewardResponseListener nhận được mã phản hồi là OK, bạn có thể khởi chạy luồng thanh toán.

* Để sử dụng item test reward nên sử dụng Product ID là **android.test.reward**, nó được tạo ra sẵn và bạn không phải tạo trên Google Play Console.

### Add Subscription

* Subscription được cấu hình trên Google Play Console. 

## Step 5: Test Play Billing

* Khi app được publish ở dạng **Internal test**, chỉ những email nào nằm trong danh sách mới tìm thấy và phải join vào trương trình test bằng cách vào Developer Console App Releases → Alpha → Manage testers → Tìm Opt-in URL và gửi email test để họ chấp nhận.


