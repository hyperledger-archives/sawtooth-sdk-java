package bitwiseio.sawtooth.xo.state.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class StateResponse(
    @field:SerializedName("data")
    @Expose
    val data: List<Entry>,

    @field:SerializedName("link")
    @Expose
    /**
     * Returns the link to query the batch status
     *
     * @return String link
     */
    val link: String,
    @field:SerializedName("head")
    @Expose
    /**
     * Returns the id of the block head the state data corresponds too
     *
     * @return String head
     */
    val head: String,
    @field:SerializedName("paging")
    @Expose
    val paging: Paging
) {
    override fun toString(): String {
        return "StateResponse(data=$data, link='$link', head='$head', paging=$paging)"
    }
}

class Entry(
    @field:SerializedName("address")
    @Expose
    /** Address of game
     *
     * @return String address
     */
    val address: String,

    @field:SerializedName("data")
    @Expose
    /**
     * Serialized current state data of game
     *
     * @return String data
     */
    val data: String
) {
    override fun toString(): String {
        return "Entry(address='$address', data='$data')"
    }
}

class Paging(
    @field:SerializedName("start")
    @Expose
    /**
     * @return String start
     */
    var start: String? = null,

    @field:SerializedName("limit")
    @Expose
    /**
     * The maximum number of resources per page
     *
     * @return String limit
     */
    var limit: String? = null,

    @field:SerializedName("next_position")
    @Expose
    /**
     * @return String next_position
     */
    var nextPosition: String? = null,

    @field:SerializedName("next")
    @Expose
    /**
     * Link to the next page
     *
     * @return String next
     */
    var next: String? = null
) {
    override fun toString(): String {
        return "Paging(start=$start, limit=$limit, nextPosition=$nextPosition, next=$next)"
    }
}