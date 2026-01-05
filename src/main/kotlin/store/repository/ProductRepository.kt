package store.repository

import store.model.Product
import store.util.FileReader

class ProductRepository {
    private val products: List<Product> = loadProducts()

    private fun loadProducts(): List<Product> {
        val fileName = "products.md"
        val lines = FileReader.loadFile(fileName)

        return lines.map { line ->
            val data = line.split(",")
            val name = data[0]
            val price = data[1].toInt()
            val quantity = data[2].toInt()
            val promotionName = parsePromotion(data[3])

            Product(name, price, quantity, promotionName)
        }
    }

    private fun parsePromotion(promo: String): String? {
        if (promo == "null") return null
        return promo
    }

    fun findByName(name: String): List<Product> {
        return products.filter { it.name == name }
    }
}
