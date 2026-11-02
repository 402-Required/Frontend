package kr.ac.tuk.loopcut

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class SubscriptionTest {
    @Test fun endOfMonthDoesNotDrift() {
        val s=Subscription(firstDate=LocalDate.of(2026,1,31))
        assertEquals(LocalDate.of(2026,2,28),s.nextPayment(LocalDate.of(2026,2,1)))
        assertEquals(LocalDate.of(2026,3,31),s.nextPayment(LocalDate.of(2026,3,1)))
    }
    @Test fun leapDayAnnualBillingClampsCorrectly() {
        val s=Subscription(cycle=BillingCycle.YEARLY,firstDate=LocalDate.of(2024,2,29))
        assertEquals(LocalDate.of(2025,2,28),s.nextPayment(LocalDate.of(2025,1,1)))
        assertEquals(LocalDate.of(2028,2,29),s.nextPayment(LocalDate.of(2027,3,1)))
    }
    @Test fun futureFirstPaymentIsNotBroughtForward() {
        val s=Subscription(firstDate=LocalDate.of(2027,4,12))
        assertEquals(s.firstDate,s.nextPayment(DemoData.today))
    }
    @Test fun currentDueDateIsToday() { val s=Subscription(firstDate=LocalDate.of(2026,1,3));assertEquals(0L,s.daysUntil(DemoData.today)) }
    @Test fun yearlySubscriptionsAreNormalizedAndArchivedExcluded(){
        val state=AppState(subscriptions=listOf(Subscription(amount=120000,cycle=BillingCycle.YEARLY),Subscription(amount=9999,status=SubscriptionStatus.CANCELLED)))
        assertEquals(10000L,state.total)
    }
    @Test fun csvEscapesUserInputAndFormulaPrefixes(){
        val csv=ExportWriter.csv(listOf(Subscription(name="=HYPERLINK(\"x\")")))
        assertTrue(csv.startsWith("\uFEFF"));assertTrue(csv.contains("\"'=HYPERLINK(\"\"x\"\")\""))
    }
    @Test fun xlsxContainsTypedNumbersAndEscapedStrings(){
        val out=ByteArrayOutputStream();ExportWriter.xlsx(listOf(Subscription(name="A&B <test>",amount=13800)),out)
        val zip=ZipInputStream(out.toByteArray().inputStream());val files=mutableMapOf<String,String>()
        while(true){val entry=zip.nextEntry?:break;files[entry.name]=zip.readBytes().toString(Charsets.UTF_8)}
        assertTrue(files.keys.containsAll(listOf("[Content_Types].xml","xl/workbook.xml","xl/worksheets/sheet1.xml")))
        assertTrue(files["xl/worksheets/sheet1.xml"]!!.contains("A&amp;B &lt;test&gt;"));assertTrue(files["xl/worksheets/sheet1.xml"]!!.contains("<v>13800</v>"))
    }
}
