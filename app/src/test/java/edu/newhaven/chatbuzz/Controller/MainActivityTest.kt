package edu.newhaven.chatbuzz.Controller

import org.junit.Assert.*
import android.content.Context
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class MainActivityTest {
    @Test
    fun mockemailAndPassword() {
        val c = Mockito.mock(MainActivity::class.java)
        var result = c.emailAndPassword("Rahul@gmail.com","qwerryuiop")

        assertEquals("Rahul@gmail.com" , "qwertyuiop", result)
    }
}
