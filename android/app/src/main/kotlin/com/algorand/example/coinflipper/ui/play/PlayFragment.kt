package com.algorand.example.coinflipper.ui.play

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.algorand.example.coinflipper.R
import com.algorand.example.coinflipper.databinding.FragmentPlayBinding
import com.algorand.example.coinflipper.ui.MainActivity
import com.algorand.example.coinflipper.ui.common.BaseFragment
import com.algorand.example.coinflipper.utils.Constants


class PlayFragment : BaseFragment() {
    private val TAG: String = "PlayFragment"
    private lateinit var playViewModel: PlayViewModel
    private lateinit var binding: FragmentPlayBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as MainActivity).actionBar?.title = getString(R.string.app_name_long)

        binding = DataBindingUtil.inflate<FragmentPlayBinding>(
            inflater,
            R.layout.fragment_play,
            container,
            false
        )
            .apply {
                composeFragmentPlay.setContent {
                    PlayFragmentComposable()
                }
            }

        playViewModel = ViewModelProvider(this).get(PlayViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.getIntent()?.getExtras()?.getString("passphrase")?.apply {
            playViewModel.recoverAccount(this, true)
            playViewModel.contract = getJsonDataFromAsset(
                requireActivity(), "contract.json") ?: ""
        }
        playViewModel.snackBarLiveData.observe(viewLifecycleOwner, Observer {
            (activity as MainActivity).showSnackbar(it)
        })

        val mainHandler = Handler(Looper.getMainLooper())
        var refresh = Runnable() {}
        refresh = Runnable {
            if(playViewModel.hasExistingBet) {
                playViewModel.hasExistingBetState()
            }
            mainHandler.postDelayed(refresh, 1000)
        }
        mainHandler.postDelayed(refresh, 1000)
    }

    @Composable
    fun PlayFragmentComposable() {
        val appOptInStateLiveData by playViewModel.appOptInStateLiveData.observeAsState()
        val appOptInState = appOptInStateLiveData

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            when (appOptInState) {
                true -> {
                    //Allowed to Play
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .height(400.dp)
                                .fillMaxWidth()
                        ) {
                            CoinFlipGameComposable()
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(),
                            ) {
                                (activity as MainActivity).AlgorandDivider()
                                AlgorandFragmentButton(
                                    resourceId = R.string.play_button_close_out,
                                    stringResourceId = R.string.play_button_close_out
                                )
                            }
                        }
                    }
                }
                false -> {
                    //Not allowed to Play
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(),
                    ) {
                        AlgorandFragmentButton(
                            resourceId = R.string.play_button_opt_in,
                            stringResourceId = R.string.play_button_opt_in
                        )
                    }
                }
                else -> {}
            }
        }
    }

    @Composable
    fun CoinFlipGameComposable() {
        val appGameStateLiveData by playViewModel.appGameStateLiveData.observeAsState()
        val appGameState = appGameStateLiveData
        when (appGameState) {
            PlayViewModel.GameState.BET -> {
                //Bet Form
                BetViewComposable()
            }
            PlayViewModel.GameState.PENDING -> {
                //Coin Flipping
                PendingViewComposable()
            }
            PlayViewModel.GameState.SETTLE -> {
                //Settle bet
                SettleViewComposable()
            }
            else -> {
                //Default First View
                if (playViewModel.hasExistingBet) {
                    playViewModel.hasExistingBetState()
                } else {
                    //Bet Form
                    BetViewComposable()
                }
            }
        }
    }

    @Composable
    fun BetViewComposable() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.play_title),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    fontSize = 24.sp
                ),
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
            )
            playViewModel.accountInfo?.let {
                Text(
                    stringResource(R.string.play_balance) + " ${it.amount} mAlgos",
                    color = Color.Black,
                    style = TextStyle(
                        fontSize = 16.sp
                    ),
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                )
            }
            (activity as MainActivity).AlgorandDivider()
            Text(
                stringResource(R.string.play_game_header),
                color = Color.Black,
                style = TextStyle(
                    fontSize = 16.sp
                ),
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
            )
            Row(
                modifier = Modifier
                    .heightIn(max = 100.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val painter = painterResource(R.drawable.coin_heads)
                Image(
                    painter = painter,
                    contentDescription = stringResource(
                        id = R.string.play_game_instructions
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(
                            weight = painter.intrinsicSize.width / painter.intrinsicSize.height,
                            fill = false
                        )
                        .clickable {
                            if (onClick(R.string.play_button_flip_heads)) {
                                Modifier.alpha(1.0f)
                            } else {
                                Modifier.alpha(0.3f)
                            }
                        }
                );
                Image(
                    painter = painterResource(R.drawable.coin_tails),
                    contentDescription = stringResource(
                        id = R.string.play_game_instructions
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(
                            weight = painter.intrinsicSize.width / painter.intrinsicSize.height,
                            fill = false
                        )
                        .clickable {
                            if (onClick(R.string.play_button_flip_tails)) {
                                Modifier.alpha(1.0f)
                            } else {
                                Modifier.alpha(0.3f)
                            }
                        }
                )
            }
            (activity as MainActivity).AlgorandDivider()
            Text(
                stringResource(R.string.play_game_instructions),
                color = Color.Black,
                style = TextStyle(
                    fontSize = 16.sp
                ),
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(start = 30.dp, end = 30.dp)
            )
        }
    }

    @Composable
    fun PendingViewComposable() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
        ) {
            ProgressComposable()
            AlgorandFragmentButton(
                resourceId = R.string.play_button_pending,
                stringResourceId = R.string.play_button_pending
            )
        }
    }

    @Composable
    fun SettleViewComposable() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
        ) {
            ProgressComposable()
            AlgorandFragmentButton(
                resourceId = R.string.play_button_settle,
                stringResourceId = R.string.play_button_settle
            )
        }
    }

    @Composable
    fun ProgressComposable() {
        val currentRoundLiveData by playViewModel.currentRoundLiveData.observeAsState()
        val currentRound = currentRoundLiveData
        if (currentRound != null) {
            Text(
                stringResource(R.string.play_current_round) + " ${playViewModel.currentRound}",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    fontSize = 24.sp
                ),
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
            )
            Text(
                stringResource(R.string.play_waiting_round) + " ${(playViewModel.commitmentRound + 10L)}",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    fontSize = 24.sp
                ),
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
            )
            CircularProgressIndicator(progress = playViewModel.calculateProgress(),
                backgroundColor = colorResource(R.color.gray_999999),
                color = colorResource(R.color.teal_700),
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round,
                modifier = Modifier.size(size = 100.dp)
            )
            (activity as MainActivity).AlgorandDivider()
        }
    }

    override fun onClick(resourceId: Int): Boolean {
        playViewModel.accountInfo?.let {
            if (it.amount < (Constants.DEFAULT_MICRO_ALGO_BET_AMOUNT + Constants.DEFAULT_MICRO_ALGO_TXN_FEE_SAFE_AMOUNT)) {
                Log.d(TAG, getString(R.string.play_balance_low))
                (activity as MainActivity).showSnackbar(getString(R.string.play_balance_low))
                return true
            }
        }
        when (resourceId) {
            R.string.play_button_opt_in -> {
                playViewModel.account?.let { playViewModel.appOptIn(it, Constants.COINFLIP_APP_ID_TESTNET) }
                (activity as MainActivity).showSnackbar(getString(R.string.play_button_wait_message))
            }
            R.string.play_button_close_out -> {
                playViewModel.account?.let { playViewModel.closeOutApp(it, Constants.COINFLIP_APP_ID_TESTNET) }
                (activity as MainActivity).showSnackbar(getString(R.string.play_button_wait_message))
            }
            R.string.play_button_flip_heads -> {
                playViewModel.isHeads = true
                playViewModel.updateGameState()
                (activity as MainActivity).showSnackbar(getString(R.string.play_button_submit_bet))
            }
            R.string.play_button_flip_tails -> {
                playViewModel.isHeads = false
                playViewModel.updateGameState()
                (activity as MainActivity).showSnackbar(getString(R.string.play_button_submit_bet))
            }
            R.string.play_button_pending -> {
                //playViewModel.updateGameState()
                //settle button is not clickable when pending
            }
            R.string.play_button_settle -> {
                playViewModel.updateGameState()
                (activity as MainActivity).showSnackbar(getString(R.string.play_button_settle_wait))
            }
        }
        return true
    }

    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            return null
        }
        return jsonString
    }
}