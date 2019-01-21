package bitwiseio.sawtooth.xo.state.api

import com.google.gson.annotations.SerializedName

/**
 * Data class for interacting with the Sawtooth REST Api
 */
class BatchListResponse(
    @field:SerializedName("link")
    /** Returns the link to query the batch status
     *
     * @return String link
     */
    val link: String

) {
    override fun toString(): String {
        return "BatchListResponse(link='$link')"
    }
}
