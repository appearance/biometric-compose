package com.gucodero.biometric_compose


/**
 * Created by Mazhar on 17/07/23
 */
object Extensions {

    val String.toPreservedByteArray: ByteArray
        get() {
            return this.toByteArray(Charsets.ISO_8859_1)
        }

    val ByteArray.toPreservedString: String
        get() {
            return String(this, Charsets.ISO_8859_1)
        }
}