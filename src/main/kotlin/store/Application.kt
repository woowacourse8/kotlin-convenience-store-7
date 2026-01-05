package store

import store.repository.ProductRepository
import store.repository.PromotionRepository
import store.service.StoreService

fun main() {
    // TODO: 프로그램 구현
    val productRepo = ProductRepository()
    val promotionRepo = PromotionRepository()
    val service = StoreService(productRepo, promotionRepo)

    val result = service.orderItem("콜라", 3)
    println(result.product.name)
    println(result.purchaseCount)
    println(result.giftCount)
}
