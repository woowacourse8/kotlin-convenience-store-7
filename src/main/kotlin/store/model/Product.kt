package store.model

data class Product(
    val name: String,
    val price: Int,
    var quantity: Int,
    var promotionQuantity: Int,
    val promotionName: String?
) {
    fun decreasePromotionStock(amount: Int) {
        require(promotionQuantity >= amount) { "[ERROR] 프로모션 재고 부족" }
        promotionQuantity -= amount
    }

    fun decreaseGeneralStock(amount: Int) {
        require(quantity >= amount) { "[ERROR] 일반 재고 부족" }
        quantity -= amount
    }

    fun hasPromotionStock(): Boolean {
        return promotionQuantity > 0
    }
}
