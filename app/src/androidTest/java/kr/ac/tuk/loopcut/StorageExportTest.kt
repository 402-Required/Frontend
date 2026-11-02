package kr.ac.tuk.loopcut

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class StorageExportTest {
    @Test fun saveAndReloadPreservesSubscriptionsAndSettings()= runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val repository=LocalRepository(context)
        val original=repository.load()
        try {
            val added=Subscription(name="테스트 구독",amount=12345,firstDate=DemoData.today,category="교육",paymentId="shinhan")
            val expected=original.copy(subscriptions=original.subscriptions+added,settings=original.settings.copy(budget=95000,days=setOf(7,0)))
            repository.save(expected)
            assertEquals(expected,LocalRepository(context).load())
            val archived=expected.copy(subscriptions=expected.subscriptions.map{if(it.id==added.id)it.copy(status=SubscriptionStatus.CANCELLED,endedAt=DemoData.today)else it})
            repository.save(archived)
            assertTrue(LocalRepository(context).load().history.any{it.id==added.id})
        } finally { repository.save(original) }
    }
    @Test fun nativePdfAndExcelHaveValidFileSignatures() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val pdf=ByteArrayOutputStream();ExportWriter.write(context,DemoData.subscriptions,"PDF",pdf)
        assertTrue(pdf.toByteArray().take(5).toByteArray().toString(Charsets.US_ASCII).startsWith("%PDF-"))
        val excel=ByteArrayOutputStream();ExportWriter.write(context,DemoData.subscriptions,"Excel",excel)
        assertEquals('P'.code,excel.toByteArray()[0].toInt());assertEquals('K'.code,excel.toByteArray()[1].toInt())
        assertTrue(pdf.size()>1000);assertTrue(excel.size()>1000)
    }
}
