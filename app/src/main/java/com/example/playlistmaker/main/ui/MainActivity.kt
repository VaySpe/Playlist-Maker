package com.example.playlistmaker.main.ui

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val isDark = (resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
        bottomNav.itemIconTintList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.YP_Blue), // активная
                ContextCompat.getColor(this, if (isDark) R.color.white else R.color.black)     // неактивные
            )
        )

        bottomNav.itemTextColor = bottomNav.itemIconTintList

        bottomNav.viewTreeObserver.addOnGlobalLayoutListener {
            val insets = bottomNav.rootWindowInsets
            val isKeyboardVisible = insets?.isVisible(android.view.WindowInsets.Type.ime()) == true
            bottomNav.visibility = if (isKeyboardVisible) View.GONE else View.VISIBLE
        }
    }

}
