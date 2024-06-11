package com.algorand.example.coinflipper.ui

import android.app.ActionBar
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.algorand.example.coinflipper.R
import com.algorand.example.coinflipper.databinding.ActivityMainBinding
import com.algorand.example.coinflipper.ui.common.BaseActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_nav_main)
        val appBarConfiguration =
            AppBarConfiguration(
                setOf(
                    R.id.navigation_play,
                    R.id.navigation_account,
                ),
            )
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigation.setupWithNavController(navController)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_play -> {
                    navController.navigate(R.id.navigation_play)
                }
                R.id.navigation_account -> {
                    navController.navigate(R.id.navigation_account)
                }
            }
            return@setOnItemSelectedListener true
        }

        supportActionBar?.title = getString(R.string.app_name_long)
    }

    fun showSnackbar(str: String) {
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
