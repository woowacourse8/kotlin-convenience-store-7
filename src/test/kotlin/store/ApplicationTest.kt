package store

import camp.nextstep.edu.missionutils.test.Assertions.assertNowTest
import camp.nextstep.edu.missionutils.test.Assertions.assertSimpleTest
import camp.nextstep.edu.missionutils.test.NsTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ApplicationTest : NsTest() {
    @Test
    fun `파일에 있는 상품 목록 출력`() {
        assertSimpleTest {
            run("[물-1]", "N", "N")
            assertThat(output()).contains(
                "- 콜라 1,000원 10개 탄산2+1",
                "- 콜라 1,000원 10개",
                "- 사이다 1,000원 8개 탄산2+1",
                "- 사이다 1,000원 7개",
                "- 오렌지주스 1,800원 9개 MD추천상품",
                "- 오렌지주스 1,800원 재고 없음",
                "- 탄산수 1,200원 5개 탄산2+1",
                "- 탄산수 1,200원 재고 없음",
                "- 물 500원 10개",
                "- 비타민워터 1,500원 6개",
                "- 감자칩 1,500원 5개 반짝할인",
                "- 감자칩 1,500원 5개",
                "- 초코바 1,200원 5개 MD추천상품",
                "- 초코바 1,200원 5개",
                "- 에너지바 2,000원 5개",
                "- 정식도시락 6,400원 8개",
                "- 컵라면 1,700원 1개 MD추천상품",
                "- 컵라면 1,700원 10개"
            )
        }
    }

    @Test
    fun `여러 개의 일반 상품 구매`() {
        assertSimpleTest {
            run("[비타민워터-3],[물-2],[정식도시락-2]", "N", "N")
            assertThat(output().replace("\\s".toRegex(), "")).contains("내실돈18,300")
        }
    }

    @Test
    fun `기간에 해당하지 않는 프로모션 적용`() {
        assertNowTest({
            run("[감자칩-2]", "N", "N")
            assertThat(output().replace("\\s".toRegex(), "")).contains("내실돈3,000")
        }, LocalDate.of(2026, 2, 1).atStartOfDay())
    }

    @Test
    fun `예외 테스트`() {
        assertSimpleTest {
            runException("[컵라면-12]", "N", "N")
            assertThat(output()).contains("[ERROR]")
        }
    }

    override fun runMain() {
        main()
    }
}
