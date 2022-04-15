package com.mxin.jdweb

import org.junit.Test

import org.junit.Assert.*
import java.net.URLEncoder

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        //%E9%A9%AC%E6%96%B09630;
        println("%E9%A9%AC%E6%96%B09630")
        println(URLEncoder.encode("马新9630"))
        assertEquals(4, 2 + 2)
    }
}