package store.service

import camp.nextstep.edu.missionutils.DateTimes
import store.model.Promotion
import store.repository.ProductRepository
import store.repository.PromotionRepository

class StoreService(
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository
) {
    fun orderItem(name: String, quantity: Int) {
        val products = productRepository.findByName(name)
        require(products.isNotEmpty()) { "[ERROR] 존재하지 않는 상품입니다." }

        val totalStock = products.sumOf { it.quantity }
        require(quantity <= totalStock) { "[ERROR] 재고 수량을 초과하여 구매할 수 없습니다." }

        val promotionProduct = products.find { it.promotionName != null }

        if (promotionProduct != null) {
            val promotion = promotionRepository.findByName(promotionProduct.promotionName!!)

            if (promotion != null && isPromotionDate(promotion)) {
                // TODO: calculatePromotion(promotionProduct, quantity, promotion)
                println("프로모션 적용 가능! 계산 로직 수행 필요")
                return
            }
        }

        println("일반 결제 진행")
    }

    private fun isPromotionDate(promotion: Promotion): Boolean {
        val now = DateTimes.now().toLocalDate()
        return !now.isBefore(promotion.startDate) && !now.isAfter(promotion.endDate)
    }
}
