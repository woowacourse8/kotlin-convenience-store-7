package store.controller

import store.model.PurchaseResult
import store.service.StoreService
import store.view.InputView
import store.view.OutputView

class StoreController(
    private val inputView: InputView,
    private val outputView: OutputView,
    private val storeService: StoreService
) {
    fun run() {
        // 1. 환영 인사 및 상품 목록 출력
        outputView.printWelcome()
        outputView.printProducts(storeService.getAllProducts())

        // 2. 구매 입력 받기 (예외 발생 시 재시도)
        val results = getValidOrder()

        // 3. 멤버십 여부 묻기
        // 구매 내역이 있을 때만 멤버십 질문
        val isMembership = if (results.isNotEmpty()) {
            try {
                inputView.readYesNo("\n멤버십 할인을 받으시겠습니까? (Y/N)")
            } catch (e: IllegalArgumentException) {
                outputView.printError(e.message!!)
                false
            }
        } else {
            false
        }

        // 4. 영수증 출력
        outputView.printReceipt(results, isMembership)
    }

    private fun getValidOrder(): List<PurchaseResult> {
        while (true) {
            try {
                val inputOrders = inputView.readItem()
                val results = mutableListOf<PurchaseResult>()

                // 주문 처리 반복문
                for ((name, quantity) in inputOrders) {

                    // [🚀 긴급 수정] 여기서 먼저 재고 체크를 수행합니다!
                    // 컵라면 12개 입력 시, 여기서 바로 에러 터지고 catch 블록으로 날아감
                    storeService.checkStockAvailability(name, quantity)

                    // ------------------------------------------------

                    var finalQuantity = quantity

                    // 1. 프로모션 혜택 (1개 더?)
                    if (storeService.checkBonusStatus(name, finalQuantity)) {
                        // ... (생략)
                    }

                    // 2. 프로모션 재고 부족 (정가 결제?)
                    val shortage = storeService.checkStockShortage(name, finalQuantity)
                    if (shortage > 0) {
                        // ... (생략)
                    }

                    if (finalQuantity > 0) {
                        val result = storeService.orderItem(name, finalQuantity)
                        results.add(result)
                    }
                }
                return results

            } catch (e: IllegalArgumentException) {
                outputView.printError(e.message!!)
            }
        }
    }
}