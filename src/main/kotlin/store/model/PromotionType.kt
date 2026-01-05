package store.model

enum class PromotionType(val buy: Int, val get: Int) {
    TWO_PLUS_ONE(2, 1),
    ONE_PLUS_ONE(1, 1),
    NONE(0, 0);

    companion object {
        fun checkType(buy: Int, get: Int): PromotionType {
            return entries.find { it.buy == buy && it.get == get } ?: NONE
        }
    }
}
