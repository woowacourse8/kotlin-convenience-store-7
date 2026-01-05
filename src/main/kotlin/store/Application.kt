package store

import store.repository.ProductRepository
import store.repository.PromotionRepository

fun main() {
    // TODO: 프로그램 구현
    val repo = PromotionRepository()
    val twoPlusOne = "탄산2+1"

    println(repo.findByName(twoPlusOne))
}
