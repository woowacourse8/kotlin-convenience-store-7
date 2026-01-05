package store.view

import store.model.Product
import store.model.PurchaseResult
import java.text.DecimalFormat
import kotlin.math.min

class OutputView {
    private val formatter = DecimalFormat("#,###")

    fun printWelcome() {
        println("\n안녕하세요. W편의점입니다.")
        println("현재 보유하고 있는 상품입니다.\n")
    }

    fun printProducts(products: List<Product>) {
        products.forEach { product ->
            if (product.promotionName != null) {
                // 프로모션 상품인 경우: 프로모션 재고 줄 + 일반 재고 줄(재고 없음이라도) 출력
                printProductLine(product.name, product.price, product.promotionQuantity, product.promotionName)
                printProductLine(product.name, product.price, product.quantity, null)
            } else {
                // 일반 상품인 경우: 한 줄만 출력
                printProductLine(product.name, product.price, product.quantity, null)
            }
        }
    }

    private fun printProductLine(name: String, price: Int, quantity: Int, promotion: String?) {
        var quantityStr = "${quantity}개"
        if (quantity == 0) quantityStr = "재고 없음"
        val promoStr = promotion ?: ""

        println("- $name ${formatter.format(price)}원 $quantityStr $promoStr")
    }

    // 3. 영수증 출력 (핵심!)
    fun printReceipt(results: List<PurchaseResult>, isMembership: Boolean) {
        println("\n==============W 편의점================")
        println(String.format("%-10s\t%-8s\t%-6s", "상품명", "수량", "금액"))

        var totalCount = 0
        var totalAmount = 0
        var giftDiscount = 0
        var membershipTargetAmount = 0

        results.forEach { result ->
            val amount = result.purchaseCount * result.product.price
            println(String.format("%-10s\t%-8d\t%-6s",
                result.product.name,
                result.purchaseCount,
                formatter.format(amount)))

            totalCount += result.purchaseCount
            totalAmount += amount
            giftDiscount += result.giftCount * result.product.price
            membershipTargetAmount += result.nonPromotionAmount
        }

        val hasGift = results.any { it.giftCount > 0 }
        if (hasGift) {
            println("=============증\t정===============")
            for (result in results) {
                if (result.giftCount > 0) {
                    println(String.format("%-10s\t%-8d", result.product.name, result.giftCount))
                }
            }
        }

        var membershipDiscount = 0
        if (isMembership) {
            membershipDiscount = (membershipTargetAmount * 0.3).toInt().coerceAtMost(8000)
        }

        val finalAmount = totalAmount - giftDiscount - membershipDiscount

        println("====================================")
        printSummary("총구매액", totalCount, totalAmount)
        printSummary("행사할인", null, -giftDiscount)
        printSummary("멤버십할인", null, -membershipDiscount)
        printSummary("내실돈", null, finalAmount)
    }

    private fun printSummary(label: String, count: Int?, amount: Int) {
        val countStr = count?.toString() ?: ""
        println(String.format("%-10s\t%-8s\t%-6s", label, countStr, formatter.format(amount)))
    }

    // 4. 에러 메시지 출력
    fun printError(message: String) {
        println(message)
    }
}