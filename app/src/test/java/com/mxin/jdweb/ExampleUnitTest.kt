package com.mxin.jdweb

import com.mxin.jdweb.ui.ql.compareQLVersion
import org.junit.Test

import org.junit.Assert.*
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

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

    @Test
    fun testTime(){
        //13294750017301406
        //13297342017301406
        println(utcToLocal(13294750017301406))
        println(utcToLocal(13297342017301406))

        //1650772776169
        //1621698390.7632656
//        println(System.currentTimeMillis())
        println(getLocalSDF().format(Date().apply { time =  1621698390763}))
    }


    fun getUtcDataSDF(): SimpleDateFormat {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf
    }

    fun getLocalSDF(): SimpleDateFormat {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return sdf
    }

//    //普通时间转为UTC
//    public static String localToUTC(String localTimeStr) {
//        try {
//            Date localDate = getLocalSDF().parse(localTimeStr);
//            return getUTCSDF().format(localDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }



    //UTC转为普通时间
    fun utcToLocal(utcTimeStr:String):String{
        try{
            val date = getUtcDataSDF().parse(utcTimeStr)
            return getLocalSDF().format(date)
        }catch (e:Exception){
            e.printStackTrace()
        }
        return ""
    }

    fun utcToLocal(utcTimes:Long):String{
        try{
            val date = Date().apply { time = ((utcTimes /1000000f  - 11644473600)*1000).toLong() }
            return getLocalSDF().format(date)
        }catch (e:Exception){
            e.printStackTrace()
        }
        return ""
    }

    @Test
    fun textQlVersionCompase(){
        val v1 = "2.10.1"
        val v2 = "2.10.1"
        println(v1.compareQLVersion(v2))
    }

}