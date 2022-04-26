package com.mxin.jdweb.network.data

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import java.sql.Timestamp

//{"id":1,"value":"456456","timestamp":"Tue Apr 19 2022 08:55:06 GMT+0800 (中国标准时间)","status":0,
// "position":4999999999.5,"name":"jd_cookie","remarks":"1","createdAt":"2022-04-19T00:55:06.131Z",
// "updatedAt":"2022-04-19T01:02:23.132Z"}

data class EnvsData(

    private var id:Long?,
    var value:String?,
    val timestamp: String?,
    var status:Int,
    val position:Float,
    var name:String?,
    var remarks:String?,
    val createdAt:String?,
    val updatedAt:String?

) : Parcelable {
    private var showTime :String? = ""

    var _id:String?=null

    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
        showTime = parcel.readString()
        _id = parcel.readString()
    }

    fun getEId():Any?{
        return id ?: _id
    }

    fun setEid(data:EnvsData){
        this.id = data.id
        this._id = data._id
    }

    fun getTime(): String {
        if(showTime.isNullOrEmpty()){
            val t = if(TextUtils.isEmpty(updatedAt)) createdAt else updatedAt
            showTime = if(t?.length?:0>19) t!!.substring(0,19).replace("T"," ") else ""
        }
        return showTime!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(value)
        parcel.writeString(timestamp)
        parcel.writeInt(status)
        parcel.writeFloat(position)
        parcel.writeString(name)
        parcel.writeString(remarks)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
        parcel.writeString(showTime)
        parcel.writeString(_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EnvsData> {
        override fun createFromParcel(parcel: Parcel): EnvsData {
            return EnvsData(parcel)
        }

        override fun newArray(size: Int): Array<EnvsData?> {
            return arrayOfNulls(size)
        }
    }


}
