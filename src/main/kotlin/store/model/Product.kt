package store.model

data class Product(
    val name: String,
    val price: Int,
    var quantity: Int,
    var promotionQuantity: Int,
    val promotionName: String?
) {
    fun decreaseQuantity(amount: Int) {
        require(quantity >= amount) { "[ERROR] 재고가 부족합니다." }
        quantity -= amount
    }

    fun hasPromotionStock(): Boolean {
        return promotionQuantity > 0
    }
}
