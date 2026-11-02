package kr.ac.tuk.loopcut

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

interface SubscriptionRepository {
    suspend fun load(): AppState
    suspend fun save(state: AppState)
}
/** Local demo adapter. No credentials, card middles, or network requests are stored or sent. */
class LocalRepository(context: Context): SubscriptionRepository {
    private val preferences = context.applicationContext.getSharedPreferences("loopcut_v1", Context.MODE_PRIVATE)
    override suspend fun load(): AppState = withContext(Dispatchers.IO) {
        val saved = preferences.getString("state", null) ?: return@withContext AppState()
        runCatching { decode(JSONObject(saved)) }.getOrElse { AppState() }
    }
    override suspend fun save(state: AppState) = withContext(Dispatchers.IO) {
        check(preferences.edit().putString("state", encode(state).toString()).commit()) { "기기에 저장할 수 없어요." }
    }
    private fun encode(state: AppState): JSONObject = JSONObject().apply {
        put("version", 1)
        put("subscriptions", JSONArray().apply { state.subscriptions.forEach { s -> put(JSONObject().apply {
            put("id",s.id); put("name",s.name); put("mark",s.mark); put("category",s.category); put("amount",s.amount); put("cycle",s.cycle.name); put("firstDate",s.firstDate?.toString()); put("paymentId",s.paymentId); put("reminder",s.reminder); put("review",s.review); put("status",s.status.name); put("endedAt",s.endedAt?.toString())
        }) } })
        put("payments", JSONArray().apply { state.payments.forEach { p -> put(JSONObject().apply { put("id",p.id);put("name",p.name);put("prefix",p.prefix);put("suffix",p.suffix);put("bank",p.bank) }) } })
        val s = state.settings
        put("settings", JSONObject().apply { put("name",s.name);put("budget",s.budget);put("notifications",s.notifications);put("days",JSONArray(s.days.toList()));put("hour",s.hour);put("minute",s.minute);put("push",s.push);put("email",s.email) })
        put("recent",JSONArray(state.recent))
    }
    private fun decode(root: JSONObject): AppState {
        require(root.getInt("version") == 1)
        val subs = root.getJSONArray("subscriptions").let { a -> (0 until a.length()).map { i -> a.getJSONObject(i).let { s ->
            Subscription(s.getString("id"),s.getString("name"),s.getString("mark"),s.getString("category"),s.getLong("amount"),BillingCycle.valueOf(s.getString("cycle")),s.optString("firstDate").takeIf { it.isNotEmpty() }?.let(LocalDate::parse),s.optString("paymentId"),s.getBoolean("reminder"),s.getBoolean("review"),SubscriptionStatus.valueOf(s.getString("status")),s.optString("endedAt").takeIf { it.isNotEmpty() }?.let(LocalDate::parse))
        } } }
        val payments = root.getJSONArray("payments").let { a -> (0 until a.length()).map { i -> a.getJSONObject(i).let { p -> PaymentMethod(p.getString("id"),p.getString("name"),p.getString("prefix"),p.getString("suffix"),p.optBoolean("bank")) } } }
        val s = root.getJSONObject("settings")
        val days = s.getJSONArray("days").let { a -> (0 until a.length()).map(a::getInt).toSet() }
        val settings = AppSettings(s.getString("name"),s.getLong("budget"),s.getBoolean("notifications"),days,s.getInt("hour"),s.getInt("minute"),s.getBoolean("push"),s.getBoolean("email"))
        val recent = root.getJSONArray("recent").let { a -> (0 until a.length()).map(a::getString) }
        return AppState(subs,payments,settings,recent)
    }
}
