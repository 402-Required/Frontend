package kr.ac.tuk.loopcut

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable fun ExportScreen(state:AppState,notify:(String)->Unit) {
    var active by rememberSaveable{mutableStateOf(true)};var history by rememberSaveable{mutableStateOf(true)};var format by rememberSaveable{mutableStateOf("CSV")};var period by rememberSaveable{mutableStateOf("전체")};var from by rememberSaveable{mutableStateOf("2026-01-01")};var until by rememberSaveable{mutableStateOf(DemoData.today.toString())};var saving by rememberSaveable{mutableStateOf(false)}
    val context=LocalContext.current;val scope=rememberCoroutineScope()
    val start=when(period){"올해"->DemoData.today.withDayOfYear(1);"최근 1년"->DemoData.today.minusYears(1);"직접 선택"->LocalDate.parse(from);else->LocalDate.MIN}
    val end=if(period=="직접 선택")LocalDate.parse(until)else LocalDate.MAX
    val entries=state.subscriptions.filter{(if(it.status==SubscriptionStatus.ACTIVE)active else history)&&(it.firstDate?:DemoData.today) in start..end}
    var pending by remember{mutableStateOf(emptyList<Subscription>())}
    fun save(uri:android.net.Uri?,type:String){if(uri==null){saving=false;return};scope.launch{val result=withContext(Dispatchers.IO){runCatching{context.contentResolver.openOutputStream(uri)?.use{stream->ExportWriter.write(context,pending,type,stream)}?:error("파일을 열 수 없어요.")}};saving=false;notify(if(result.isSuccess)"파일을 저장했어요."else "파일을 저장하지 못했어요. 저장 위치를 확인해주세요.")}}
    val csv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){save(it,"CSV")}
    val excel=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){save(it,"Excel")}
    val pdf=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){save(it,"PDF")}
    Column(Modifier.fillMaxSize()){
        PageColumn(Modifier.weight(1f)){
            SectionTitle("내보낼 항목");Spacer(Modifier.height(8.dp));Panel{
                CheckRow("현재 구독 목록",active,{active=it});Small("이용 중인 구독 ${state.active.size}개",Modifier.padding(start=30.dp));Spacer(Modifier.height(8.dp));HorizontalDivider(color=Line)
                CheckRow("지난 구독 이력",history,{history=it});Small("해지 · 만료된 구독 ${state.history.size}개",Modifier.padding(start=30.dp));Spacer(Modifier.height(8.dp));HorizontalDivider(color=Line)
                Row(verticalAlignment=Alignment.CenterVertically){Checkbox(false,null,enabled=false);Column{Text("결제 내역",color=Muted);Small("실제 결제 내역은 백엔드 연결 후 제공")}}
            }
            Spacer(Modifier.height(16.dp));SectionTitle("파일 형식");Spacer(Modifier.height(8.dp));Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){listOf("CSV","Excel","PDF").forEach{f->Panel(Modifier.weight(1f).clickable{format=f},color=if(format==f)Soft else androidx.compose.ui.graphics.Color.White){Text(f,Modifier.align(Alignment.CenterHorizontally),color=if(format==f)Primary else Ink);Small(if(f=="CSV")"시트 호환"else if(f=="Excel")".xlsx"else "보기용",Modifier.align(Alignment.CenterHorizontally))}}}
            Spacer(Modifier.height(16.dp));SectionTitle("기간");Spacer(Modifier.height(8.dp));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("전체","최근 1년","올해","직접 선택").forEach{p->Chip(p,period==p){period=p}}}
            if(period=="직접 선택"){Spacer(Modifier.height(8.dp));Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){Link("시작 $from"){val d=LocalDate.parse(from);DatePickerDialog(context,{_,y,m,day->from=LocalDate.of(y,m+1,day).toString()},d.year,d.monthValue-1,d.dayOfMonth).show()};Link("종료 $until"){val d=LocalDate.parse(until);DatePickerDialog(context,{_,y,m,day->until=LocalDate.of(y,m+1,day).toString()},d.year,d.monthValue-1,d.dayOfMonth).show()}}}
            Spacer(Modifier.height(24.dp));Panel{SectionTitle("내보내기 미리보기");Spacer(Modifier.height(12.dp));Fact("포함 항목","구독 ${entries.size}개");Spacer(Modifier.height(8.dp));Fact("파일 이름","loopcut_${DemoData.today}.${if(format=="Excel")"xlsx"else format.lowercase()}");Spacer(Modifier.height(8.dp));Small("기간은 첫 결제일 기준으로 적용돼요.")}
            Spacer(Modifier.height(18.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FigmaIcon("shield");Small("선택한 저장 위치에 파일을 저장해요.")}
        }
        BottomButton(if(saving)"저장 중…"else "파일로 저장하기",enabled=entries.isNotEmpty()&&!saving&&start<=end){pending=entries;saving=true;val base="loopcut_${DemoData.today}";when(format){"CSV"->csv.launch("$base.csv");"Excel"->excel.launch("$base.xlsx");else->pdf.launch("$base.pdf")}}
    }
}
object ExportWriter {
    val headers=listOf("서비스","카테고리","결제금액","결제주기","첫결제일","상태","종료일")
    fun rows(subs:List<Subscription>):List<List<String>> = listOf(headers)+subs.map{listOf(it.name,it.category,it.amount.toString(),if(it.cycle==BillingCycle.MONTHLY)"매월"else "매년",it.firstDate?.toString().orEmpty(),when(it.status){SubscriptionStatus.ACTIVE->"이용 중";SubscriptionStatus.CANCELLED->"해지";SubscriptionStatus.EXPIRED->"만료"},it.endedAt?.toString().orEmpty())}
    fun csv(subs:List<Subscription>):String="\uFEFF"+rows(subs).joinToString("\r\n"){row->row.joinToString(","){value->val safe=if(value.firstOrNull() in listOf('=','+','-','@','\t','\r'))"'$value"else value;"\"${safe.replace("\"","\"\"")}\""}}
    fun write(context:Context,subs:List<Subscription>,format:String,stream:OutputStream){when(format){"CSV"->stream.write(csv(subs).toByteArray(Charsets.UTF_8));"Excel"->xlsx(subs,stream);else->pdf(context,subs,stream)}}
    private fun xml(value:String)=value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").filter{it=='\t'||it=='\n'||it=='\r'||it.code>=32}
    fun xlsx(subs:List<Subscription>,stream:OutputStream){
        val zip=ZipOutputStream(stream)
        fun entry(name:String,text:String){zip.putNextEntry(ZipEntry(name));zip.write(text.toByteArray(Charsets.UTF_8));zip.closeEntry()}
        entry("[Content_Types].xml","""<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""")
        entry("_rels/.rels","""<?xml version="1.0"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
        entry("xl/workbook.xml","""<?xml version="1.0" encoding="UTF-8"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="구독 목록" sheetId="1" r:id="rId1"/></sheets></workbook>""")
        entry("xl/_rels/workbook.xml.rels","""<?xml version="1.0"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""")
        val data=rows(subs).mapIndexed{i,row->"<row r=\"${i+1}\">"+row.mapIndexed{j,value->val ref="${('A'.code+j).toChar()}${i+1}";if(i>0&&j==2)"<c r=\"$ref\"><v>$value</v></c>"else "<c r=\"$ref\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${xml(value)}</t></is></c>"}.joinToString("")+"</row>"}.joinToString("")
        entry("xl/worksheets/sheet1.xml","""<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><cols><col min="1" max="7" width="22" customWidth="1"/></cols><sheetData>$data</sheetData></worksheet>""")
        zip.finish();zip.flush()
    }
    private fun pdf(context:Context,subs:List<Subscription>,stream:OutputStream){
        val doc=PdfDocument();val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=context.resources.getFont(R.font.noto_sans_kr);textSize=10f;color=android.graphics.Color.rgb(24,46,42)}
        subs.chunked(18).forEachIndexed{index,pageSubs->val page=doc.startPage(PdfDocument.PageInfo.Builder(595,842,index+1).create());val canvas=page.canvas;paint.textSize=22f;canvas.drawText("LoopCut 구독 목록",32f,48f,paint);paint.textSize=10f;canvas.drawText("기준일 ${DemoData.today} · ${index+1} 페이지",32f,70f,paint);var y=106f;pageSubs.forEach{s->paint.textSize=12f;canvas.drawText("${s.name.take(34)}  ·  ${s.amount.won()} / ${if(s.cycle==BillingCycle.MONTHLY)"월"else "년"}",32f,y,paint);paint.textSize=9f;canvas.drawText("${s.category}  |  첫 결제 ${s.firstDate}  |  ${if(s.status==SubscriptionStatus.ACTIVE)"이용 중"else "종료"}",32f,y+16f,paint);y+=38f};doc.finishPage(page)}
        try{doc.writeTo(stream)}finally{doc.close()}
    }
}
