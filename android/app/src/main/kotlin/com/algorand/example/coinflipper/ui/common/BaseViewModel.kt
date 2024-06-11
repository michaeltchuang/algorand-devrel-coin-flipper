package com.algorand.example.coinflipper.ui.common

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorand.algosdk.account.Account
import com.algorand.example.coinflipper.data.AlgorandRepository
import com.algorand.example.coinflipper.utils.Constants
import kotlinx.coroutines.launch
import com.algorand.algosdk.v2.client.model.Account as AccountInfo

open class BaseViewModel : ViewModel() {
    companion object {
        const val TAG = "BaseViewModel"
    }

    val accountLiveData = MutableLiveData<Account?>()
    val appOptInStateLiveData = MutableLiveData<Boolean>()
    var account: Account? = null
    var accountInfo: AccountInfo? = null
    var hasExistingBet = false
    var commitmentRound = 0L

    val repository: AlgorandRepository = AlgorandRepository()

    fun recoverAccount(
        passphrase: String?,
        appOptInStateCheck: Boolean,
    ) {
        if (passphrase == null) {
            accountLiveData.value = null
        }

        passphrase?.apply {
            viewModelScope.launch {
                val result = repository.recoverAccount(passphrase)
                result?.let { r ->
                    accountLiveData.value = r
                    account = r
                    accountInfo = r.let { repository.getAccountInfo(it) }

                    // auto opt into app if account exists
                    if (appOptInStateCheck) {
                        appOptInStateCheck(r, Constants.COINFLIP_APP_ID_TESTNET)
                    }
                }
            }
        }
    }

    fun appOptInStateCheck(
        account: Account,
        appId: Long,
    ) {
        viewModelScope.launch {
            try {
                var optInAlready = false
                hasExistingBet = false
                val accountInfo = repository.getAccountInfo(account)

                // skip if opt in already
                accountInfo?.appsLocalState?.forEach {
                    if (it.id == appId) {
                        Log.d(TAG, String.format("Account has already opt in to app id: " + appId))
                        optInAlready = true
                        if (it.keyValue.size > 0) {
                            hasExistingBet = true
                        }
                    }
                }
                Log.d(TAG, "account app ($appId) - opt in state($optInAlready), has existing bet($hasExistingBet)")
                appOptInStateLiveData.value = optInAlready
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    fun appOptIn(
        account: Account,
        appId: Long,
    ) {
        viewModelScope.launch {
            try {
                val res = repository.appOptIn(account, appId)
                if (res?.confirmedRound != null) {
                    appOptInStateCheck(account, appId)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    fun closeOutApp(
        account: Account,
        appId: Long,
    ) {
        viewModelScope.launch {
            try {
                val res = repository.closeOutApp(account, appId)
                if (res?.confirmedRound != null) {
                    appOptInStateCheck(account, appId)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }
}
