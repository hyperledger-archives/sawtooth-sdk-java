package io.bitwise.sawtooth_xo.state


import io.bitwise.sawtooth_xo.state.rest_api.SawtoothRestApi
import retrofit2.Retrofit
import io.bitwise.sawtooth_xo.state.rest_api.BatchListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import com.google.common.io.BaseEncoding
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.converter.gson.GsonConverterFactory
import sawtooth.sdk.signing.Secp256k1Context
import sawtooth.sdk.signing.Signer
import com.google.protobuf.ByteString
import java.security.MessageDigest
import sawtooth.sdk.protobuf.*
import java.util.UUID



class XoState {
    private var service: SawtoothRestApi? = null
    private var signer: Signer? = null

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:9708")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create<SawtoothRestApi>(SawtoothRestApi::class.java)

        val context = Secp256k1Context()
        val privateKey = context.newRandomPrivateKey()
        signer = Signer(context, privateKey)
    }

    fun createGame(gameName: String) {
        val createGameTransaction = makeTransaction(gameName, "create", null)
        val batch = makeBatch(arrayOf(createGameTransaction))
        sendRequest(batch)
    }

    private fun makeTransaction(gameName: String, action: String, space: String?): Transaction {
        val payload ="$gameName,$action,$space"

        val address = makeGameAddress(gameName)

        val header = TransactionHeader.newBuilder()
            .setSignerPublicKey(signer?.publicKey?.hex())
            .setFamilyName("xo")
            .setFamilyVersion("1.0")
            .addInputs(address)
            .addOutputs(address)
            .setPayloadSha512(hash(payload))
            .setBatcherPublicKey(signer?.publicKey?.hex())
            .setNonce(UUID.randomUUID().toString())
            .build()

        val signature = signer?.sign(header.toByteArray())

        return Transaction.newBuilder()
            .setHeader(header.toByteString())
            .setPayload(ByteString.copyFrom(payload, "UTF-8"))
            .setHeaderSignature(signature)
            .build()
    }

    private fun makeBatch(transactions: Array<Transaction>): Batch {
        val batchHeader = BatchHeader.newBuilder()
            .setSignerPublicKey(signer?.publicKey?.hex())
            .addAllTransactionIds(transactions.map({transaction -> transaction.headerSignature  }))
            .build()

        val batch_signature = signer?.sign(batchHeader.toByteArray())

        return Batch.newBuilder()
            .setHeader(batchHeader.toByteString())
            .addAllTransactions(transactions.asIterable())
            .setHeaderSignature(batch_signature)
            .build()
    }

    private fun sendRequest(batch: Batch) {
        val batchList = BatchList.newBuilder()
            .addBatches(batch)
            .build()
            .toByteArray()

        val body = RequestBody.create(MediaType.parse("application/octet-stream"), batchList)

        val call1 = service?.postBatchList(body)
        call1?.enqueue(object : Callback<BatchListResponse> {
            override fun onResponse(call: Call<BatchListResponse>, response: Response<BatchListResponse>) {
                if(response.body() != null) {
                    Log.d("XO.State", response.body().toString())
                } else {
                    Log.d("XO.State", response.toString())
                }
            }
            override fun onFailure(call: Call<BatchListResponse>, t: Throwable) {
                Log.d("XO.State", t.toString())
                call.cancel()
            }
        })
    }

    private fun hash(input: String) : String{
        val digest = MessageDigest.getInstance("SHA-512")
        digest.reset()
        digest.update(input.toByteArray())
        return BaseEncoding.base16().lowerCase().encode(digest.digest())
    }

    private fun makeGameAddress(gameName: String) : String {
        val xo_prefix = hash("xo").substring(0, 6)
        val game_address = hash(gameName).substring(0, 64)
        return xo_prefix + game_address
    }

}
