package store.service

import camp.nextstep.edu.missionutils.DateTimes
import store.model.Product
import store.model.Promotion
import store.repository.ProductRepository
import store.repository.PromotionRepository
import kotlin.math.min

class StoreService(
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository
) {
    fun orderItem(name: String, quantity: Int) {
        val product = validateAndLoadProduct(name, quantity)

        val remainQuantity = processPromotionIfApplicable(product, quantity)

        processGeneralStock(product, remainQuantity)
    }

    private fun validateAndLoadProduct(name: String, quantity: Int): Product {
        val product = productRepository.findByName(name)
        requireNotNull(product) { "[ERROR] 존재하지 않는 상품입니다." }

        val totalStock = product.quantity + product.promotionQuantity
        require(totalStock >= quantity) { "[ERROR] 재고 수량을 초과하여 구매할 수 없습니다." }

        return product
    }

    private fun processPromotionIfApplicable(product: Product, quantity: Int): Int {
        if (product.promotionName != null && isPromotionActive(product.promotionName)) {
            val promotion = promotionRepository.findByName(product.promotionName)!!
            return applyPromotionPolicy(product, promotion, quantity)
        }
        return quantity
    }

    private fun applyPromotionPolicy(product: Product, promotion: Promotion, count: Int): Int {
        val setSize = promotion.buy + promotion.get
        val applicableSets = calculateApplicableSets(product, setSize, count)

        deductSetStock(product, applicableSets, setSize)

        val remain = count - (applicableSets * setSize)

        return deductRemainderFromPromotion(product, remain)
    }

    private fun calculateApplicableSets(product: Product, setSize: Int, count: Int): Int {
        val maxSets = product.promotionQuantity / setSize
        val reqSets = count / setSize
        return min(maxSets, reqSets)
    }

    private fun deductSetStock(product: Product, sets: Int, setSize: Int) {
        val deductQuantity = sets * setSize
        product.decreasePromotionStock(deductQuantity)
        // TODO: 여기서 증정품(sets * get) 정보를 기록.
    }

    private fun deductRemainderFromPromotion(product: Product, count: Int): Int {
        var remain = count
        if (product.quantity > 0) {
            val deduct = min(remain, product.quantity)
            product.decreasePromotionStock(deduct)
            remain -= deduct
        }
        return remain
    }

    private fun processGeneralStock(product: Product, quantity: Int) {
        if (quantity > 0) {
            product.decreaseGeneralStock(quantity)
        }
    }

    private fun isPromotionActive(promoName: String?): Boolean {
        val promotion = promotionRepository.findByName(promoName ?: return false) ?: return false
        val now = DateTimes.now().toLocalDate()
        return !now.isBefore(promotion.startDate) && !now.isAfter(promotion.endDate)
    }
}
