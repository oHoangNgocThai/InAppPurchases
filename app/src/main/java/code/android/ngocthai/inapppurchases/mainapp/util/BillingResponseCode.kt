package code.android.ngocthai.inapppurchases.mainapp.util

enum class BillingResponseCode(val code: Int, val message: String) {
    BILLING_UNAVAILABLE(3, "Billing API version is not supported for the type requested"),
    DEVELOPER_ERROR(5, "Invalid arguments provided to the API. This error can also indicate that the application was not correctly signed or properly set up for In-app Billing in Google Play, or does not have the necessary permissions in its manifest"),
    ERROR(6, "Fatal error during the API action"),
    FEATURE_NOT_SUPPORTED(-2, "Requested feature is not supported by Play Store on the current device"),
    ITEM_ALREADY_OWNED(7, "Failure to purchase since item is already owned"),
    ITEM_NOT_OWNED(8, "Failure to consume since item is not owned"),
    ITEM_UNAVAILABLE(4, "Requested product is not available for purchase"),
    OK(0, "Success"),
    SERVICE_DISCONNECTED(-1, "Play Store service is not connected now - potentially transient state"),
    SERVICE_UNAVAILABLE(2, "Network connection is down"),
    USER_CANCELED(1, "User pressed back or canceled a dialog")
}
