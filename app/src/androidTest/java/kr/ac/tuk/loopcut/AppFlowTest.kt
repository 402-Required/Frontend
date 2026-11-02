package kr.ac.tuk.loopcut

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Bitmap
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AppFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun capture(name:String){compose.waitForIdle();Thread.sleep(650);val instrumentation=InstrumentationRegistry.getInstrumentation();val image=instrumentation.uiAutomation.takeScreenshot();val dir=File(compose.activity.filesDir,"screenshots");dir.mkdirs();File(dir,"$name.png").outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)};image.recycle()}
    private fun enter(){compose.onNodeWithText("데모 둘러보기").performScrollTo().performClick();compose.onNodeWithText("전체 6개").assertExists();compose.mainClock.advanceTimeBy(6000);compose.waitForIdle()}
    @Test fun browseDesignScreensAndFilter(){
        capture("01-login");enter();capture("02-subscriptions")
        compose.onNodeWithText("서비스 이름으로 검색").performTextInput("Canva")
        compose.onNode(hasText("Canva") and !hasSetTextAction(),useUnmergedTree=true).assertExists()
        compose.onNodeWithText("Netflix",useUnmergedTree=true).assertDoesNotExist()
        compose.onNode(hasText("Canva") and !hasSetTextAction(),useUnmergedTree=true).performClick();capture("03-detail")
        compose.onNodeWithText("구독 수정").performClick();capture("04-edit")
        compose.onNodeWithText("생산성").performClick();capture("05-category")
        compose.onNodeWithText("선택 완료").performClick()
        compose.onNodeWithText("수정하기").performClick()
        compose.onNodeWithText("지난 이력").performClick();capture("06-history")
    }
    @Test fun settingsAndExportsRender(){
        enter();compose.onNodeWithText("마이").performClick();capture("07-my")
        compose.onNodeWithText("결제 알림").performClick();capture("08-notifications")
        compose.onNodeWithText("저장하기").performClick();compose.activityRule.scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
        compose.onNodeWithText("결제수단").performClick();capture("09-payments")
        compose.activityRule.scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
        compose.onNodeWithText("데이터 내보내기").performClick();capture("10-export")
        compose.onNodeWithText("PDF").performClick();compose.onNodeWithText("파일로 저장하기").assertIsEnabled()
        compose.activityRule.scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
        compose.onNodeWithText("도움말").performClick();capture("11-help")
    }
    @Test fun signupAndNewSubscriptionSearchRender(){
        compose.onNodeWithText("회원가입").performScrollTo().performClick();capture("12-signup")
        compose.activityRule.scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
        enter();compose.onNodeWithContentDescription("구독 추가").performClick();capture("13-add-empty")
        compose.onNodeWithText("등록하기").assertIsNotEnabled()
        compose.onNodeWithText("어떤 서비스를 등록할까요?").performClick();capture("14-search-empty")
        compose.onNodeWithText("서비스 이름을 검색하세요").performTextInput("넷플")
        compose.onNodeWithText("Netflix").assertExists()
        compose.activityRule.scenario.onActivity{it.getSystemService(android.view.inputmethod.InputMethodManager::class.java).hideSoftInputFromWindow(it.window.decorView.windowToken,0)}
        capture("15-search-results")
        compose.onNodeWithText("Netflix").performClick()
        compose.onNodeWithText("17000").assertExists()
    }
}
