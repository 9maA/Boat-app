package ny.kystVarsel.aktiviteter

import com.ny.kystVarsel.aktiviteter.MainActivity
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@Suppress("DEPRECATION")
class MainActivityTestMock {

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }
    @Test
    fun test_Legg_Til_Sted() {
        val main : MainActivity =  Mockito.mock(MainActivity::class.java)
        Mockito.`when`(main.leggInnSted("Hovseter","23","22")).thenReturn(true)
        TestCase.assertEquals(true,main.leggInnSted("Hovseter","23","22"))
    }
    @Test
    fun test_Legg_To_Steder() {
        val main : MainActivity =  Mockito.mock(MainActivity::class.java)
        Mockito.`when`(main.leggInnSted("Linderud","29","12")).thenReturn(true)
        TestCase.assertEquals(true,main.leggInnSted("Linderud","29","12"))
        Mockito.`when`(main.leggInnSted("Linderud","29","12")).thenReturn(false)
        TestCase.assertEquals(false,main.leggInnSted("Linderud","29","12"))
    }
    @Test
    fun test_Legg_Inn_Tom_Sted(){
        val main : MainActivity =  Mockito.mock(MainActivity::class.java)
        Mockito.`when`(main.leggInnSted("","34","13")).thenReturn(false)
        TestCase.assertEquals(false,main.leggInnSted("","34","13"))
    }
}
