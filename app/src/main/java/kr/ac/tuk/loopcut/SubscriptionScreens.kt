package kr.ac.tuk.loopcut

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable fun SubscriptionsScreen(subscriptions:List<Subscription>,onDetail:(Subscription)->Unit) {
    var query by rememberSaveable{mutableStateOf("")};var filter by rememberSaveable{mutableStateOf("전체")};var sort by rememberSaveable{mutableStateOf(0)};var dropdown by remember{mutableStateOf(false)}
    val filtered=subscriptions.filter{(filter=="전체"||it.category==filter)&&it.name.contains(query,true)}.let{list->when(sort){1->list.sortedByDescending{it.amount};2->list.sortedBy{it.name};else->list.sortedBy{it.nextPayment(DemoData.today)}}}
    PageColumn {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {Title("전체 ${subscriptions.size}개");Box{Link(listOf("결제일순","높은 금액순","이름순")[sort],Modifier.height(28.dp),color=Muted){dropdown=true};DropdownMenu(dropdown,{dropdown=false}){listOf("결제일순","높은 금액순","이름순").forEachIndexed{i,s->DropdownMenuItem({Text(s)},{sort=i;dropdown=false})}}}}
        Spacer(Modifier.height(16.dp));SearchField(query,{query=it},"서비스 이름으로 검색");Spacer(Modifier.height(12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("전체","영상","음악","생산성").forEach{Chip(it,filter==it,white=true,modifier=Modifier.width(if(it=="생산성")104.dp else 74.dp)){filter=it}}}
        Spacer(Modifier.height(12.dp));filtered.forEach{SubscriptionRow(it){onDetail(it)};Spacer(Modifier.height(8.dp))}
        if(filtered.isEmpty())EmptyState("검색된 구독이 없어요.")
    }
}
@Composable fun HistoryScreen(history:List<Subscription>,onRestart:(Subscription)->Unit) {
    var filter by rememberSaveable{mutableStateOf(0)}
    PageColumn(top=4.dp) {
        Panel(color=Color(0xFF0B4C44)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Small("지금까지 정리한 구독",color=Color(0xFF99CEC2));Spacer(Modifier.height(10.dp));Text("${history.size}개",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold)};Column(horizontalAlignment=Alignment.End){Small("정리한 구독의 월 환산 금액",color=Color(0xFF99CEC2));Spacer(Modifier.height(10.dp));Text("월 ${history.sumOf{it.monthlyAmount}.won()}",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)}}}
        Spacer(Modifier.height(16.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("전체","해지","만료").forEachIndexed{i,s->val count=history.count{i==0||it.status==if(i==1)SubscriptionStatus.CANCELLED else SubscriptionStatus.EXPIRED};Chip("$s $count",filter==i){filter=i}}};Spacer(Modifier.height(16.dp))
        history.filter{filter==0||it.status==if(filter==1)SubscriptionStatus.CANCELLED else SubscriptionStatus.EXPIRED}.forEach{s->
            val months=if(s.firstDate!=null&&s.endedAt!=null)ChronoUnit.MONTHS.between(s.firstDate.withDayOfMonth(1),s.endedAt.withDayOfMonth(1))+1 else 0
            Panel(padding=16.dp){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Mark(s.name,s.mark);Column{Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){Text(s.name);Badge(if(s.status==SubscriptionStatus.CANCELLED)"해지"else "만료",s.status==SubscriptionStatus.CANCELLED)};Small("${s.firstDate.toString().take(7).replace('-','.')} – ${s.endedAt.toString().take(7).replace('-','.')} · ${months}개월")}};Spacer(Modifier.height(10.dp));HorizontalDivider(color=Line);Row(Modifier.fillMaxWidth().height(24.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Small("월 ${s.monthlyAmount.won()} · 총 ${(s.monthlyAmount*months).won()}");Link("다시 구독"){onRestart(s)}}};Spacer(Modifier.height(12.dp))
        }
        if(history.isEmpty())EmptyState("지난 구독 이력이 없어요.")
        Spacer(Modifier.height(16.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FigmaIcon("history");Small("삭제한 구독도 이력은 이 화면에 계속 보관돼요.")}
    }
}
@Composable fun AddScreen(draft:Subscription,payments:List<PaymentMethod>,editing:Boolean,onChange:(Subscription)->Unit,onSearch:()->Unit,onCategory:()->Unit,onSave:(Subscription)->Unit) {
    val context=LocalContext.current;var dropdown by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize()) {
        PageColumn(Modifier.weight(1f)) {
            Panel(Modifier.clickable(onClick=onSearch)) { Row(Modifier.heightIn(min=52.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Mark(draft.name,draft.mark);Column(Modifier.weight(1f)){SectionTitle(draft.name.ifBlank{"어떤 서비스를 등록할까요?"});Small("서비스 검색 또는 직접 입력")};FigmaIcon("searchPrimary")} }
            Spacer(Modifier.height(12.dp));LabeledField("결제 금액",draft.amount.takeIf{it>0}?.toString()?:"",{v->if(v.length<=10&&v.all(Char::isDigit))onChange(draft.copy(amount=v.toLongOrNull()?:0))},"금액을 입력하세요",KeyboardType.Number)
            Small("결제 주기");Spacer(Modifier.height(8.dp));Row(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(16.dp)).background(Soft).padding(4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)){BillingCycle.entries.forEach{cycle->Row(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(if(draft.cycle==cycle)Color.White else Color.Transparent).clickable{onChange(draft.copy(cycle=cycle))},verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center){if(draft.cycle==cycle){FigmaIcon("check");Spacer(Modifier.width(8.dp))};Text(if(cycle==BillingCycle.MONTHLY)"매월"else "매년",fontSize=14.sp,color=if(draft.cycle==cycle)Primary else Muted)}}}
            Spacer(Modifier.height(12.dp));Small("첫 결제일");Spacer(Modifier.height(8.dp));Row(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable{val date=draft.firstDate?:DemoData.today;DatePickerDialog(context,{_,y,m,d->onChange(draft.copy(firstDate=LocalDate.of(y,m+1,d)))},date.year,date.monthValue-1,date.dayOfMonth).show()}.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){FigmaIcon("calendar");Text(draft.firstDate?.toString()?.replace('-','.')?:"첫 결제일을 선택하세요",color=Muted)}
            Spacer(Modifier.height(12.dp));Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Column(Modifier.weight(1f)){Small("카테고리");Spacer(Modifier.height(8.dp));Row(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable(onClick=onCategory).padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){FigmaIcon("grid");Text(draft.category.ifBlank{"선택"},fontSize=14.sp,modifier=Modifier.weight(1f),maxLines=1);FigmaIcon("down")}};Column(Modifier.weight(1f)){Small("결제수단");Spacer(Modifier.height(8.dp));Box{Row(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable{dropdown=true}.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){FigmaIcon("card");Text(payments.find{it.id==draft.paymentId}?.name?.substringBefore(' ')?:"선택",fontSize=14.sp,modifier=Modifier.weight(1f));FigmaIcon("down")};DropdownMenu(dropdown,{dropdown=false}){payments.forEach{p->DropdownMenuItem({Text(p.name)},{onChange(draft.copy(paymentId=p.id));dropdown=false})}}}}}
            Spacer(Modifier.height(12.dp));Panel { Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){FigmaIcon("bell");Column(Modifier.weight(1f)){Text("결제 알림",fontSize=14.sp);Small("3일 전 · 당일")};Toggle(draft.reminder,"구독 결제 알림"){onChange(draft.copy(reminder=it))}} }
            Spacer(Modifier.height(12.dp));Small("등록한 정보는 언제든 수정할 수 있어요.")
        }
        BottomButton(if(editing)"수정하기"else "등록하기",enabled=draft.name.isNotBlank()&&draft.amount>0&&draft.firstDate!=null&&draft.category.isNotBlank()&&draft.paymentId.isNotBlank(),compact=true){onSave(draft)}
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable fun SearchScreen(recent:List<String>,onBack:()->Unit,onChoose:(Service)->Unit,onCustom:(String)->Unit,onRecent:(List<String>)->Unit) {
    var query by rememberSaveable{mutableStateOf("")};var category by rememberSaveable{mutableStateOf("")};var custom by rememberSaveable{mutableStateOf(false)};var name by rememberSaveable{mutableStateOf("")}
    Row(Modifier.fillMaxWidth().padding(horizontal=20.dp).height(72.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onBack,Modifier.width(40.dp)){FigmaIcon("back")};SearchField(query,{query=it},"서비스 이름을 검색하세요",outlined=true,modifier=Modifier.weight(1f))}
    PageColumn {
        if(query.isBlank()&&category.isBlank()) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){SectionTitle("최근 검색");Link("전체 삭제",color=Muted){onRecent(emptyList())}}
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){recent.forEach{r->Row(Modifier.clip(RoundedCornerShape(20.dp)).background(Soft).padding(start=14.dp),verticalAlignment=Alignment.CenterVertically){Text(r,Modifier.clickable{query=r},fontSize=14.sp);IconButton({onRecent(recent-r)},Modifier.size(32.dp)){FigmaIcon("close")}}}}
            Spacer(Modifier.height(24.dp));SectionTitle("인기 서비스")
        } else {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){SectionTitle(if(category.isNotBlank())category else "검색 결과 ${DemoData.services.count{"${it.name} ${it.aliases}".contains(query,true)}}개");if(category.isNotBlank())Link("전체 보기"){category=""}}}
        Spacer(Modifier.height(12.dp))
        val found=if(query.isBlank()&&category.isBlank())DemoData.services.take(5)else DemoData.services.filter{"${it.name} ${it.aliases}".contains(query,true)&&(category.isBlank()||it.category==category)}
        if(found.isNotEmpty())Panel(padding=16.dp){found.forEachIndexed{i,s->ServiceRow(s){onChoose(s)};if(i<found.lastIndex)HorizontalDivider(color=Line)}}
        else EmptyState("검색 결과가 없어요. 직접 등록해보세요.")
        if(query.isNotBlank()) {
            Spacer(Modifier.height(12.dp));OutlinedButton({onCustom(query.trim())},Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Primary),contentPadding=PaddingValues(16.dp)){Column(Modifier.weight(1f)){Text("\"$query\"(으)로 직접 등록하기",fontSize=16.sp);Small("서비스 이름과 금액을 직접 입력해요")};FigmaIcon("next")}
            Spacer(Modifier.height(16.dp));SectionTitle("같은 카테고리 서비스");Spacer(Modifier.height(8.dp));Panel{DemoData.services.filter{it.category==(found.firstOrNull()?.category?:"영상")&&it !in found}.takeLast(3).forEach{ServiceRow(it){onChoose(it)}}}
        } else {
            Spacer(Modifier.height(24.dp));SectionTitle("카테고리로 찾기");Spacer(Modifier.height(12.dp));FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){DemoData.categories.forEach{Chip(it,it==category){category=it}}}
            Spacer(Modifier.height(24.dp));Link("찾는 서비스가 없다면 직접 입력하기",Modifier.align(Alignment.CenterHorizontally)){custom=true}
        }
    }
    if(custom)AlertDialog(onDismissRequest={custom=false},title={Title("서비스 직접 입력")},text={LabeledField("서비스 이름",name,{name=it.take(40)})},confirmButton={TextButton({onCustom(name.trim());custom=false},enabled=name.isNotBlank()){Text("계속하기")}},dismissButton={TextButton({custom=false}){Text("취소")}},containerColor=Color.White)
}
@Composable private fun ServiceRow(service:Service,onClick:()->Unit){Row(Modifier.fillMaxWidth().heightIn(min=64.dp).clickable(onClick=onClick),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Mark(service.name,service.mark);Column(Modifier.weight(1f)){Text(service.name);Small(service.category)};FigmaIcon("addService")}}
@Composable fun DetailScreen(s:Subscription,payment:PaymentMethod?,onEdit:()->Unit,onDelete:()->Unit) {
    Column(Modifier.fillMaxSize()) {
        PageColumn(Modifier.weight(1f)) {
            Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){Mark(s.name,s.mark,true);Spacer(Modifier.height(4.dp));Title(if(s.name=="Canva")"Canva Pro"else s.name);Spacer(Modifier.height(4.dp));Small("${s.category}  ·  ${if(s.review)"미사용 확인 필요"else "이용 중"}")}
            Spacer(Modifier.height(32.dp));Panel(color=Deep){val next=s.nextPayment(DemoData.today);Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Text("다음 결제 ${next.monthValue}월 ${next.dayOfMonth}일",color=Color.White,fontSize=14.sp,fontWeight=FontWeight.Medium);Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF3C7)).padding(horizontal=12.dp,vertical=3.dp)){Small("D−${s.daysUntil(DemoData.today)}",color=Color(0xFF92400E))}};Text(s.amount.won(),color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold)}
            Spacer(Modifier.height(16.dp));Panel { Fact("결제 주기","${if(s.cycle==BillingCycle.MONTHLY)"매월"else "매년"} ${s.firstDate?.dayOfMonth}일");Spacer(Modifier.height(16.dp));Fact("결제수단",payment?.name?:"미지정",payment?.masked);Spacer(Modifier.height(16.dp));Fact("결제 알림",if(s.reminder)"3일 전 · 당일"else "꺼짐");Spacer(Modifier.height(16.dp));Fact("등록일",s.firstDate.toString().replace('-','.')) }
            Spacer(Modifier.height(16.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){SectionTitle("최근 결제 내역");Small("예시 2건",color=Primary)};Spacer(Modifier.height(16.dp));Panel { listOf(1L,2L).forEachIndexed{i,n->val date=s.nextPayment(DemoData.today).minusMonths(if(s.cycle==BillingCycle.MONTHLY)n else n*12);Fact(date.toString().replace('-','.'),s.amount.won());if(i==0)Spacer(Modifier.height(20.dp))} }
            Spacer(Modifier.height(12.dp));Link("관리 목록에서 삭제",Modifier.align(Alignment.CenterHorizontally),Danger,onDelete)
        }
        BottomButton("구독 수정",compact=true,onClick=onEdit)
    }
}
@Composable fun Fact(label:String,value:String,note:String?=null) {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){Text(label,color=Muted,fontSize=16.sp);Column(Modifier.weight(1f),horizontalAlignment=Alignment.End){Text(value,fontSize=14.sp,fontWeight=FontWeight.Medium);if(note!=null)Small(note)}}}

