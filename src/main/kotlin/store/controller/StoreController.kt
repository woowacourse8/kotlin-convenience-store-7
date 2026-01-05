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

        // 2. 구매할 상품 입력받기
        val orders = inputView.readItem()

        // 3. 서비스 호출 및 결과 수집
        val results = mutableListOf<PurchaseResult>()

        orders.forEach { (name, quantity) ->
            val result = storeService.orderItem(name, quantity)
            results.add(result)
        }

        // 4. 결과 확인 (임시 로그)
        println("=== 결제 결과 확인 (임시) ===")
        results.forEach {
            println("상품: ${it.product.name}, 구매: ${it.purchaseCount}, 증정: ${it.giftCount}")
        }

        // 5. 멤버십 할인 여부 묻기

        // 6. 영수증 출력
    }
}
