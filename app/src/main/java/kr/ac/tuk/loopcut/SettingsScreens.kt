package kr.ac.tuk.loopcut

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable fun MyScreen(state:AppState,onNavigate:(Screen)->Unit,onBudget:()->Unit,onProfile:()->Unit) {
    PageColumn {
        Panel(Modifier.clickable(onClick=onProfile)){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){Box(Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Soft),contentAlignment=Alignment.Center){Text("T",color=Primary,fontWeight=FontWeight.Bold,fontSize=20.sp)};Column{Title("${state.settings.name}님");Small("구독 관리 23일째 · 예시 프로필")}}}
        Spacer(Modifier.height(16.dp));Panel(color=Deep){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("월 예산 · ${if(state.total>state.settings.budget)"초과"else "주의"}",color=Color(0xFFFEF3C7),fontSize=14.sp);Link("수정",Modifier.height(24.dp),color=Color(0xFF9BD8C9),onClick=onBudget)};Text(state.settings.budget.won(),color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={ (state.total.toFloat()/state.settings.budget.coerceAtLeast(1)).coerceIn(0f,1f) },modifier=Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),color=Color(0xFF9EE7CE),trackColor=Primary);Spacer(Modifier.height(8.dp));Small("${state.total.won()} 사용 · ${kotlin.math.abs(state.settings.budget-state.total).won()} ${if(state.total>state.settings.budget)"초과"else "남음"}",color=Color.White)}
        Spacer(Modifier.height(16.dp));Panel(padding=16.dp,verticalPadding=0.dp){
            MenuRow("결제 알림","결제 3일 전 알려드려요","bellMuted",if(state.settings.notifications)"켜짐"else "꺼짐"){onNavigate(Screen.NOTIFICATIONS)};HorizontalDivider(color=Line)
            MenuRow("결제수단","등록한 구독의 결제수단 관리","cardMuted","${state.payments.size}개"){onNavigate(Screen.PAYMENTS)};HorizontalDivider(color=Line)
            MenuRow("데이터 내보내기","내 구독 목록을 파일로 저장","download"){onNavigate(Screen.EXPORT)};HorizontalDivider(color=Line)
            MenuRow("도움말","자주 묻는 질문과 문의","help"){onNavigate(Screen.HELP)}
        }
        Spacer(Modifier.height(28.dp));Column(Modifier.padding(horizontal=12.dp)){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){FigmaIcon("shield");Text("내 구독 정보는 안전하게",color=Primary,fontSize=14.sp,fontWeight=FontWeight.Medium)};Spacer(Modifier.height(12.dp));Small("필요한 정보만 입력하고 직접 관리할 수 있어요.");Spacer(Modifier.height(8.dp));Small("루프컷 v1.0 · GDGoC TUK")}
    }
}
@Composable private fun MenuRow(title:String,description:String,icon:String,value:String="",onClick:()->Unit){Row(Modifier.fillMaxWidth().heightIn(min=64.dp).clickable(onClick=onClick),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Bg),contentAlignment=Alignment.Center){FigmaIcon(icon)};Column(Modifier.weight(1f)){Text(title,fontSize=14.sp,fontWeight=FontWeight.Medium);Small(description)};if(value.isNotEmpty())Small(value,color=Primary);FigmaIcon("next")}}
@Composable fun NotificationsScreen(initial:AppSettings,onSave:(AppSettings)->Unit) {
    var enabled by rememberSaveable{mutableStateOf(initial.notifications)};var days by remember{mutableStateOf(initial.days)};var hour by rememberSaveable{mutableStateOf(initial.hour)};var minute by rememberSaveable{mutableStateOf(initial.minute)};var push by rememberSaveable{mutableStateOf(initial.push)};var email by rememberSaveable{mutableStateOf(initial.email)};val context=LocalContext.current
    Column(Modifier.fillMaxSize()){
        PageColumn(Modifier.weight(1f)){
            Panel{SettingToggle("결제 알림 받기","등록한 구독의 결제일을 미리 알려드려요",enabled){enabled=it}}
            Spacer(Modifier.height(16.dp));SectionTitle("알림 시점");Spacer(Modifier.height(8.dp));Panel{listOf(7,3,1,0).forEachIndexed{i,d->SettingToggle(if(d==0)"결제 당일"else "결제 ${d}일 전","",d in days,enabled){days=if(it)days+d else days-d};if(i<3)HorizontalDivider(color=Line)}}
            Spacer(Modifier.height(16.dp));SectionTitle("알림 시간");Spacer(Modifier.height(8.dp));Panel(Modifier.clickable{TimePickerDialog(context,{_,h,m->hour=h;minute=m},hour,minute,false).show()}){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Text("알림 받을 시간");Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Small("${if(hour<12)"오전"else "오후"} ${if(hour%12==0)12 else hour%12}:%02d".format(minute));FigmaIcon("next")}}}
            Spacer(Modifier.height(16.dp));SectionTitle("알림 방식");Spacer(Modifier.height(8.dp));Panel{SettingToggle("앱 푸시","결제일 전에 휴대폰으로 알려드려요",push,enabled){push=it};HorizontalDivider(color=Line);SettingToggle("이메일","가입한 이메일로 함께 보내드려요",email,enabled){email=it}}
            Spacer(Modifier.height(12.dp));Small("알림은 결제 예정일 기준으로 발송돼요.")
        }
        BottomButton("저장하기"){onSave(initial.copy(notifications=enabled,days=days,hour=hour,minute=minute,push=push,email=email))}
    }
}
@Composable private fun SettingToggle(title:String,description:String,checked:Boolean,enabled:Boolean=true,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth().heightIn(min=52.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Column(Modifier.weight(1f)){Text(title);if(description.isNotBlank())Small(description)};Toggle(checked,title,enabled,onChange)}}
@Composable fun PaymentsScreen(state:AppState,onEdit:(PaymentMethod?)->Unit){
    Column(Modifier.fillMaxSize()){
        PageColumn(Modifier.weight(1f)){
            Text("등록한 결제수단 ${state.payments.size}개",fontSize=14.sp,color=Muted);Spacer(Modifier.height(12.dp));state.payments.forEachIndexed{i,p->
                val subs=state.active.filter{it.paymentId==p.id}
                Panel{Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){FigmaIcon(if(p.bank)"14f6a"else "bf2de");Column(Modifier.weight(1f)){Text(p.name,fontSize=16.sp);Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Small(p.masked);if(i==0)Badge("기본")}}};Spacer(Modifier.height(10.dp));HorizontalDivider(color=Line);Row(Modifier.fillMaxWidth().height(28.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Small("구독 ${subs.size}개 · 이번 달 ${subs.sumOf{it.monthlyAmount}.won()}");Link("수정"){onEdit(p)}}};Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(16.dp));SectionTitle("이번 달 결제수단별 지출");Spacer(Modifier.height(12.dp));Panel{
                Row(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp))){state.payments.forEachIndexed{i,p->val total=state.active.filter{it.paymentId==p.id}.sumOf{it.monthlyAmount};if(total>0)Box(Modifier.weight(total.toFloat()).fillMaxHeight().background(if(i==0)Primary else Color(0xFF98DACA)))}}
                Spacer(Modifier.height(16.dp));state.payments.forEachIndexed{i,p->Row(Modifier.fillMaxWidth().padding(bottom=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){Box(Modifier.size(10.dp).clip(CircleShape).background(if(i==0)Primary else Color(0xFF98DACA)));Text(p.name.substringBefore(" 체크카드"),Modifier.weight(1f),fontSize=14.sp);Text(state.active.filter{it.paymentId==p.id}.sumOf{it.monthlyAmount}.won(),fontSize=14.sp)}};HorizontalDivider(color=Line);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Small("합계");Text(state.total.won(),fontSize=12.sp,fontWeight=FontWeight.Bold)}
            }
            Spacer(Modifier.height(28.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FigmaIcon("shield");Small("카드번호는 앞 6자리와 뒤 4자리만 보여드려요.")}
        }
        BottomButton("결제수단 추가"){onEdit(null)}
    }
}
@Composable fun PaymentDialog(initial:PaymentMethod,onDismiss:()->Unit,onSave:(PaymentMethod)->Unit){var name by rememberSaveable{mutableStateOf(initial.name)};var prefix by rememberSaveable{mutableStateOf(initial.prefix)};var suffix by rememberSaveable{mutableStateOf(initial.suffix)};AlertDialog(onDismissRequest=onDismiss,title={Title(if(initial.name.isBlank())"결제수단 추가"else "결제수단 수정")},text={Column{LabeledField("결제수단 이름",name,{name=it.take(50)});LabeledField("앞 6자리",prefix,{prefix=it.filter(Char::isDigit).take(6)},keyboard=KeyboardType.Number);LabeledField("뒤 4자리",suffix,{suffix=it.filter(Char::isDigit).take(4)},keyboard=KeyboardType.Number);Small("중간 번호·유효기간·CVC는 입력하지 않아요. 실제 결제는 이루어지지 않아요.")}},confirmButton={TextButton({onSave(initial.copy(name=name.trim(),prefix=prefix,suffix=suffix))},enabled=name.isNotBlank()&&prefix.length==6&&suffix.length==4){Text("저장하기")}},dismissButton={TextButton(onDismiss){Text("취소")}},containerColor=Color.White)}
@Composable fun HelpScreen(notice:(String)->Unit){var query by rememberSaveable{mutableStateOf("")};var expanded by rememberSaveable{mutableStateOf(0)};val items=listOf("구독은 어떻게 등록하나요?" to "내 구독 탭의 + 버튼이나 홈의 새 구독 등록을 눌러 서비스, 금액, 결제일을 입력하면 돼요.","결제 알림은 언제 오나요?" to "결제 알림 설정에서 원하는 시점과 시간을 선택할 수 있어요. 데모에서는 설정만 저장되며 실제 알림은 발송되지 않아요.","결제수단을 바꾸고 싶어요" to "마이 > 결제수단에서 추가·수정한 다음, 구독 수정에서 사용할 결제수단을 선택해주세요.","구독을 삭제하면 이력도 사라지나요?" to "지난 구독 이력으로 옮겨져 계속 보관돼요. 실제 서비스의 해지는 별도로 진행해주세요.","내 정보는 어디에 저장되나요?" to "데모 정보는 이 기기의 앱 저장소에만 저장돼요. 앱 삭제·데이터 초기화 시 사라지며 다른 기기와 동기화되지 않아요.")
    PageColumn {SearchField(query,{query=it},"궁금한 내용을 검색하세요");Spacer(Modifier.height(24.dp));SectionTitle("자주 묻는 질문");Spacer(Modifier.height(12.dp));Panel{items.forEachIndexed{i,(q,a)->if(q.contains(query)){Row(Modifier.fillMaxWidth().heightIn(min=56.dp).clickable{expanded=if(expanded==i)-1 else i},verticalAlignment=Alignment.CenterVertically){Text(q,Modifier.weight(1f));FigmaIcon(if(expanded==i)"69a47"else "99fdd")};if(expanded==i){Text(a,fontSize=14.sp,lineHeight=22.sp,color=Muted);Spacer(Modifier.height(20.dp))};if(i<items.lastIndex)HorizontalDivider(color=Line)}};if(items.none{it.first.contains(query)})Small("검색 결과가 없어요.")};Spacer(Modifier.height(16.dp));SectionTitle("문의하기");Spacer(Modifier.height(12.dp));Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){listOf("1:1 문의","이메일 문의").forEachIndexed{i,s->Panel(Modifier.weight(1f).clickable{notice(if(i==0)"문의 접수는 백엔드 연결 후 제공돼요. 지금은 전송되지 않아요."else "운영 이메일 주소가 아직 정해지지 않았어요.")}){Box(Modifier.size(40.dp).clip(CircleShape).background(Soft),contentAlignment=Alignment.Center){FigmaIcon(if(i==0)"message"else "mail")};Spacer(Modifier.height(8.dp));Text(s);Small(if(i==0)"앱에서 바로 남겨요"else "메일로 문의하기")}}};Spacer(Modifier.height(28.dp));Small("운영시간 평일 10:00 – 18:00 (공휴일 휴무)",Modifier.align(Alignment.CenterHorizontally));Small("루프컷 v1.0 · GDGoC TUK",Modifier.align(Alignment.CenterHorizontally))}
}
@Composable fun OverviewScreen(state:AppState,analysis:Boolean,onAdd:()->Unit,onDetail:(Subscription)->Unit){PageColumn{Text(if(analysis)"내 구독의 월 환산 금액을 확인해요."else "${state.settings.name}님, 이번 달 구독을 확인해보세요.",color=Muted);Spacer(Modifier.height(20.dp));Panel(color=Deep){Small("이번 달 구독 지출",color=Color.White);Text(state.total.won(),color=Color.White,fontSize=32.sp,fontWeight=FontWeight.Bold);Small("이용 중인 구독 ${state.active.size}개",color=Color.White)};Spacer(Modifier.height(20.dp));if(!analysis){PrimaryButton("새 구독 등록",onClick=onAdd);Spacer(Modifier.height(24.dp));SectionTitle("다가오는 결제");Spacer(Modifier.height(12.dp));state.active.sortedBy{it.nextPayment(DemoData.today)}.take(4).forEach{SubscriptionRow(it){onDetail(it)};Spacer(Modifier.height(8.dp))}}else{SectionTitle("카테고리별 지출");Spacer(Modifier.height(12.dp));Panel{state.active.groupBy{it.category}.forEach{(category,subs)->val amount=subs.sumOf{it.monthlyAmount};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(category);Text(amount.won(),fontWeight=FontWeight.Bold)};Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={amount.toFloat()/state.total.coerceAtLeast(1)},modifier=Modifier.fillMaxWidth(),color=Primary,trackColor=Soft);Spacer(Modifier.height(20.dp))}};Spacer(Modifier.height(24.dp));SectionTitle("확인이 필요한 구독");Spacer(Modifier.height(12.dp));state.active.filter{it.review}.forEach{SubscriptionRow(it){onDetail(it)}};Spacer(Modifier.height(12.dp));Small("실제 사용량 측정 결과가 아닌 예시 상태예요.")}}}

