package store.service

import camp.nextstep.edu.missionutils.DateTimes
import store.model.Product
import store.model.Promotion
import store.model.PurchaseResult
import store.repository.ProductRepository
import store.repository.PromotionRepository
import kotlin.math.min

class StoreService(
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository
) {
    fun orderItem(name: String, quantity: Int): PurchaseResult {
        val product = validateAndLoadProduct(name, quantity)

        val (remainQuantity, giftCount) = processPromotionIfApplicable(product, quantity)

        processGeneralStock(product, remainQuantity)

        return PurchaseResult(product, quantity, giftCount)
    }

    private fun validateAndLoadProduct(name: String, quantity: Int): Product {
        val product = productRepository.findByName(name)
        requireNotNull(product) { "[ERROR] 존재하지 않는 상품입니다." }

        val totalStock = product.quantity + product.promotionQuantity
        require(totalStock >= quantity) { "[ERROR] 재고 수량을 초과하여 구매할 수 없습니다." }

        return product
    }

    private fun processPromotionIfApplicable(product: Product, quantity: Int): Pair<Int, Int> {
        if (product.promotionName != null && isPromotionActive(product.promotionName)) {
            val promotion = promotionRepository.findByName(product.promotionName)!!
            return applyPromotionPolicy(product, promotion, quantity)
        }
        return Pair(quantity, 0)
    }

    private fun applyPromotionPolicy(product: Product, promotion: Promotion, count: Int): Pair<Int, Int> {
        val setSize = promotion.buy + promotion.get
        val applicableSets = calculateApplicableSets(product, setSize, count)

        val giftCount = deductSetStock(product, applicableSets, setSize, promotion.get)

        val remain = count - (applicableSets * setSize)
        val finalRemain = deductRemainderFromPromotion(product, remain)

        return Pair(finalRemain, giftCount)
    }

    private fun calculateApplicableSets(product: Product, setSize: Int, count: Int): Int {
        val maxSets = product.promotionQuantity / setSize
        val reqSets = count / setSize
        return min(maxSets, reqSets)
    }

    private fun deductSetStock(product: Product, sets: Int, setSize: Int, getCount: Int): Int {
        val deductQuantity = sets * setSize
        product.decreasePromotionStock(deductQuantity)

        return sets * getCount
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
