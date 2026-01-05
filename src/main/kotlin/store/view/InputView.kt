package store.view

import camp.nextstep.edu.missionutils.Console
import net.bytebuddy.pool.TypePool

class InputView {
    fun readItem(): Map<String,Int> {
        println("구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])")
        val input = Console.readLine()

        if (input.isBlank()) throw IllegalArgumentException("[ERROR] 입력값이 없습니다.")

        try {
            return parseInput(input)
        } catch (e: Exception) {
            throw IllegalArgumentException("[ERROR] 올바르지 않은 형식입니다. 다시 입력해주세요.")
        }
    }

    private fun parseInput(input: String): Map<String, Int> {
        val items = input.split(",")
        val result = mutableMapOf<String, Int>()

        items.forEach { item ->
            val cleaned = item.replace("[", "").replace("]", "")

            val parts = cleaned.split("-")

            require(parts.size == 2) { "[ERROR] 형식이 맞지 않습니다."}

            val name = parts[0].trim()
            val quantity = parts[1].trim().toIntOrNull() ?: throw IllegalArgumentException("[ERROR] 형식이 맞지 않습니다.")

            result[name] = quantity
        }
        return result
    }

    fun readYesNo(message: String): Boolean {
        println(message)
        val input = Console.readLine().trim().uppercase()

        if (input == "Y") return true
        if (input == "N") return false

        throw IllegalArgumentException("[ERROR] Y 또는 N만 입력 가능합니다.")
    }
}
