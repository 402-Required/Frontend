package kr.ac.tuk.loopcut

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

enum class Screen(val label:String) { LOGIN("로그인"),SIGNUP("회원가입"),HOME("홈"),SUBSCRIPTIONS("내 구독"),HISTORY("지난 구독 이력"),ADD("구독 추가 등록"),SEARCH("서비스 검색"),DETAIL("구독 상세"),ANALYSIS("분석"),MY("마이"),NOTIFICATIONS("결제 알림"),PAYMENTS("결제수단"),EXPORT("데이터 내보내기"),HELP("도움말") }
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LoopCutApp(vm:LoopCutViewModel=viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ready by vm.ready.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    var routeName by rememberSaveable { mutableStateOf(Screen.LOGIN.name) }
    val route=Screen.valueOf(routeName)
    var selected by rememberSaveable { mutableStateOf("Canva") }
    var modal by rememberSaveable { mutableStateOf("") }
    var payment by remember { mutableStateOf(PaymentMethod()) }
    var notice by remember { mutableStateOf("") }
    val snackbar=remember { SnackbarHostState() }
    val keyboard=LocalSoftwareKeyboardController.current
    val focus=LocalFocusManager.current
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it);vm.clearMessage() } }
    fun go(screen:Screen){focus.clearFocus();keyboard?.hide();routeName=screen.name;modal=""}
    fun back(){when(route){Screen.LOGIN->Unit;Screen.SIGNUP->go(Screen.LOGIN);Screen.SEARCH->go(Screen.ADD);Screen.ADD->go(if(vm.editing)Screen.DETAIL else Screen.SUBSCRIPTIONS);Screen.DETAIL,Screen.HISTORY->go(Screen.SUBSCRIPTIONS);Screen.PAYMENTS,Screen.NOTIFICATIONS,Screen.EXPORT,Screen.HELP->go(Screen.MY);else->go(Screen.SUBSCRIPTIONS)}}
    fun add(s:Subscription?=null){vm.editing=false;vm.draft=s?.copy(id=UUID.randomUUID().toString(),status=SubscriptionStatus.ACTIVE,endedAt=null,firstDate=DemoData.today)?:Subscription();go(Screen.ADD)}
    fun showNotice(text:String){notice=text;modal="notice"}
    val main=route in listOf(Screen.HOME,Screen.SUBSCRIPTIONS,Screen.ANALYSIS,Screen.MY)
    BackHandler(enabled=route!=Screen.LOGIN){if(modal.isNotEmpty())modal="" else if(route==Screen.SUBSCRIPTIONS)go(Screen.LOGIN)else back()}
    if(!ready){Box(Modifier.fillMaxSize().background(Bg),contentAlignment=Alignment.Center){CircularProgressIndicator(color=Primary)};return}
    Scaffold(containerColor=Bg,contentWindowInsets=WindowInsets.safeDrawing,snackbarHost={SnackbarHost(snackbar)},bottomBar={
        if(main) Row(Modifier.fillMaxWidth().background(Bg).navigationBarsPadding().padding(horizontal=20.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
            listOf(Screen.HOME,Screen.SUBSCRIPTIONS,Screen.ANALYSIS,Screen.MY).forEachIndexed { i,s ->
                Column(Modifier.weight(1f).height(72.dp).clickable{go(s)}.padding(top=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(60.dp,30.dp).clip(RoundedCornerShape(16.dp)).background(if(route==s)Soft else Color.Transparent),contentAlignment=Alignment.Center) { FigmaIcon(listOf("home",if(route==s)"gridActive" else "grid","chart",if(route==s)"userActive" else "user")[i]) }
                    Small(s.label,color=if(route==s)Primary else Muted)
                }
            }
        }
    }) { insets ->
        Column(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets).imePadding()) {
            if(route !in listOf(Screen.LOGIN,Screen.SEARCH)) Header(if(route==Screen.ADD&&vm.editing)"구독 수정" else if(route==Screen.HOME)"LoopCut"else route.label,if(main)null else ({back()})) {
                when(route){
                    Screen.SUBSCRIPTIONS->{Row(Modifier.clip(RoundedCornerShape(16.dp)).background(Soft).clickable{go(Screen.HISTORY)}.padding(horizontal=10.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){FigmaIcon("history");Small("지난 이력",color=Primary)};Spacer(Modifier.width(12.dp));FilledIconButton({add()},Modifier.size(48.dp).semantics { contentDescription="구독 추가" },colors=IconButtonDefaults.filledIconButtonColors(containerColor=Primary)){FigmaIcon("plus")}}
                    Screen.MY->IconButton({go(Screen.HELP)}){FigmaIcon("help")}
                    else->Unit
                }
            }
            when(route) {
                Screen.LOGIN->LoginScreen(onLogin={go(Screen.SUBSCRIPTIONS);vm.notify("데모로 시작했어요. 실제 인증은 연결 예정이에요.")},onSignup={go(Screen.SIGNUP)},notice=::showNotice)
                Screen.SIGNUP->SignupScreen(onComplete={name->vm.update{it.copy(settings=it.settings.copy(name=name))};go(Screen.SUBSCRIPTIONS);vm.notify("데모 프로필을 만들었어요. 실제 계정은 생성되지 않아요.")},notice=::showNotice)
                Screen.SUBSCRIPTIONS->SubscriptionsScreen(state.active){selected=it.id;go(Screen.DETAIL)}
                Screen.HISTORY->HistoryScreen(state.history,::add)
                Screen.ADD->AddScreen(vm.draft,state.payments,vm.editing,{vm.draft=it},{go(Screen.SEARCH)},{modal="category"},{s->vm.saveSubscription(s);go(Screen.SUBSCRIPTIONS)})
                Screen.SEARCH->SearchScreen(state.recent,onBack={go(Screen.ADD)},onChoose={service->vm.draft=vm.draft.copy(name=service.name,mark=service.mark,category=service.category,amount=service.amount);vm.update{it.copy(recent=(listOf(service.name)+it.recent.filter{n->n!=service.name}).take(5))};go(Screen.ADD)},onCustom={name->vm.draft=vm.draft.copy(name=name,mark=name.take(1));go(Screen.ADD)},onRecent={recent->vm.update{it.copy(recent=recent)}})
                Screen.DETAIL->{val sub=state.subscriptions.find{it.id==selected};if(sub==null)EmptyState("구독을 찾을 수 없어요.")else DetailScreen(sub,state.payments.find{it.id==sub.paymentId},onEdit={vm.editing=true;vm.draft=sub;go(Screen.ADD)},onDelete={modal="delete"})}
                Screen.MY->MyScreen(state,onNavigate=::go,onBudget={modal="budget"},onProfile={modal="profile"})
                Screen.NOTIFICATIONS->NotificationsScreen(state.settings){settings->vm.update{it.copy(settings=settings)};vm.notify("설정을 저장했어요. 실제 알림 발송은 연결 예정이에요.")}
                Screen.PAYMENTS->PaymentsScreen(state){p->payment=p?:PaymentMethod();modal="payment"}
                Screen.EXPORT->ExportScreen(state,vm::notify)
                Screen.HELP->HelpScreen(::showNotice)
                Screen.HOME,Screen.ANALYSIS->OverviewScreen(state,route==Screen.ANALYSIS,onAdd={add()},onDetail={selected=it.id;go(Screen.DETAIL)})
            }
        }
    }
    if(modal=="category") ModalBottomSheet(onDismissRequest={modal=""},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),containerColor=Color.White) {
        Column(Modifier.padding(horizontal=20.dp).padding(bottom=24.dp)) {
            Title("카테고리 선택");Small("구독 성격에 맞는 분류를 골라주세요");Spacer(Modifier.height(16.dp))
            DemoData.categories.chunked(2).forEach { pair->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){pair.forEach { category ->
                val chosen=vm.draft.category==category
                Surface(onClick={vm.draft=vm.draft.copy(category=category)},modifier=Modifier.weight(1f).padding(bottom=8.dp),shape=RoundedCornerShape(16.dp),color=if(chosen)Soft else Bg,border=if(chosen)androidx.compose.foundation.BorderStroke(1.5.dp,Primary)else null) {
                    Column(Modifier.padding(14.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text(category,fontWeight=FontWeight.Bold,color=if(chosen)Primary else Ink);if(chosen){Spacer(Modifier.width(8.dp));FigmaIcon("check")}};Small(DemoData.categoryHints[DemoData.categories.indexOf(category)])}
                }
            }}}
            Spacer(Modifier.height(8.dp));PrimaryButton("선택 완료",Modifier.align(Alignment.CenterHorizontally),compact=true){modal=""}
        }
    }
    if(modal=="notice") AlertDialog(onDismissRequest={modal=""},title={Title("안내")},text={Text(notice)},confirmButton={TextButton({modal=""}){Text("확인")}},containerColor=Color.White)
    if(modal=="delete") AlertDialog(onDismissRequest={modal=""},title={Title("관리 목록에서 삭제할까요?")},text={Text("지난 구독 이력에 보관해요. 실제 서비스의 구독은 해지되지 않아요.")},confirmButton={TextButton({vm.archive(selected);go(Screen.SUBSCRIPTIONS)}){Text("삭제하고 이력에 보관")}},dismissButton={TextButton({modal=""}){Text("취소")}},containerColor=Color.White)
    if(modal=="budget"||modal=="profile") {
        var value by remember(modal){mutableStateOf(if(modal=="budget")state.settings.budget.toString()else state.settings.name)}
        AlertDialog(onDismissRequest={modal=""},title={Title(if(modal=="budget")"월 예산 수정"else "프로필 수정")},text={LabeledField(if(modal=="budget")"예산 (원)"else "이름",value,{value=it},keyboard=if(modal=="budget")KeyboardType.Number else KeyboardType.Text)},confirmButton={TextButton({vm.update{it.copy(settings=if(modal=="budget")it.settings.copy(budget=value.toLong())else it.settings.copy(name=value.trim()))};modal=""},enabled=if(modal=="budget")(value.toLongOrNull()?:0)>0 else value.isNotBlank()){Text("저장")}},dismissButton={TextButton({modal=""}){Text("취소")}},containerColor=Color.White)
    }
    if(modal=="payment") PaymentDialog(payment,onDismiss={modal=""}){vm.savePayment(it);modal=""}
}


