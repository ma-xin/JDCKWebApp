//package com.mxin.jdweb.db.dao
//
//import androidx.room.*
//import com.mxin.jdweb.db.model.WebCookieModel
//
//@Dao
//interface WebCookieDao {
//
//    @Insert
//    fun insert(cookie : List<WebCookieModel>)
//
////    @Update
////    fun update(vararg cookie: WebCookieModel)
//
//    @Query("delete from web_cookie where remark=:remark")
//    fun delete(remark: String)
//
//    @Query("select * from web_cookie where remark=:remark")
//    suspend fun get(remark: String) : List<WebCookieModel>
//
//    @Query("select remark from web_cookie group by remark")
//    suspend fun getRemarkList() : List<String>
//
//
//
//}