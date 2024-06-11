package com.algorand.example.coinflipper.ui.login

import androidx.lifecycle.viewModelScope
import com.algorand.example.coinflipper.ui.common.BaseViewModel
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {
    fun createAccount() {
        viewModelScope.launch {
            val result = repository.generateAlgodPair()
            result.let {
                accountLiveData.value = it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
