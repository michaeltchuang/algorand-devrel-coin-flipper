package com.algorand.example.coinflipper.ui.play

import android.app.GameState
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.algorand.example.coinflipper.ui.common.BaseViewModel
import com.algorand.example.coinflipper.utils.Constants
import kotlinx.coroutines.launch
import java.math.BigInteger

class PlayViewModel : BaseViewModel() {
    companion object {
        const val TAG: String = "PlayViewModel"
    }

    val appGameStateLiveData = MutableLiveData<GameState>()
    val snackBarLiveData = MutableLiveData<String>()
    val currentRoundLiveData = MutableLiveData<Long>()
    var currentRound = 0L
    var currentGameState: GameState = GameState.BET
    var betMicroAlgosAmount = Constants.DEFAULT_MICRO_ALGO_BET_AMOUNT
    var isHeads = true
    var contract = ""

    enum class GameState {
        BET,
        PENDING,
        SETTLE,
    }

    fun hasExistingBetState() {
        accountInfo?.appsLocalState?.firstOrNull({ it.id == Constants.COINFLIP_APP_ID_TESTNET })
            .let { applicationLocalState ->
                // first matching app id
                // Log.d(TAG, "User's application local state: ${applicationLocalState?.keyValue.toString()}")
                applicationLocalState?.keyValue?.forEach {
                    when (it.key) {
                        Constants.COINFLIP_APP_COMMITTMENT_ROUND_KEY -> {
                            if (commitmentRound == 0L) {
                                commitmentRound = it.value.uint.toLong()
                            }
                            // Log.d(TAG, "User's application local state: ${it}")
                        }
                    }
                }
            }
        if (currentRound < (commitmentRound + 10L)) {
            currentGameState = GameState.PENDING
            getCurrentRound()
        } else if (currentRound == 0L || commitmentRound == 0L) {
            // skip if network error
        } else {
            currentGameState = GameState.SETTLE
        }
        appGameStateLiveData.value = currentGameState
    }

    fun updateGameState() {
        viewModelScope.launch {
            when (currentGameState) {
                GameState.BET -> {
                    val acct = account
                    acct?.apply {
                        val result = repository.appFlipCoin(acct, Constants.COINFLIP_APP_ID_TESTNET, contract, betMicroAlgosAmount, isHeads)
                        if (result?.confirmedRound != null) {
                            commitmentRound = (result.methodResults.get(0).value as BigInteger).toLong()
                            hasExistingBet = true
                            currentGameState = GameState.PENDING
                        } else {
                            snackBarLiveData.value = "Could not submit bet on chain.  Please check logs for issue"
                            currentGameState = GameState.BET
                        }
                    }
                }
                GameState.PENDING -> {
                    // currentGameState = GameState.SETTLE
                    // settle button is not clickable when pending
                }
                GameState.SETTLE -> {
                    val acct = account
                    acct?.apply {
                        val result =
                            repository.appSettleBet(
                                acct,
                                Constants.COINFLIP_APP_ID_TESTNET,
                                contract,
                                BigInteger.valueOf(Constants.RANDOM_BEACON_APPID),
                            )
                        if (result?.confirmedRound == null) {
                            snackBarLiveData.value = "Could not settle bet on chain.  Please check logs for issue"
                        } else if (result.methodResults == null) {
                            snackBarLiveData.value = "Unexpected server response.  Please check logs for detail"
                        } else {
                            // successful result
                            val betResult = result.methodResults?.get(0)?.value as Array<*>
                            snackBarLiveData.value = createAlertMessage(betResult)
                            resetGame()
                        }
                    }
                }
            }
            appGameStateLiveData.value = currentGameState
        }
    }

    fun createAlertMessage(result: Array<*>): String {
        val outcome = if (result.get(0) as Boolean == true) "Won!" else "Lost :("
        val amount = result.get(1) as BigInteger
        val msg = "You $outcome  ($amount)"
        return(msg)
    }

    fun getCurrentRound() {
        viewModelScope.launch {
            val acct = account
            acct?.apply {
                val round = repository.getCurrentRound(acct, Constants.COINFLIP_APP_ID_TESTNET)
                Log.d(TAG, "Current Round: $round")
                currentRound = round
                currentRoundLiveData.value = round
            }
        }
    }

    fun calculateProgress(): Float {
        val targetRound = commitmentRound + 10L
        if (commitmentRound == 0L) {
            return 0.0f
        } else if (currentRound >= targetRound) {
            return 1.0f
        } else {
            val diff = targetRound - currentRound
            Log.d(TAG, "$targetRound $currentRound $diff ${(diff / 10.0f)}")
            return (10.0f - diff) / 10.0f
        }
    }

    fun resetGame() {
        currentGameState = GameState.BET
        betMicroAlgosAmount = Constants.DEFAULT_MICRO_ALGO_BET_AMOUNT
        commitmentRound = 0L
        hasExistingBet = false
    }
}
