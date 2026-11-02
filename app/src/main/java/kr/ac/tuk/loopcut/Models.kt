package kr.ac.tuk.loopcut

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

enum class SubscriptionStatus { ACTIVE, CANCELLED, EXPIRED }
enum class BillingCycle { MONTHLY, YEARLY }
data class Subscription(
    val id: String = UUID.randomUUID().toString(), val name: String = "", val mark: String = "+",
    val category: String = "", val amount: Long = 0, val cycle: BillingCycle = BillingCycle.MONTHLY,
    val firstDate: LocalDate? = null, val paymentId: String = "", val reminder: Boolean = true,
    val review: Boolean = false, val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val endedAt: LocalDate? = null
) {
    val monthlyAmount: Long get() = if (cycle == BillingCycle.YEARLY) amount / 12 else amount
    fun nextPayment(today: LocalDate): LocalDate {
        val first = firstDate ?: today
        if (first >= today) return first
        fun clamped(year: Int, month: Int): LocalDate {
            val base = LocalDate.of(year, month, 1)
            return base.withDayOfMonth(minOf(first.dayOfMonth, base.lengthOfMonth()))
        }
        var next = clamped(today.year, if (cycle == BillingCycle.YEARLY) first.monthValue else today.monthValue)
        if (next < today) {
            val target = if (cycle == BillingCycle.YEARLY) next.plusYears(1) else next.plusMonths(1)
            next = clamped(target.year, target.monthValue)
        }
        return next
    }
    fun daysUntil(today: LocalDate) = ChronoUnit.DAYS.between(today, nextPayment(today))
}
data class PaymentMethod(val id: String = UUID.randomUUID().toString(), val name: String = "", val prefix: String = "", val suffix: String = "", val bank: Boolean = false) {
    val masked: String get() = if (bank) "${prefix.take(4)}-••-•••• $suffix" else "${prefix.take(4)} ${prefix.drop(4)}•• •••• $suffix"
}
data class AppSettings(val name: String = "길동", val budget: Long = 80000, val notifications: Boolean = true, val days: Set<Int> = setOf(3, 1, 0), val hour: Int = 9, val minute: Int = 0, val push: Boolean = true, val email: Boolean = false)
data class AppState(val subscriptions: List<Subscription> = DemoData.subscriptions, val payments: List<PaymentMethod> = DemoData.payments, val settings: AppSettings = AppSettings(), val recent: List<String> = listOf("Canva", "Notion", "쿠팡 와우")) {
    val active get() = subscriptions.filter { it.status == SubscriptionStatus.ACTIVE }
    val history get() = subscriptions.filter { it.status != SubscriptionStatus.ACTIVE }
    val total get() = active.sumOf { it.monthlyAmount }
}
data class Service(val name: String, val mark: String, val category: String, val amount: Long, val aliases: String = "")
object DemoData {
    // Fixed only for reproducible Figma demo. Replace with the server/user clock in production.
    val today: LocalDate = LocalDate.of(2026, 10, 3)
    val categories = listOf("영상", "음악", "생산성", "클라우드", "쇼핑 · 멤버십", "게임", "교육", "도서 · 뉴스", "건강 · 운동", "기타")
    val categoryHints = listOf("넷플릭스 · 티빙 · 유튜브", "멜론 · 스포티파이 · 지니", "Notion · Canva · MS 365", "iCloud+ · Google One", "쿠팡 와우 · 네이버플러스", "Game Pass · 닌텐도 온라인", "클래스101 · 듀오링고", "밀리의 서재 · 리디", "홈트 · 식단 관리 앱", "목록에 없으면 직접 입력")
    val services = listOf(Service("Netflix", "N", "영상", 17000, "넷플릭스 넷플"), Service("YouTube Premium", "Y", "영상", 14900, "유튜브"), Service("Spotify", "S", "음악", 10900, "스포티파이"), Service("Notion", "n", "생산성", 7900, "노션"), Service("쿠팡 와우", "쿠", "쇼핑 · 멤버십", 7890), Service("Canva", "C", "생산성", 13800, "캔바"), Service("Google One", "G", "클라우드", 2400, "구글"), Service("티빙", "T", "영상", 9500, "tving"), Service("웨이브", "W", "영상", 13900, "wavve"), Service("왓챠", "W", "영상", 7900, "watcha"))
    val payments = listOf(PaymentMethod("shinhan", "신한카드 Deep Dream 체크카드", "531079", "1234"), PaymentMethod("toss", "토스뱅크 입출금통장", "100200", "5678", true))
    val subscriptions: List<Subscription> = listOf("Canva" to 4, "Google One" to 5, "Spotify" to 8, "YouTube Premium" to 12, "Netflix" to 15, "Notion" to 20).map { (name, day) ->
        val service = services.first { it.name == name }
        Subscription(id = name, name = name, mark = service.mark, category = service.category, amount = service.amount, firstDate = LocalDate.of(2026, 8, day), paymentId = if (name == "Spotify" || name == "YouTube Premium") "toss" else "shinhan", review = name == "Notion")
    } + listOf(
        Subscription("past-wave", "웨이브", "W", "영상", 13900, firstDate = LocalDate.of(2025,6,1), status = SubscriptionStatus.CANCELLED, endedAt = LocalDate.of(2026,3,31)),
        Subscription("past-watcha", "왓챠", "W", "영상", 7900, firstDate = LocalDate.of(2025,11,1), status = SubscriptionStatus.CANCELLED, endedAt = LocalDate.of(2026,3,31)),
        Subscription("past-millie", "밀리의 서재", "밀", "도서 · 뉴스", 9900, firstDate = LocalDate.of(2025,7,1), status = SubscriptionStatus.CANCELLED, endedAt = LocalDate.of(2026,2,28)),
        Subscription("past-ridi", "리디셀렉트", "R", "도서 · 뉴스", 6900, firstDate = LocalDate.of(2025,9,1), status = SubscriptionStatus.EXPIRED, endedAt = LocalDate.of(2025,12,31))
    )
}
fun Long.won() = "%,d원".format(java.util.Locale.KOREA, this)
