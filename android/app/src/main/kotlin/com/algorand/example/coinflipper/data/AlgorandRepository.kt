package com.algorand.example.coinflipper.data

import android.util.Log
import com.algorand.algosdk.abi.Contract
import com.algorand.algosdk.account.Account
import com.algorand.algosdk.builder.transaction.MethodCallTransactionBuilder
import com.algorand.algosdk.builder.transaction.PaymentTransactionBuilder
import com.algorand.algosdk.crypto.Address
import com.algorand.algosdk.transaction.AtomicTransactionComposer
import com.algorand.algosdk.transaction.AtomicTransactionComposer.ReturnValue
import com.algorand.algosdk.transaction.SignedTransaction
import com.algorand.algosdk.transaction.Transaction
import com.algorand.algosdk.transaction.TransactionWithSigner
import com.algorand.algosdk.util.Encoder
import com.algorand.algosdk.v2.client.Utils
import com.algorand.algosdk.v2.client.common.AlgodClient
import com.algorand.algosdk.v2.client.common.Response
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse
import com.algorand.example.coinflipper.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.util.function.Consumer
import com.algorand.algosdk.v2.client.model.Account as AccountInfo

class AlgorandRepository() {
    private val TAG: String = "AlgorandRepository"
    private var client: AlgodClient = AlgodClient(
        Constants.ALGOD_API_ADDR,
        Constants.ALGOD_PORT,
        Constants.ALGOD_API_TOKEN,
        Constants.ALGOD_API_TOKEN_KEY
    )

    val txHeaders = arrayOf("Content-Type")
    val txValues = arrayOf("application/x-binary")

    fun generateAlgodPair() : Account {
        return Account()
    }

    fun recoverAccount(passPhrase : String) : Account? {
        try {
            return Account(passPhrase)
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getAccountInfo(account:  Account) : AccountInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val respAcct = client.AccountInformation(account.getAddress()).execute()
                val accountInfo = respAcct.body()
                Log.d(TAG, String.format("Account Balance: %d microAlgos", accountInfo.amount))
                accountInfo
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                null
            }
        }
    }

    suspend fun appOptIn(account: Account, appId: Long) : PendingTransactionResponse? {
        return withContext(Dispatchers.IO) {
            try {
                // define sender as creator
                val sender: Address = account.address
                val params: TransactionParametersResponse =
                    client.TransactionParams().execute().body()

                // create unsigned transaction
                val txn: Transaction = Transaction.ApplicationOptInTransactionBuilder()
                    .sender(sender)
                    .suggestedParams(params)
                    .applicationId(appId)
                    .build()

                // sign transaction
                val signedTxn: SignedTransaction = account.signTransaction(txn)

                // send to network
                val encodedTxBytes: ByteArray = Encoder.encodeToMsgPack(signedTxn)
                val txnId: String =
                    client.RawTransaction().rawtxn(encodedTxBytes).execute(txHeaders, txValues)
                        .body().txId
                Log.d(TAG, "Transaction $txnId")

                // Wait for transaction confirmation
                val pTrx: PendingTransactionResponse = Utils.waitForConfirmation(client, txnId, 10)
                Log.d(TAG, "Closed out from app-id: $appId and $pTrx")
                pTrx
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                null
            }
        }
    }

    suspend fun closeOutApp(account: Account, appId: Long) : PendingTransactionResponse? {
        return withContext(Dispatchers.IO) {
            try {
                // define sender as creator
                val sender: Address = account.address
                val params: TransactionParametersResponse =
                    client.TransactionParams().execute().body()

                // create unsigned transaction
                val txn: Transaction = Transaction.ApplicationCloseTransactionBuilder()
                    .sender(sender)
                    .suggestedParams(params)
                    .applicationId(appId)
                    .build()

                // sign transaction
                val signedTxn: SignedTransaction = account.signTransaction(txn)

                // send to network
                val encodedTxBytes: ByteArray = Encoder.encodeToMsgPack(signedTxn)
                val txnId: String =
                    client.RawTransaction().rawtxn(encodedTxBytes).execute(txHeaders, txValues)
                        .body().txId
                Log.d(TAG, "Transaction $txnId")

                // Wait for transaction confirmation
                val pTrx: PendingTransactionResponse = Utils.waitForConfirmation(client, txnId, 10)
                Log.d(TAG, "Closed out from app-id: $appId and $pTrx")
                pTrx
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                null
            }
        }
    }

    suspend fun getCurrentRound(account: Account, appId: Long) : Long {
        return withContext(Dispatchers.IO) {
            try {
                val params: TransactionParametersResponse =
                    client.TransactionParams().execute().body()
                        ?: client.TransactionParams().execute().body() ?: client.TransactionParams()
                            .execute().body()
                params.lastRound
            } catch (e: Exception) {
                0L
            }
        }
    }

    suspend fun createTransactionWithSigner(account: Account, appId: Long, amount: Int) : TransactionWithSigner? {
        return withContext(Dispatchers.IO) {
            try {
                // Get suggested params from client
                val rsp: Response<TransactionParametersResponse> =
                    client.TransactionParams().execute()
                val sp: TransactionParametersResponse = rsp.body() ?: rsp.body() ?: rsp.body()

                // Create a transaction
                val ptxn = PaymentTransactionBuilder.Builder()
                    .suggestedParams(sp)
                    .amount(amount)
                    .sender(account.address)
                    .receiver(Address.forApplication(appId))
                    .build()

                // Construct TransactionWithSigner
                val tws = TransactionWithSigner(ptxn, account.transactionSigner)
                tws
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                null
            }
        }
    }

    suspend fun appFlipCoin(account: Account, appId: Long, contractStr: String, amount: Int, isHeads: Boolean) : AtomicTransactionComposer.ExecuteResult? {
        return withContext(Dispatchers.IO) {
            try {
                val contract: Contract = Encoder.decodeFromJson(contractStr, Contract::class.java)

                val rsp = client.TransactionParams().execute()
                val tsp: TransactionParametersResponse = rsp.body()
                val tws = createTransactionWithSigner(account, appId, amount)
                val method_args = listOf(tws, isHeads)

                // create methodCallParams by builder (or create by constructor) for add method
                val mctb = MethodCallTransactionBuilder.Builder()
                mctb.applicationId(appId)
                mctb.sender(account.address.toString())
                mctb.signer(account.transactionSigner)
                mctb.suggestedParams(tsp)
                mctb.method(contract.getMethodByName("flip_coin"))
                mctb.methodArguments(method_args)
                mctb.onComplete(Transaction.OnCompletion.NoOpOC)

                val atc = AtomicTransactionComposer()
                //atc.addTransaction(tws);
                atc.addMethodCall(mctb.build())
                val res = atc.execute(client, 100)

                res.methodResults.forEach(Consumer<ReturnValue> { methodResult: ReturnValue? ->
                    Log.d(
                        TAG,
                        methodResult.toString()
                    )
                })

                Log.d(TAG, "flip coin call success for app-id: $appId")
                res
            } catch (e: Exception) {
                Log.e(TAG, "" + e.toString())
                null
            }
        }
    }

    suspend fun appSettleBet(account: Account, appId: Long, contractStr: String, randomBeaconApp: BigInteger) : AtomicTransactionComposer.ExecuteResult? {
        return withContext(Dispatchers.IO) {
            try {
                val contract: Contract = Encoder.decodeFromJson(contractStr, Contract::class.java)

                val rsp = client.TransactionParams().execute()
                val tsp: TransactionParametersResponse = rsp.body()
                val method_args = listOf(account.address, randomBeaconApp)

                // create methodCallParams by builder (or create by constructor) for add method
                val mctb = MethodCallTransactionBuilder.Builder()
                mctb.applicationId(appId)
                mctb.sender(account.address.toString())
                mctb.signer(account.transactionSigner)
                mctb.suggestedParams(tsp)
                mctb.method(contract.getMethodByName("settle"))
                mctb.methodArguments(method_args)
                mctb.onComplete(Transaction.OnCompletion.NoOpOC)

                val atc = AtomicTransactionComposer()
                //atc.addTransaction(tws);
                atc.addMethodCall(mctb.build())

                val res = atc.execute(client, 100)
                res.methodResults.forEach(Consumer<ReturnValue> { methodResult: ReturnValue? ->
                    Log.d(
                        TAG,
                        methodResult.toString()
                    )
                })
                Log.d(TAG, "flip coin call success for app-id: $appId")
                res
            } catch (e: Exception) {
                Log.e(TAG, "" + e.toString())
                null
            }
        }
    }
}
