********************************************
Client: Building and Submitting Transactions
********************************************

The process of encoding information to be submitted to a distributed ledger is
generally non-trivial. A series of cryptographic safeguards are used to
confirm identity and data validity. Hyperledger Sawtooth is no different, but
the Java SDK provides client functionality that handles
most of these details and greatly simplifies the process of making changes to
the blockchain.


Creating a Private Key and Signer
=================================

In order to confirm your identity and sign the information you send to the
validator, you will need a 256-bit key. Sawtooth uses the secp256k1 ECDSA
standard for signing, which means that almost any set of 32 bytes is a valid
key. It is fairly simple to generate a valid key using the SDK's *signing*
module.

A *Signer* wraps a private key and provides some convenient methods for signing
bytes and getting the private key's associated public key.


**Kotlin**

.. code-block:: kotlin

    import sawtooth.sdk.signing.Secp256k1Context
    import sawtooth.sdk.signing.Signer

    val context = Secp256k1Context()
    val privateKey = context.newRandomPrivateKey()
    val signer = Signer(context, privateKey)

**Java**

.. code-block::  java

    import sawtooth.sdk.signing.PrivateKey;
    import sawtooth.sdk.signing.Secp256k1Context;
    import sawtooth.sdk.signing.Signer;

    private Secp256k1Context context  = new Secp256k1Context();
    private PrivateKey privateKey = context.newRandomPrivateKey();
    private Signer signer = new Signer(context, privateKey);


.. note::

   This key is the **only** way to prove your identity on the blockchain. Any
   person possessing it will be able to sign Transactions using your identity,
   and there is no way to recover it if lost. It is very important that any
   private key is kept secret and secure.


Encoding Your Payload
=====================

Transaction payloads are composed of binary-encoded data that is opaque to the
validator. The logic for encoding and decoding them rests entirely within the
particular Transaction Processor itself. As a result, there are many possible
formats, and you will have to look to the definition of the Transaction
Processor itself for that information. As an example, the *IntegerKey*
Transaction Processor uses a payload of three key/value pairs encoded as
`CBOR <https://en.wikipedia.org/wiki/CBOR>`_. Creating one might look like this:


   **Kotlin**

   .. code-block:: kotlin

       import co.nstant.`in`.cbor.CborBuilder
       import co.nstant.`in`.cbor.CborEncoder

       val payload = ByteArrayOutputStream()
       CborEncoder(payload).encode(
            CborBuilder()
                .addMap()
                .put("Verb", "set")
                .put("Name", "foo")
                .put("Value", 42)
                .end()
                .build())

        val encodedBytes = payload.toByteArray()

   **Java**

   .. code-block::  java

       import co.nstant.in.cbor.CborBuilder;
       import co.nstant.in.cbor.CborEncoder;
       import co.nstant.in.cbor.CborException;

       ByteArrayOutputStream payload = new ByteArrayOutputStream();
        try {
            new CborEncoder(payload).encode(new CborBuilder()
                    .addMap()
                    .put("Verb", "set")
                    .put("Name", "foo")
                    .put("Value", 42)
                    .end()
                    .build());
        } catch (CborException e) {
            e.printStackTrace();
        }
        byte[] payloadBytes = payload.toByteArray();


Building the Transaction
========================

*Transactions* are the basis for individual changes of state to the Sawtooth
blockchain. They are composed of a binary payload, a binary-encoded
*TransactionHeader* with some cryptographic safeguards and metadata about how
it should be handled, and a signature of that header. It would be worthwhile
to familiarize yourself with the information in  `Transactions and Batches
<https://sawtooth.hyperledger.org/docs/core/releases/latest/architecture/transactions_and_batches.html>`_,
particularly the definition of TransactionHeaders.


1. Create the Transaction Header
--------------------------------

A TransactionHeader contains information for routing a transaction to the
correct transaction processor, what input and output state addresses are
involved, references to prior transactions it depends on, and the public keys
associated with the its signature. The header references the payload through a
SHA-512 hash of the payload bytes.

**Kotlin**

.. code-block:: kotlin

    import sawtooth.sdk.protobuf.TransactionHeader
    import java.util.UUID

    import com.google.common.io.BaseEncoding
    import java.security.MessageDigest

    fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        digest.reset()
        digest.update(input.toByteArray())
        return BaseEncoding.base16().lowerCase().encode(digest.digest())
    }

    val header = TransactionHeader.newBuilder()
      .setSignerPublicKey(signer.publicKey.hex())
      .setFamilyName("intkey")
      .setFamilyVersion("1.0")
      .addInputs("1cf1266e282c41be5e4254d8820772c5518a2c5a8c0c7f7eda19594a7eb539453e1ed7")
      .addOutputs("1cf1266e282c41be5e4254d8820772c5518a2c5a8c0c7f7eda19594a7eb539453e1ed7")
      .setPayloadSha512(hash(payload))
      .setBatcherPublicKey(signer.publicKey.hex())
      .setNonce(UUID.randomUUID().toString())
      .build()


**Java**

.. code-block::  java

    import sawtooth.sdk.protobuf.TransactionHeader;
    import java.util.UUID;

    TransactionHeader header = TransactionHeader.newBuilder()
      .setSignerPublicKey(signer.getPublicKey().hex())
      .setFamilyName("xo")
      .setFamilyVersion("1.0")
      .addInputs("1cf1266e282c41be5e4254d8820772c5518a2c5a8c0c7f7eda19594a7eb539453e1ed7")
      .addOutputs("1cf1266e282c41be5e4254d8820772c5518a2c5a8c0c7f7eda19594a7eb539453e1ed7")
      .setPayloadSha512(hash(payload))
      .setBatcherPublicKey(signer.getPublicKey().hex())
      .setNonce(UUID.randomUUID().toString())
      .build();

.. note::

   Remember that a *batcher public_key* is the hex public key matching the private
   key that will later be used to sign a Transaction's Batch, and
   *dependencies* are the *header signatures* of Transactions that must be
   committed before this one (see `TransactionHeaders
   <https://sawtooth.hyperledger.org/docs/core/releases/latest/
   architecture/transactions_and_batches.html>`_).

.. note::

   The *inputs* and *outputs* are the state addresses a Transaction is allowed
   to read from or write to. With the Transaction above, we referenced the
   specific address where the value of  ``'foo'`` is stored.  Whenever possible,
   specific addresses should be used, as this will allow the validator to
   schedule transaction processing more efficiently.

   Note that the methods for assigning and validating addresses are entirely up
   to the Transaction Processor. In the case of IntegerKey, there are `specific
   rules to generate valid addresses <https://sawtooth.hyperledger.org/docs/core/
   releases/latest/transaction_family_specifications/
   integerkey_transaction_family.html#addressing>`_, which must be followed or
   Transactions will be rejected. You will need to follow the addressing rules
   for whichever Transaction Family you are working with.


2. Create the Transaction
-------------------------

Once the TransactionHeader is constructed, its bytes are then used to create a
signature.  This header signature also acts as the ID of the transaction.  The
header bytes, the header signature, and the payload bytes are all used to
construct the complete Transaction.

**Kotlin**

.. code-block:: kotlin

    import com.google.protobuf.ByteString
    import sawtooth.sdk.protobuf.Transaction

    val signature = signer.sign(header.toByteArray())

    val transaction =  Transaction.newBuilder()
                        .setHeader(header.toByteString())
                        .setPayload(ByteString.copyFrom(payloadBytes))
                        .setHeaderSignature(signature)
                        .build()

**Java**

.. code-block::  java

    import com.google.protobuf.ByteString;
    import sawtooth.sdk.protobuf.Transaction;

    String signature = signer.sign(header.toByteArray());

    Transaction transaction =  Transaction.newBuilder()
                                   .setHeader(header.toByteString())
                                   .setPayload(ByteString.copyFrom(payloadBytes))
                                   .setHeaderSignature(signature)
                                   .build();


3. (optional) Encode the Transaction(s)
---------------------------------------

If the same machine is creating Transactions and Batches there is no need to
encode the Transaction instances. However, in the use case where Transactions
are being batched externally, they must be serialized before being transmitted
to the batcher. The Java SDK offers two options for this. One or more
Transactions can be combined into a serialized *TransactionList* method, or can
be serialized as a single Transaction.


**Kotlin**

.. code-block:: kotlin

    import sawtooth.sdk.protobuf.TransactionList

    val txn_list_bytes = TransactionList.newBuilder()
        .addTransactions(txn1)
        .addTransactions(txn2)
        .build()
        .toByteString()

    txn_bytes = txn.toByteString()

**Java**

.. code-block::  java

    import com.google.protobuf.ByteString;
    import sawtooth.sdk.protobuf.TransactionList;

    ByteString txn_list_bytes = TransactionList.newBuilder()
        .addTransactions(txn1)
        .addTransactions(txn2)
        .build()
        .toByteString();

    ByteString txn_bytes = transaction.toByteString();


Building the Batch
==================

Once you have one or more Transaction instances ready, they must be wrapped in a
*Batch*. Batches are the atomic unit of change in Sawtooth's state. When a Batch
is submitted to a validator each Transaction in it will be applied (in order),
or *no* Transactions will be applied. Even if your Transactions are not
dependent on any others, they cannot be submitted directly to the validator.
They must all be wrapped in a Batch.


1. Create the BatchHeader
-------------------------

Similar to the TransactionHeader, there is a *BatchHeader* for each Batch.
As Batches are much simpler than Transactions, a BatchHeader needs only  the
public key of the signer and the list of Transaction IDs, in the same order they
are listed in the Batch.


**Kotlin**

.. code-block:: kotlin

    import sawtooth.sdk.protobuf.BatchHeader

    val transactions = arrayOf(transaction)

    val batchHeader = BatchHeader.newBuilder()
           .setSignerPublicKey(signer.publicKey.hex())
           .addAllTransactionIds(
              transactions.map { transaction -> transaction.headerSignature }
           )
           .build()

**Java**

.. code-block::  java

      import sawtooth.sdk.protobuf.BatchHeader;
      import sawtooth.sdk.protobuf.Transaction;

      List<Transaction> transactions = new ArrayList();
      transactions.add(transaction);

      BatchHeader batchHeader = BatchHeader.newBuilder()
          .setSignerPublicKey(signer.getPublicKey().hex())
          .addAllTransactionIds(
                  transactions
                          .stream()
                          .map(Transaction::getHeaderSignature)
                          .collect(Collectors.toList())
          )
          .build();

2. Create the Batch
-------------------

Using the SDK, creating a Batch is similar to creating a transaction.  The
header is signed, and the resulting signature acts as the Batch's ID.  The Batch
is then constructed out of the header bytes, the header signature, and the
transactions that make up the batch.

**Kotlin**

.. code-block:: kotlin

        import sawtooth.sdk.protobuf.Batch

        val batchSignature = signer.sign(batchHeader.toByteArray())

        val batch = Batch.newBuilder()
            .setHeader(batchHeader.toByteString())
            .addAllTransactions(transactions.asIterable())
            .setHeaderSignature(batchSignature)
            .build()

**Java**

.. code-block::  java

    import sawtooth.sdk.protobuf.Batch;

    String batchSignature = signer.sign(batchHeader.toByteArray());

    Batch batch = Batch.newBuilder()
             .setHeader(batchHeader.toByteString())
             .addAllTransactions(transactions)
             .setHeaderSignature(batchSignature)
             .build();

3. Encode the Batch(es) in a BatchList
--------------------------------------

In order to submit Batches to the validator, they  must be collected into a
*BatchList*.  Multiple batches can be submitted in one BatchList, though the
Batches themselves don't necessarily need to depend on each other. Unlike
Batches, a BatchList is not atomic. Batches from other clients may be
interleaved with yours.


**Kotlin**

.. code-block:: kotlin

    import sawtooth.sdk.protobuf.BatchList

    val batchList = BatchList.newBuilder()
        .addBatches(batch)
        .build()
        .toByteArray()

**Java**

.. code-block::  java

      import sawtooth.sdk.protobuf.BatchList;

      byte[] batchListBytes = BatchList.newBuilder()
              .addBatches(batch)
              .build()
              .toByteArray();

.. note::

   Note, if the transaction creator is using a different private key than the
   batcher, the *batcher public_key* must have been specified for every Transaction,
   and must have been generated from the private key being used to sign the
   Batch, or validation will fail.


Submitting Batches to the Validator
===================================

The prescribed way to submit Batches to the validator is via the REST API.
This is an independent process that runs alongside a validator, allowing clients
to communicate using HTTP/JSON standards. Simply send a *POST* request to the
*/batches* endpoint, with a *"Content-Type"* header of
*"application/octet-stream"*, and the *body* as a serialized *BatchList*.

There are a many ways to make an HTTP request, and hopefully the submission
process is fairly straightforward from here, but as an example in Kotlin, this is what it
might look if you sent the request from the same process that
prepared the BatchList.

 **Kotlin**

.. code-block:: kotlin

      import okhttp3.RequestBody
      import retrofit2.Call
      import retrofit2.http.Body
      import retrofit2.http.POST
      import retrofit2.converter.gson.GsonConverterFactory

      interface SawtoothRestApi {
          @POST("/batches")
          fun postBatchList(@Body payload: RequestBody): Call<BatchListResponse>
      }

      val retrofit = Retrofit.Builder()
           .baseUrl("http://rest.api.domain/batches")
           .addConverterFactory(GsonConverterFactory.create())
           .build()

       val service = retrofit.create<SawtoothRestApi>(SawtoothRestApi::class.java)

       val body = RequestBody.create(
                    MediaType.parse("application/octet-stream"),
                    batchListBytes)

       val call1 = service.postBatchList(body)
       call1.enqueue(object : Callback<BatchListResponse> {
           override fun onResponse(call: Call<BatchListResponse>, response: Response<BatchListResponse>) {
                 if (response.body() != null) {
                     Log.d("Response", response.body().toString())
                 } else {
                     Log.d("Response", response.toString())
                 }
           }
           override fun onFailure(call: Call<BatchListResponse>, t: Throwable) {
               Log.d("Response", "Failed to submit transaction")
               call.cancel()
           }
       })



And here is what it would look like if you saved the binary to a file, and then
sent it from the command line with ``curl``:


 **Kotlin**

.. code-block:: kotlin

  import java.io.File
  import java.nio.file.Files
  import java.nio.file.StandardOpenOption

  val myfile = File("intkey.batches")
  Files.write(myfile.toPath(), batchListBytes, StandardOpenOption.APPEND)


.. code-block:: bash

   % curl --request POST \
       --header "Content-Type: application/octet-stream" \
       --data-binary @intkey.batches \
       "http://rest.api.domain/batches"


.. Licensed under Creative Commons Attribution 4.0 International License
.. https://creativecommons.org/licenses/by/4.0/
