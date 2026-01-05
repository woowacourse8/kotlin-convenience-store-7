package store

import store.controller.StoreController
import store.repository.ProductRepository
import store.repository.PromotionRepository
import store.service.StoreService
import store.view.InputView
import store.view.OutputView

fun main() {
    // TODO: 프로그램 구현
    val productRepo = ProductRepository()
    val promotionRepo = PromotionRepository()

    val service = StoreService(productRepo, promotionRepo)
    val inputView = InputView()
    val outputView = OutputView()


    val controller = StoreController(inputView, outputView, service)

    controller.run()
}
