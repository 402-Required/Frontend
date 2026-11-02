package kr.ac.tuk.loopcut

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable fun LoginScreen(onLogin:()->Unit,onSignup:()->Unit,notice:(String)->Unit) {
    var email by rememberSaveable{mutableStateOf("")};var password by remember{mutableStateOf("")};var keep by rememberSaveable{mutableStateOf(true)};var error by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Spacer(Modifier.height(112.dp));Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(Deep),contentAlignment=Alignment.Center){FigmaIcon("loop")};Spacer(Modifier.height(8.dp));Text("LoopCut",fontSize=34.sp,lineHeight=44.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text("결제 전에 확인하는 구독 관리",color=Muted,fontSize=16.sp);Spacer(Modifier.height(32.dp))
        LabeledField("이메일",email,{email=it},"이메일 주소를 입력하세요",KeyboardType.Email)
        LabeledField("비밀번호",password,{password=it},"비밀번호를 입력하세요",KeyboardType.Password,password=true)
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Row(verticalAlignment=Alignment.CenterVertically){Checkbox(keep,{keep=it},Modifier.size(20.dp),colors=CheckboxDefaults.colors(checkedColor=Primary));Spacer(Modifier.width(8.dp));Text("로그인 상태 유지",fontSize=14.sp)};Link("비밀번호 찾기",color=Muted){notice("백엔드 연결 후 이메일로 비밀번호를 재설정할 수 있어요. 지금은 데모 둘러보기를 이용해주세요.")}}
        if(error.isNotEmpty())Small(error,color=Danger)
        Spacer(Modifier.height(20.dp));PrimaryButton("로그인"){if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()||password.isBlank())error="이메일과 비밀번호를 확인해주세요." else onLogin()}
        Spacer(Modifier.height(32.dp));Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){HorizontalDivider(Modifier.weight(1f),color=Line);Small("또는 간편 로그인");HorizontalDivider(Modifier.weight(1f),color=Line)};Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(20.dp)){listOf("K","A","G").forEachIndexed{i,label->Surface(onClick={notice("소셜 로그인은 백엔드 연결 후 사용할 수 있어요.")},modifier=Modifier.size(56.dp),shape=CircleShape,color=listOf(Color(0xFFFEE500),Ink,Color.White)[i],border=if(i==2)androidx.compose.foundation.BorderStroke(1.dp,Line)else null){Box(contentAlignment=Alignment.Center){Text(label,fontWeight=FontWeight.Bold,color=if(i==1)Color.White else Ink)}}}}
        Spacer(Modifier.height(24.dp));Link("데모 둘러보기",onClick=onLogin)
        Spacer(Modifier.height(24.dp));Row(verticalAlignment=Alignment.CenterVertically){Small("아직 계정이 없나요?");Spacer(Modifier.width(6.dp));Link("회원가입",onClick=onSignup)};Spacer(Modifier.height(20.dp))
    }
}
@Composable fun SignupScreen(onComplete:(String)->Unit,notice:(String)->Unit) {
    var name by rememberSaveable{mutableStateOf("길동")};var email by rememberSaveable{mutableStateOf("gildong@loopcut.app")};var password by remember{mutableStateOf("")};var confirm by remember{mutableStateOf("")};var terms by rememberSaveable{mutableStateOf(true)};var privacy by rememberSaveable{mutableStateOf(true)};var marketing by rememberSaveable{mutableStateOf(false)};var error by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize()) {
        PageColumn(Modifier.weight(1f),top=0.dp){
            Text("필요한 정보만 입력하면 바로 시작할 수 있어요.",color=Muted);Spacer(Modifier.height(20.dp));LabeledField("이름",name,{name=it.take(30)});LabeledField("이메일",email,{email=it},keyboard=KeyboardType.Email);Small("데모에서는 이메일 중복 확인을 진행하지 않아요.");Spacer(Modifier.height(12.dp));LabeledField("비밀번호",password,{password=it},"비밀번호를 입력하세요",KeyboardType.Password,true);Small("영문, 숫자를 포함해 8자 이상 입력해주세요");Spacer(Modifier.height(12.dp));LabeledField("비밀번호 확인",confirm,{confirm=it},"비밀번호를 한 번 더 입력하세요",KeyboardType.Password,true)
            Panel { CheckRow("전체 동의하기",terms&&privacy&&marketing,{terms=it;privacy=it;marketing=it});HorizontalDivider(color=Line);CheckRow("[필수] 서비스 이용약관",terms,{terms=it}){notice("정식 서비스 이용약관은 준비 중이에요. 데모는 실제 계정을 생성하지 않아요.")};CheckRow("[필수] 개인정보 수집 및 이용",privacy,{privacy=it}){notice("구독 정보는 이 기기 안에만 저장돼요. 비밀번호는 저장하지 않아요. 정식 개인정보 처리방침은 서비스 출시 전에 확정해야 해요.")};CheckRow("[선택] 결제 알림 · 혜택 소식 받기",marketing,{marketing=it}){notice("데모에서는 푸시·이메일·혜택 알림을 발송하지 않아요.")} }
            if(error.isNotEmpty()){Spacer(Modifier.height(8.dp));Small(error,color=Danger)}
        }
        BottomButton("가입하기") {
            error=when{ name.isBlank()->"이름을 입력해주세요.";!Patterns.EMAIL_ADDRESS.matcher(email).matches()->"올바른 이메일을 입력해주세요.";!Regex("^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$").matches(password)->"비밀번호는 영문과 숫자를 포함해 8자 이상이어야 해요.";password!=confirm->"비밀번호가 일치하지 않아요.";!terms||!privacy->"필수 약관에 동의해주세요.";else->"" }
            if(error.isEmpty())onComplete(name.trim())
        }
    }
}
@Composable fun CheckRow(label:String,checked:Boolean,onChange:(Boolean)->Unit,onView:(()->Unit)?=null) {
    Row(Modifier.fillMaxWidth().heightIn(min=40.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) { Checkbox(checked,onChange,Modifier.size(20.dp),colors=CheckboxDefaults.colors(checkedColor=Primary));Text(label,Modifier.weight(1f).clickable{onChange(!checked)},fontSize=14.sp);if(onView!=null)Link("보기",color=Muted,onClick=onView) }
}

