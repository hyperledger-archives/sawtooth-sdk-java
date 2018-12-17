package bitwiseio.sawtooth.xo.state.api

import android.net.Uri
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import com.google.protobuf.ByteString
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sawtooth.sdk.signing.Secp256k1Context
import sawtooth.sdk.signing.Signer
import java.util.UUID
import bitwiseio.sawtooth.xo.state.makeGameAddress
import bitwiseio.sawtooth.xo.state.hash
import sawtooth.sdk.protobuf.Batch
import sawtooth.sdk.protobuf.BatchList
import sawtooth.sdk.protobuf.BatchHeader
import sawtooth.sdk.protobuf.Transaction
import sawtooth.sdk.protobuf.TransactionHeader
import sawtooth.sdk.signing.PrivateKey

class XORequestHandler(private var restApiURL: String, privateKey: PrivateKey) {
    private var service: SawtoothRestApi? = null
    private var signer: Signer? = null

    init {
        buildService()
        val context = Secp256k1Context()
        signer = Signer(context, privateKey)
    }

    fun createGame(gameName: String, view: View, restApiURL: String, callback: (Boolean) -> Unit) {
        checkURLChanged(restApiURL)
        val createGameTransaction = makeTransaction(gameName, "create", null)
        val batch = makeBatch(arrayOf(createGameTransaction))
        sendRequest(batch, view, callback={ it->
            callback(it)
        })
    }

    fun takeSpace(gameName: String, space: String, view: View, restApiURL: String, callback: (Boolean) -> Unit) {
        checkURLChanged(restApiURL)
        val takeSpaceTransaction = makeTransaction(gameName, "take", space)
        val batch = makeBatch(arrayOf(takeSpaceTransaction))
        sendRequest(batch, view, callback={ it->
            callback(it)
        })
    }

    private fun checkURLChanged(url: String) {
        if (restApiURL != url) {
            restApiURL = url
            buildService()
        }
    }

    private fun buildService() {
        val retrofit = Retrofit.Builder()
            .baseUrl(restApiURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create<SawtoothRestApi>(SawtoothRestApi::class.java)
    }

    private fun makeTransaction(gameName: String, action: String, space: String?): Transaction {
        val payload = "$gameName,$action,$space"

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
            .addAllTransactionIds(transactions.map { transaction -> transaction.headerSignature })
            .build()

        val batchSignature = signer?.sign(batchHeader.toByteArray())

        return Batch.newBuilder()
            .setHeader(batchHeader.toByteString())
            .addAllTransactions(transactions.asIterable())
            .setHeaderSignature(batchSignature)
            .build()
    }

    private fun sendRequest(batch: Batch, view: View, callback: (Boolean) -> Unit) {
        val batchList = BatchList.newBuilder()
            .addBatches(batch)
            .build()
            .toByteArray()

        val body = RequestBody.create(MediaType.parse("application/octet-stream"), batchList)

        val call1 = service?.postBatchList(body)
        call1?.enqueue(object : Callback<BatchListResponse> {
            override fun onResponse(call: Call<BatchListResponse>, response: Response<BatchListResponse>) {
                if (response.body() != null) {
                    Log.d("XO.State", response.body().toString())
                    waitForBatch(response.body()?.link, 5, view, callback={ it ->
                        callback(it)
                    })
                } else {
                    Snackbar.make(view, "Failed to submit transaction", Snackbar.LENGTH_LONG).show()
                    Log.d("XO.State", response.toString())
                }
            }
            override fun onFailure(call: Call<BatchListResponse>, t: Throwable) {
                Log.d("XO.State", t.toString())
                Snackbar.make(view, "Failed to submit transaction", Snackbar.LENGTH_LONG).show()
                call.cancel()
            }
        })
    }

    private fun waitForBatch(batchLink: String?, wait: Int, view: View, callback: (Boolean) -> Unit) {
        val uri = Uri.parse(batchLink)
        val batchId = uri.getQueryParameter("id")
        if (batchId != null) {
            val call1 = service?.getBatchStatus(batchId, wait)
            call1?.enqueue(object : Callback<BatchStatusResponse> {
                override fun onResponse(call: Call<BatchStatusResponse>, response: Response<BatchStatusResponse>) {
                    Log.d("XO.State", response.body().toString())
                    Snackbar.make(view, "Batch status: " + response.body()?.data?.get(0)?.status, Snackbar.LENGTH_LONG).show()
                    callback(true)
                }
                override fun onFailure(call: Call<BatchStatusResponse>, t: Throwable) {
                    Log.d("XO.State", t.toString())
                    Snackbar.make(view, "Failed to get batch status", Snackbar.LENGTH_LONG).show()
                    call.cancel()
                }
            })
        } else {
            Log.d("XO.State", "Failed to retrieve batch id. Cannot request batch status.")
            Snackbar.make(view, "Failed to get batch status", Snackbar.LENGTH_LONG).show()
        }
    }
}
