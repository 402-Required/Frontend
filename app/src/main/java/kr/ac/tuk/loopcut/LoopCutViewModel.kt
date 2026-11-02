package kr.ac.tuk.loopcut

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

class LoopCutViewModel(application: Application): AndroidViewModel(application) {
    var draft by mutableStateOf(Subscription())
    var editing by mutableStateOf(false)
    private val repository: SubscriptionRepository = LocalRepository(application)
    private val mutableState = MutableStateFlow(AppState())
    val state = mutableState.asStateFlow()
    private val mutableReady = MutableStateFlow(false)
    val ready = mutableReady.asStateFlow()
    private val mutableMessage = MutableStateFlow<String?>(null)
    val message = mutableMessage.asStateFlow()
    private val mutex = Mutex()
    init { viewModelScope.launch { mutableState.value = repository.load(); mutableReady.value = true } }
    fun notify(message: String) { mutableMessage.value = message }
    fun clearMessage() { mutableMessage.value = null }
    fun update(transform: (AppState) -> AppState) { viewModelScope.launch { mutex.withLock {
        val next = transform(mutableState.value)
        runCatching { repository.save(next) }.onSuccess { mutableState.value = next }.onFailure { notify("저장하지 못했어요. 기기 저장 공간을 확인해주세요.") }
    } } }
    fun saveSubscription(subscription: Subscription) = update { state -> state.copy(subscriptions = if (state.subscriptions.any { it.id == subscription.id }) state.subscriptions.map { if (it.id == subscription.id) subscription else it } else state.subscriptions + subscription) }
    fun archive(id: String) = update { it.copy(subscriptions = it.subscriptions.map { sub -> if (sub.id == id) sub.copy(status = SubscriptionStatus.CANCELLED, endedAt = DemoData.today) else sub }) }
    fun savePayment(payment: PaymentMethod) = update { state -> state.copy(payments = if (state.payments.any { it.id == payment.id }) state.payments.map { if (it.id == payment.id) payment else it } else state.payments + payment) }
}
