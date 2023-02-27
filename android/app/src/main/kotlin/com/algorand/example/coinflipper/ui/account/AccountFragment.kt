package com.algorand.example.coinflipper.ui.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.algorand.example.coinflipper.R
import com.algorand.example.coinflipper.databinding.FragmentAccountBinding
import com.algorand.example.coinflipper.ui.LoginActivity
import com.algorand.example.coinflipper.ui.MainActivity
import com.algorand.example.coinflipper.ui.common.BaseFragment

class AccountFragment : BaseFragment() {
    private val TAG: String = "AccountFragment"
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as MainActivity).actionBar?.title = getString(R.string.app_name_long)

        binding = DataBindingUtil.inflate<FragmentAccountBinding>(
            inflater,
            R.layout.fragment_account,
            container,
            false
        )
            .apply {
                composeFragmentAccount.setContent {
                    AccountFragmentComposable()
                }
            }
        accountViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.getIntent()?.getExtras()?.getString("passphrase")?.apply {
            accountViewModel.recoverAccount(this, true)
        }
    }

    @Composable
    fun AccountFragmentComposable() {
        val account by accountViewModel.accountLiveData.observeAsState()
        if (account == null) {
            Log.d(TAG, "No account detected")
            onClick(R.string.account_button_lock)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.White)
        ) {
            (activity as MainActivity)
            .PassphraseField(
                getString(R.string.account_address),
                account?.address.toString()
            )
            (activity as MainActivity)
                .PassphraseField(
                getString(R.string.account_passphrase),
                account?.toMnemonic().toString())
            AlgorandFragmentButton(R.string.account_button_lock,
                    R.string.account_button_lock)
        }
    }

    override fun onClick(resourceId: Int): Boolean {
        when (resourceId) {
            R.string.account_button_lock -> {
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }
        return true
    }
}

