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
    private data class PromotionProcessResult(
        val remainQuantity: Int, // 일반 재고로 넘길 수량
        val giftCount: Int,      // 증정품 개수
        val nonPromoAmount: Int  // 멤버십 할인이 적용될 금액 (정가 결제분)
    )

    fun orderItem(name: String, quantity: Int): PurchaseResult {
        val product = validateAndLoadProduct(name, quantity)

        val result = processPromotionIfApplicable(product, quantity)

        processGeneralStock(product, result.remainQuantity)

        return PurchaseResult(product, quantity, result.giftCount, result.nonPromoAmount)
    }

    fun processPromotionInteraction(
        name: String,
        quantity: Int,
        askUser: (String) -> Boolean
    ): Int {
        val product = productRepository.findByName(name) ?: return quantity
        var finalQuantity = quantity

        if (checkBonusStatus(name, quantity)) {
            if (askUser("${name}을(를) 1개 더 가져오시겠습니까?"))
                finalQuantity += 1
        }

        val shortage = checkStockShortage(name, finalQuantity)
        if (shortage > 0) {
            if (!askUser("현재 ${name} ${shortage}개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까?"))
                finalQuantity -= shortage
        }

        return finalQuantity
    }

    fun checkStockAvailability(name: String, quantity: Int) {
        val product = productRepository.findByName(name)
        requireNotNull(product) { "[ERROR] 존재하지 않는 상품입니다." }

        val totalStock = product.quantity + product.promotionQuantity
        require(totalStock >= quantity) { "[ERROR] 재고 수량을 초과하여 구매할 수 없습니다." }
    }

    private fun validateAndLoadProduct(name: String, quantity: Int): Product {
        val product = productRepository.findByName(name)
        requireNotNull(product) { "[ERROR] 존재하지 않는 상품입니다." }

        val totalStock = product.quantity + product.promotionQuantity
        require(totalStock >= quantity) { "[ERROR] 재고 수량을 초과하여 구매할 수 없습니다." }

        return product
    }

    private fun processPromotionIfApplicable(product: Product, quantity: Int): PromotionProcessResult {
        if (product.promotionName != null && isPromotionActive(product.promotionName)) {
            val promotion = promotionRepository.findByName(product.promotionName!!)!!
            return applyPromotionPolicy(product, promotion, quantity)
        }
        return PromotionProcessResult(quantity, 0, quantity * product.price)
    }

    private fun applyPromotionPolicy(product: Product, promotion: Promotion, count: Int): PromotionProcessResult {
        val setSize = promotion.buy + promotion.get
        val applicableSets = calculateApplicableSets(product, setSize, count)
        val giftCount = deductSetStock(product, applicableSets, setSize, promotion.get)
        val remain = count - (applicableSets * setSize)
        val finalRemain = deductRemainderFromPromotion(product, remain)
        val nonPromoCount = count - (applicableSets * setSize)
        val nonPromoAmount = nonPromoCount * product.price

        return PromotionProcessResult(finalRemain, giftCount, nonPromoAmount)
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
        if (product.promotionQuantity > 0) {
            val deduct = min(remain, product.promotionQuantity)
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

    fun getAllProducts(): List<Product> {
        return productRepository.findAll()
    }

    fun checkBonusStatus(name: String, quantity: Int): Boolean {
        val product = productRepository.findByName(name) ?: return false
        val promotionName = product.promotionName ?: return false

        if (!isPromotionActive(promotionName)) return false

        val promotion = promotionRepository.findByName(promotionName)!!
        val setSize = promotion.buy + promotion.get

        val isBonusTarget = quantity % setSize == promotion.buy
        val hasStock = product.promotionQuantity >= (quantity + 1)

        return isBonusTarget && hasStock
    }

    fun checkStockShortage(name: String, quantity: Int): Int {
        val product = productRepository.findByName(name) ?: return 0

        if (product.promotionName == null || !isPromotionActive(product.promotionName)) return 0

        if (quantity > product.promotionQuantity) {
            return quantity - product.promotionQuantity
        }
        return 0
    }
}