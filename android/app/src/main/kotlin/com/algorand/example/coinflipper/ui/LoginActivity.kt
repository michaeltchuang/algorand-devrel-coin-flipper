package com.algorand.example.coinflipper.ui

import android.app.ActionBar
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.algorand.example.coinflipper.R
import com.algorand.example.coinflipper.databinding.ActivityLoginBinding
import com.algorand.example.coinflipper.ui.common.BaseActivity
import com.algorand.example.coinflipper.ui.login.LoginViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class LoginActivity : BaseActivity() {
    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = getString(R.string.app_name_long)

        binding =
            DataBindingUtil.setContentView<ActivityLoginBinding?>(this, R.layout.activity_login)
                .apply {
                    composeActivityLogin.setContent {
                        loginActivityComposable()
                    }
                }
        loginViewModel =
            ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory(application),
            ).get(LoginViewModel::class.java)
        Security.removeProvider("BC")
        Security.insertProviderAt(BouncyCastleProvider(), 0)
    }

    @Preview
    @Suppress("ComposableNaming")
    @Composable
    fun loginActivityComposable() {
        val label = getString(R.string.login_restore_textifield_title)
        val dataInput = getString(R.string.login_restore_textfield_value)
        passphraseTextField = dataInput

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.login_guest_message),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                style =
                    TextStyle(
                        fontSize = 24.sp,
                    ),
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .width(282.dp)
                        .wrapContentHeight(),
            )
            Image(
                painter = painterResource(id = R.drawable.coin_heads),
                contentDescription = stringResource(id = R.string.login_guest_message),
                modifier =
                    Modifier
                        .wrapContentSize()
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .size(100.dp),
            )
            algorandDivider()
            algorandButton(
                resourceId = R.string.login_button_create,
                stringResourceId = R.string.login_button_create,
            )
            algorandDivider()
            passphraseField(label, dataInput)
            algorandButton(
                resourceId = R.string.login_button_restore,
                stringResourceId = R.string.login_button_restore,
            )
            algorandDivider()
            Text(
                stringResource(R.string.login_disclaimer),
                color = Color.Black,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .width(262.dp)
                        .wrapContentHeight()
                        .alpha(0.6F),
            )
        }
    }

    override fun onClick(resourceId: Int): Boolean {
        when (resourceId) {
            R.string.login_button_create -> {
                loginViewModel.accountLiveData.observe(this) {
                    if (it == null) {
                        showSnackbar(getString(R.string.error_account_create))
                        return@observe
                    }

                    it.apply {
                        Log.d(TAG, "address: ${it.address}")
                        Log.d(TAG, "passphrase: ${it.toMnemonic()}")

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("address", it.address.toString())
                        intent.putExtra("passphrase", it.toMnemonic().toString())
                        startActivity(intent)
                        finish()
                    }
                }
                loginViewModel.createAccount()
            }
            R.string.login_button_restore -> {
                loginViewModel.accountLiveData.observe(this) {
                    if (it == null) {
                        showSnackbar(getString(R.string.error_account_restore))
                        return@observe
                    }

                    it.apply {
                        Log.d(TAG, "address: ${it.address}")
                        Log.d(TAG, "passphrase: ${it.toMnemonic()}")

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("address", it.address.toString())
                        intent.putExtra("passphrase", it.toMnemonic().toString())
                        startActivity(intent)
                        finish()
                    }
                }
                loginViewModel.recoverAccount(passphraseTextField, true)
            }
        }
        return true
    }

    private fun showSnackbar(str: String) {
        val snackbar =
            Snackbar.make(
                binding.root,
                str,
                Snackbar.LENGTH_LONG,
            )
        val layoutParams = ActionBar.LayoutParams(snackbar.view.layoutParams)
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.gravity = Gravity.CENTER or Gravity.CENTER_HORIZONTAL
        layoutParams.setMargins(50, 100, 50, 0)
        snackbar.view.layoutParams = layoutParams
        snackbar.setBackgroundTint(getColor(R.color.gray_333333))
        snackbar.setTextColor(getColor(R.color.white))
        snackbar.view.setPadding(20, 10, 20, 0)
        (snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text))?.textAlignment =
            View.TEXT_ALIGNMENT_CENTER
        snackbar.setActionTextColor(getColor(R.color.teal_700))
        val snackbarActionTextView =
            snackbar.view.findViewById<View>(com.google.android.material.R.id.snackbar_action) as TextView
        snackbarActionTextView.isAllCaps = false
        snackbarActionTextView.setTypeface(snackbarActionTextView.typeface, Typeface.BOLD)
        snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackbar.show()
    }
}
