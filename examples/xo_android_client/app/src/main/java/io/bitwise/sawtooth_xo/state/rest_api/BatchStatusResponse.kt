package io.bitwise.sawtooth_xo.state.rest_api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BatchStatusResponse(
    @field:SerializedName("data")
    @Expose
    val data: List<Datum>,

    @field:SerializedName("link")
    @Expose
    /**
     * Returns the link to query the batch status
     *
     * @return String link
     */
    val link: String

) {
    override fun toString(): String {
        return "BatchStatusResponse(data=$data, link='$link')"
    }
}

class Datum(
    @field:SerializedName("id")
    @Expose
    /** Batch ID
     *
     * @return String bach id
     */
    val id: String,

    @field:SerializedName("status")
    @Expose
    /**
     * Status of the batch. Possible values are 'COMMITTED', 'INVALID', 'PENDING', and 'UNKNOWN'.
     *
     * @return String status
     */
    val status: String,

    @field:SerializedName("invalid_transactions")
    @Expose
    /**
     * List of InvalidTransactions if there's any
     *
     * @return List InvalidTransactions
     */
    val invalidTransactions: List<InvalidTransaction>


    ){
    override fun toString(): String {
        return "Datum(id='$id', status='$status', invalidTransactions=$invalidTransactions)"
    }
}


class InvalidTransaction(
    @field:SerializedName("id")
    @Expose
    /**
     * Id of the transaction
     *
     * @return String id
     */
    var id: String? = null,

    @field:SerializedName("message")
    @Expose
    /**
     * Message that explains why the transaction failed
     *
     * @return String message
     */
    var message: String? = null,

    @field:SerializedName("extended_data")
    @Expose
    /**
     * @return String extendedData
     */
    var extendedData: String? = null
) {
    override fun toString(): String {
        return "InvalidTransaction(id=$id, message=$message, extendedData=$extendedData)"
    }
}



