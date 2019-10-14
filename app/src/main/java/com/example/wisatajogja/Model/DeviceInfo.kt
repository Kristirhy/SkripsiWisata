package com.example.wisatajogja.Model

import java.io.Serializable

class DeviceInfo : Serializable {
    private var device: String? = null
    private var email:String? = null
    private var version:String? = null
    private var regid:String? = null
    private var date_create: Long = 0

    fun DeviceInfo(){
    }

    fun DeviceInfo(
        device: String,
        email: String,
        version: String,
        regid: String,
        date_create: Long
    )
    {
        this.device = device
        this.email = email
        this.version = version
        this.regid = regid
        this.date_create = date_create
    }

    fun getDevice(): String {
        return device!!
    }

    fun setDevice(device: String) {
        this.device = device
    }

    fun getEmail(): String {
        return email!!
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun getVersion(): String {
        return version!!
    }

    fun setVersion(version: String) {
        this.version = version
    }

    fun getRegid(): String {
        return regid!!
    }

    fun setRegid(regid: String) {
        this.regid = regid
    }

    fun getDate_create(): Long {
        return date_create
    }

    fun setDate_create(date_create: Long) {
        this.date_create = date_create
    }
}
