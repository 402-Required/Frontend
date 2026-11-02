package kr.ac.tuk.loopcut

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.caverock.androidsvg.SVG

val Bg = Color(0xFFF6F8F8)
val Ink = Color(0xFF182E2A)
val Primary = Color(0xFF00796B)
val Deep = Color(0xFF0A5C53)
val Soft = Color(0xFFE1F3EC)
val Muted = Color(0xFF5F716B)
val Line = Color(0xFFDFE8E3)
val Danger = Color(0xFFB91C1C)
val Success = Color(0xFF166534)
@OptIn(ExperimentalTextApi::class)
val Noto = FontFamily(listOf(400,500,700).map { weight -> Font(R.font.noto_sans_kr, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight))) })
@Composable fun LoopCutTheme(content: @Composable () -> Unit) {
    val base = Typography()
    MaterialTheme(colorScheme = lightColorScheme(primary=Primary,onPrimary=Color.White,background=Bg,onBackground=Ink,surface=Color.White,onSurface=Ink,outline=Line), typography = Typography(
        bodyLarge=base.bodyLarge.copy(fontFamily=Noto,fontSize=16.sp,lineHeight=24.sp),
        bodyMedium=base.bodyMedium.copy(fontFamily=Noto,fontSize=14.sp,lineHeight=22.sp),
        bodySmall=base.bodySmall.copy(fontFamily=Noto,fontSize=12.sp,lineHeight=18.sp),
        titleLarge=base.titleLarge.copy(fontFamily=Noto,fontSize=26.sp,lineHeight=36.sp,fontWeight=FontWeight.Bold),
        titleMedium=base.titleMedium.copy(fontFamily=Noto,fontSize=20.sp,lineHeight=28.sp,fontWeight=FontWeight.Bold),
        titleSmall=base.titleSmall.copy(fontFamily=Noto,fontSize=16.sp,lineHeight=24.sp,fontWeight=FontWeight.Bold),
        labelLarge=base.labelLarge.copy(fontFamily=Noto),labelMedium=base.labelMedium.copy(fontFamily=Noto),labelSmall=base.labelSmall.copy(fontFamily=Noto)
    ),content=content)
}
private val assetMap = mapOf("back" to "ce80d", "search" to "ec4b7", "searchPrimary" to "c7854", "plus" to "57415", "home" to "1008c", "grid" to "294f4", "gridActive" to "d8795", "chart" to "fc3dc", "user" to "f0016", "userActive" to "36557", "history" to "05d9b", "loop" to "847e8", "calendar" to "2081d", "card" to "2ca88", "bell" to "a7e3c", "down" to "befed", "check" to "fc49a", "help" to "04885", "download" to "a3f99", "cardMuted" to "719ef", "bellMuted" to "39add", "next" to "98282", "shield" to "d0fb8", "message" to "6ace4", "mail" to "d5bcb", "close" to "978ab", "addService" to "a87ce")
/** Original Figma SVG at its intrinsic dp dimensions, without path conversion or recoloring. */
@Composable fun FigmaIcon(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val svg = remember(name) { SVG.getFromAsset(context.assets, "figma/${assetMap[name] ?: name}.svg") }
    val picture = remember(svg) { svg.renderToPicture() }
    Canvas(modifier.size(svg.documentWidth.dp,svg.documentHeight.dp)) {
        val canvas = drawContext.canvas.nativeCanvas
        canvas.save(); canvas.scale(density,density); canvas.drawPicture(picture); canvas.restore()
    }
}
@Composable fun Title(text: String, modifier: Modifier=Modifier) { Text(text,modifier,style=MaterialTheme.typography.titleMedium) }
@Composable fun SectionTitle(text: String, modifier: Modifier=Modifier) { Text(text,modifier,style=MaterialTheme.typography.titleSmall) }
@Composable fun Small(text: String, modifier: Modifier=Modifier, color: Color=Muted) { Text(text,modifier,color=color,style=MaterialTheme.typography.bodySmall) }
@Composable fun Link(text:String, modifier:Modifier=Modifier, color:Color=Primary, onClick:()->Unit) { TextButton(onClick,modifier,contentPadding=PaddingValues(0.dp),colors=ButtonDefaults.textButtonColors(contentColor=color)) { Text(text,fontFamily=Noto,fontSize=12.sp) } }
@Composable fun Header(title:String, back:(()->Unit)?=null, actions:@Composable RowScope.()->Unit={}) {
    Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically) {
        if(back!=null) { IconButton(back,Modifier.size(40.dp).offset(x=(-6).dp)) { FigmaIcon("back") }; Spacer(Modifier.width(20.dp)) }
        Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,maxLines=1)
        actions()
    }
}
@Composable fun Panel(modifier:Modifier=Modifier, padding:Dp=16.dp, verticalPadding:Dp=padding, color:Color=Color.White, content:@Composable ColumnScope.()->Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(color).padding(horizontal=padding,vertical=verticalPadding),content=content)
}
@Composable fun PrimaryButton(text:String, modifier:Modifier=Modifier, enabled:Boolean=true, compact:Boolean=false, onClick:()->Unit) {
    Button(onClick,modifier.then(if(compact) Modifier.width(120.dp) else Modifier.fillMaxWidth()).height(56.dp),enabled=enabled,shape=RoundedCornerShape(28.dp),colors=ButtonDefaults.buttonColors(containerColor=Primary,disabledContainerColor=Line,disabledContentColor=Muted)) { Text(text,fontFamily=Noto,fontWeight=if(compact) FontWeight.Medium else FontWeight.Bold,fontSize=if(compact)14.sp else 16.sp) }
}
@Composable fun BottomButton(text:String, enabled:Boolean=true, compact:Boolean=false, onClick:()->Unit) {
    Box(Modifier.fillMaxWidth().background(Bg).padding(horizontal=20.dp,vertical=12.dp),contentAlignment=Alignment.Center) { PrimaryButton(text,enabled=enabled,compact=compact,onClick=onClick) }
}
@Composable fun PageColumn(modifier:Modifier=Modifier, top:Dp=28.dp, content:@Composable ColumnScope.()->Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=20.dp,end=20.dp,top=top,bottom=24.dp),content=content)
}
@Composable fun LabeledField(label:String,value:String,onValue:(String)->Unit,placeholder:String="",keyboard:KeyboardType=KeyboardType.Text,password:Boolean=false,error:Boolean=false,modifier:Modifier=Modifier) {
    Column(modifier) { Small(label); Spacer(Modifier.height(8.dp)); OutlinedTextField(value,onValue,Modifier.fillMaxWidth(),placeholder={Text(placeholder,color=Muted,fontSize=16.sp)},singleLine=true,shape=RoundedCornerShape(12.dp),textStyle=TextStyle(fontFamily=Noto,fontSize=16.sp,lineHeight=24.sp),keyboardOptions=KeyboardOptions(keyboardType=keyboard),visualTransformation=if(password)PasswordVisualTransformation() else VisualTransformation.None,isError=error,colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=Line,focusedBorderColor=Primary,unfocusedContainerColor=Color.White,focusedContainerColor=Color.White)); Spacer(Modifier.height(12.dp)) }
}
@Composable fun SearchField(value:String,onValue:(String)->Unit,placeholder:String,outlined:Boolean=false,modifier:Modifier=Modifier) {
    OutlinedTextField(value,onValue,modifier.fillMaxWidth().heightIn(min=48.dp),placeholder={Text(placeholder,color=Muted,fontSize=14.sp)},leadingIcon={FigmaIcon(if(outlined)"searchPrimary" else "search")},trailingIcon=if(value.isNotEmpty())({IconButton({onValue("")}){FigmaIcon("close")}})else null,singleLine=true,shape=RoundedCornerShape(if(outlined)12.dp else 16.dp),textStyle=TextStyle(fontFamily=Noto,fontSize=16.sp),colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=if(outlined)Primary else Color.Transparent,focusedBorderColor=Primary,unfocusedContainerColor=Color.White,focusedContainerColor=Color.White))
}
@Composable fun Chip(text:String,selected:Boolean=false,white:Boolean=false,modifier:Modifier=Modifier,onClick:()->Unit) {
    Box(modifier.clip(RoundedCornerShape(22.dp)).background(if(selected)Primary else if(white)Color.White else Soft).clickable(onClick=onClick).padding(horizontal=16.dp,vertical=9.dp),contentAlignment=Alignment.Center) { Text(text,color=if(selected)Color.White else if(white)Muted else Ink,fontSize=14.sp,lineHeight=22.sp) }
}
@Composable fun Toggle(checked:Boolean,label:String,enabled:Boolean=true,onChange:(Boolean)->Unit) { Switch(checked,onChange,Modifier.height(28.dp).semantics { contentDescription=label },enabled=enabled,colors=SwitchDefaults.colors(checkedTrackColor=Primary,checkedThumbColor=Color.White,uncheckedTrackColor=Line,uncheckedThumbColor=Color.White,uncheckedBorderColor=Line)) }
@Composable fun Mark(name:String,mark:String,large:Boolean=false) {
    val bg=when(name){"Netflix"->Color(0xFF202723);"YouTube Premium"->Color(0xFFFEF3C7);"Google One","Notion"->Bg;else->Soft}
    val fg=when(name){"Netflix"->Color.White;"YouTube Premium"->Danger;"Google One"->Color(0xFF4C8CCB);"Notion"->Ink;else->Primary}
    Box(Modifier.size(if(large)64.dp else 44.dp).clip(RoundedCornerShape(if(large)20.dp else 12.dp)).background(bg),contentAlignment=Alignment.Center) { Text(mark,color=fg,fontSize=if(large)28.sp else 20.sp,fontWeight=FontWeight.Bold) }
}
@Composable fun Badge(text:String,review:Boolean=false,modifier:Modifier=Modifier) { Box(modifier.clip(RoundedCornerShape(12.dp)).background(if(review)Color(0xFFFEE2E2)else Color(0xFFDCFCE7)).padding(horizontal=8.dp,vertical=2.dp),contentAlignment=Alignment.Center) { Text(text,color=if(review)Danger else Success,fontSize=11.sp,lineHeight=18.sp,maxLines=1) } }
@Composable fun SubscriptionRow(s:Subscription,onClick:()->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).clickable(onClick=onClick).padding(12.dp).heightIn(min=52.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Mark(s.name,s.mark)
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text(s.name,fontSize=16.sp,lineHeight=24.sp,fontWeight=FontWeight.Medium,maxLines=1,overflow=TextOverflow.Ellipsis)
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) { Small(s.category); Badge(if(s.review)"미사용 확인 필요" else "이용 중",s.review,Modifier.width(106.dp)) }
        }
        Column(horizontalAlignment=Alignment.End) {
            Text(s.amount.won()+if(s.cycle==BillingCycle.MONTHLY)"/월" else "/년",fontSize=16.sp,lineHeight=24.sp,fontWeight=FontWeight.Bold)
            val next=s.nextPayment(DemoData.today);val days=s.daysUntil(DemoData.today)
            Small("%02d.%02d · %s".format(next.monthValue,next.dayOfMonth,if(days==0L)"오늘" else if(days<=2)"D−$days" else "${days}일 뒤"),color=if(days<=2)Color(0xFF92400E)else Muted)
        }
    }
}
@Composable fun EmptyState(text:String){Panel { Text(text,Modifier.fillMaxWidth().padding(vertical=28.dp),color=Muted) }}


