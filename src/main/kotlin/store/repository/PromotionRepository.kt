package store.repository

import store.model.Promotion
import store.util.FileReader
import java.time.LocalDate

class PromotionRepository {
    private val promotions: List<Promotion> = loadPromotions()

    private fun loadPromotions(): List<Promotion> {
        val fileName = "promotions.md"
        val lines = FileReader.loadFile(fileName)

        return lines.map { line ->
            val data = line.split(",")
            val name = data[0]
            val buy = data[1].toInt()
            val get = data[2].toInt()
            val startDate = LocalDate.parse(data[3])
            val endDate = LocalDate.parse(data[4])
            Promotion(name, buy, get, startDate, endDate)
        }
    }

    fun findByName(name: String): List<Promotion> {
        return promotions.filter { it.name == name}
    }
}
