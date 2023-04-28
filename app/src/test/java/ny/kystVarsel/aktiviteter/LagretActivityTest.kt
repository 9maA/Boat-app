package ny.kystVarsel.aktiviteter

import com.ny.kystVarsel.aktiviteter.LagretActivity
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test

import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@Suppress("DEPRECATION")
class LagretActivityTest {

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }
    @Test
    fun sjekk_Om_IngenLagrede(){
        val lagret : LagretActivity =  Mockito.mock(LagretActivity::class.java)
        Mockito.`when`(lagret.sjekkIngenLagrede(0)).thenReturn(true)
        TestCase.assertEquals(true,lagret.sjekkIngenLagrede(0))
    }

    @Test
    fun sjekk_Fjern_Sted(){
        val lagret : LagretActivity =  Mockito.mock(LagretActivity::class.java)
        Mockito.`when`(lagret.fjernSted("Sted 1")).thenReturn(true)
        TestCase.assertEquals(true,lagret.fjernSted("Sted 1"))
    }
}
