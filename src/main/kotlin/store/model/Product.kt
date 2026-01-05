package store.model

data class Product(
    val name: String,
    val price: Int,
    var quantity: Int,
    val promotionName: String?
) {
    fun hasPromotion(): Boolean {
        return promotionName != null
    }
}
