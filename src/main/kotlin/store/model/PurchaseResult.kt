package store.model

data class PurchaseResult(
    val product: Product,
    val purchaseCount: Int,
    val giftCount: Int
)
