package store.repository

import store.model.Product
import store.util.FileReader

class ProductRepository {
    private val products: List<Product> = loadProducts()

    private fun loadProducts(): List<Product> {
        val lines = FileReader.loadFile("products.md")
        val rawProducts = lines.map { parseToRaw(it) }
        val grouped = rawProducts.groupBy { it.name }

        return grouped.map { (name, rawList) ->
            mergeToProduct(name, rawList)
        }
    }

    private fun parseToRaw(line:String): RawProduct {
        val values = line.split(",")
        val promo = parsePromotionName(values[3])
        return RawProduct(values[0], values[1].toInt(), values[2].toInt(), promo)
    }

    private fun mergeToProduct(name: String, raws: List<RawProduct>): Product {
        val price = raws[0].price
        val promoStock = raws.find { it.promoName != null }?.quantity ?: 0
        val generalStock = raws.find { it.promoName == null }?.quantity ?: 0
        val promoName = raws.find { it.promoName != null }?.promoName

        return Product(name, price, generalStock, promoStock, promoName)
    }

    private fun parsePromotionName(value: String): String? {
        if (value == "null") return null
        return value
    }

    fun findByName(name: String): Product? {
        return products.find { it.name == name }
    }

    private data class RawProduct(
        val name: String,
        val price: Int,
        val quantity: Int,
        val promoName: String?
    )
}
