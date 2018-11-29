package io.bitwise.sawtooth_xo.state

import com.google.common.io.BaseEncoding
import java.security.MessageDigest

fun transactionFamilyPrefix() : String{
    return hash("xo").substring(0,6)
}

fun hash(input: String) : String{
    val digest = MessageDigest.getInstance("SHA-512")
    digest.reset()
    digest.update(input.toByteArray())
    return BaseEncoding.base16().lowerCase().encode(digest.digest())
}

fun makeGameAddress(gameName: String) : String {
    val xoPrefix = transactionFamilyPrefix()
    val gameAddress = hash(gameName).substring(0, 64)
    return xoPrefix + gameAddress
}
