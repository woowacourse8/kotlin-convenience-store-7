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

        outputView.printReceipt(results, isMembership)
    }

    private fun getValidOrder(): List<PurchaseResult> {
        while (true) {
            try {
                val inputOrders = inputView.readItem()
                val results = mutableListOf<PurchaseResult>()

                for ((name, quantity) in inputOrders) {
                    storeService.checkStockAvailability(name, quantity)

                    val finalQuantity = storeService.processPromotionInteraction(name, quantity) { message ->
                        inputView.readYesNo(message)}

                    if (finalQuantity > 0) {
                        val result = storeService.orderItem(name, finalQuantity)
                        results.add(result)
                    }
                }
                return results

            } catch (e: IllegalArgumentException) {
                outputView.printError(e.message ?: "[Error] 알 수 없는 에러")
            }
        }
    }
}