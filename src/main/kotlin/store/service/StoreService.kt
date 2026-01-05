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
    // 내부적으로 계산 결과를 주고받기 위한 데이터 클래스
    private data class PromotionProcessResult(
        val remainQuantity: Int, // 일반 재고로 넘길 수량
        val giftCount: Int,      // 증정품 개수
        val nonPromoAmount: Int  // 멤버십 할인이 적용될 금액 (정가 결제분)
    )

    fun orderItem(name: String, quantity: Int): PurchaseResult {
        val product = validateAndLoadProduct(name, quantity)

        // [수정] 결과로 remain, gift, nonPromoAmount 3개를 다 받아옵니다.
        val result = processPromotionIfApplicable(product, quantity)

        // 남은 수량(remain)은 일반 재고에서 처리
        processGeneralStock(product, result.remainQuantity)

        return PurchaseResult(product, quantity, result.giftCount, result.nonPromoAmount)
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
        // 프로모션이 없으면: 남은수량=전체, 증정=0, 비프로모션금액=전체가격
        return PromotionProcessResult(quantity, 0, quantity * product.price)
    }

    private fun applyPromotionPolicy(product: Product, promotion: Promotion, count: Int): PromotionProcessResult {
        val setSize = promotion.buy + promotion.get
        val applicableSets = calculateApplicableSets(product, setSize, count)

        // 1. 세트만큼 프로모션 재고 차감 & 증정 개수 계산
        val giftCount = deductSetStock(product, applicableSets, setSize, promotion.get)

        // 2. 프로모션 적용 안 된 나머지 수량
        val remain = count - (applicableSets * setSize)

        // 3. 나머지를 프로모션 자투리 재고에서 차감
        val finalRemain = deductRemainderFromPromotion(product, remain)

        // [핵심 수정] 멤버십 할인 대상 금액 계산
        // 멤버십 대상 = (총 구매 수량 - (적용된 세트 수 * 세트 크기)) * 가격
        // 즉, 2+1 적용된 3개는 멤버십 할인 제외, 나머지 낱개만 멤버십 할인
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
        // [수정] 오타 수정: product.quantity(일반) -> product.promotionQuantity(프로모션)
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

    // --- 인터랙션용 검사 로직 ---

    fun checkBonusStatus(name: String, quantity: Int): Boolean {
        val product = productRepository.findByName(name) ?: return false
        val promotionName = product.promotionName ?: return false

        // 날짜 확인 추가 (날짜가 안 맞으면 보너스도 없음)
        if (!isPromotionActive(promotionName)) return false

        val promotion = promotionRepository.findByName(promotionName)!!
        val setSize = promotion.buy + promotion.get

        // 2+1인데 2개만 가져왔고, 재고가 1개 더 남아있는가?
        val isBonusTarget = quantity % setSize == promotion.buy
        // 재고 체크: (현재 주문량 + 1)을 감당할 수 있는가?
        // 엄밀히는 프로모션 재고가 남아있어야 함
        val hasStock = product.promotionQuantity >= (quantity + 1)

        return isBonusTarget && hasStock
    }

    fun checkStockShortage(name: String, quantity: Int): Int {
        val product = productRepository.findByName(name) ?: return 0

        // 프로모션 상품이 아니거나 기간이 아니면 체크할 필요 없음 (항상 0)
        if (product.promotionName == null || !isPromotionActive(product.promotionName)) return 0

        // 내가 사려는 개수가 프로모션 재고보다 많은가?
        // 예: 프로모션재고 7개, 주문 10개 -> 3개는 정가로 사야 함
        // 문제 의도: "프로모션 혜택을 못 받는 수량" (일반재고로 넘어가는 수량 + 프로모션 자투리)
        // 일단 간단하게 [주문량 - 프로모션재고]가 양수면 그만큼 부족하다고 알림
        if (quantity > product.promotionQuantity) {
            return quantity - product.promotionQuantity
        }
        return 0
    }
}