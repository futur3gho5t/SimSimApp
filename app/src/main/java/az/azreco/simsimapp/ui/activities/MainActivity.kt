package az.azreco.simsimapp.ui.activities

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import az.azreco.simsimapp.R
import az.azreco.simsimapp.exoplayer.ExoListener
import az.azreco.simsimapp.exoplayer.TestPlayer
import az.azreco.simsimapp.exoplayer.WildExoPlayer
import az.azreco.simsimapp.scenario.HomeScenario
import az.azreco.simsimapp.service.SimSimService
import az.azreco.simsimapp.ui.fragments.MyDialogFragment
import az.azreco.simsimapp.ui.viewmodels.MainViewModel
import az.azreco.simsimapp.util.SpeechLiveData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ExoListener {

//    @Inject
//    lateinit var exoPlayer: ExxoPlayer

    @Inject
    lateinit var homeScenario: HomeScenario

//    @Inject
//    lateinit var exoPlayer: WildExoPlayer

    private val permissonAll = 1
    private val permissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE
    )

    private val viewModel: MainViewModel by viewModels()

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        exoPlayer.initPlayer()

        setSupportActionBar(toolbarMain)


        if (!checkPermissions(this, permissions.toString())) {
            ActivityCompat.requestPermissions(this, permissions, permissonAll)
        }
        bottomNavigationView.background = null
        bottomNavigationView.menu.apply {
            getItem(2).isEnabled = false
        }
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.subscribeDetailFragment -> {
                        toolbarMain.setNavigationIcon(R.drawable.ic_back_arrow)
                        toolbarMain.setNavigationOnClickListener {
                            onBackPressed()
                        }
                        supportActionBar?.setIcon(null)
                        bottomAppBar.visibility = View.INVISIBLE
                        fabStartService.visibility = View.INVISIBLE
                    }
                    else -> {
                        toolbarMain.apply {
                            navigationIcon = null
                        }
                        supportActionBar?.setIcon(R.drawable.ic_idrak_logo)
                        bottomAppBar.visibility = View.VISIBLE
                        fabStartService.visibility = View.VISIBLE
                    }
                }
            }

        fabStartService.setOnClickListener {

            lifecycleScope.launch {
                Log.d("EXOPLAYER","First")
                TestPlayer(this).play(R.raw.sms_contact_name) {
                    Log.d("EXOPLAYER","Second")
                    TestPlayer(this).play(R.raw.sms_canceled) {
                        Log.d("EXOPLAYER","Third")

                    }
                }
            }

//            when (isAccessibilityOn(this, SimSimService::class.java)) {
//                true -> {
//
//            lifecycleScope.launch(Dispatchers.Main) {
//                        fabStartService.apply {
//                            isEnabled = false
//                            backgroundTintList = AppCompatResources.getColorStateList(
//                                this@MainActivity,
//                                R.color.unselected_bottom_item
//                            )
//                        }

//                exoPlayer.play(R.raw.sms_contact_name)

//                MyDialogFragment().apply {
//                    show(supportFragmentManager, "example")
//                }
//                if (!SpeechLiveData.isWorking) {
//                    withContext(Dispatchers.IO) {
//                        homeScenario.greetingUser()
//                    }
//                }

//                        fabStartService.apply {
//                            isEnabled = true
//                            backgroundTintList = AppCompatResources.getColorStateList(
//                                this@MainActivity,
//                                R.color.main
//                            )
//                        }
//            }
//                }
//                false -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
//            }

        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun checkPermissions(context: Context, vararg permissions: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (permission?.let {
                        ActivityCompat.checkSelfPermission(
                            context,
                            it
                        )
                    } != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    private fun isAccessibilityOn(
        context: Context,
        serviceClass: Class<out AccessibilityService?>
    ): Boolean {
        var accessibilityEnabled = 0
        val service = context.packageName + "/" + serviceClass.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (ignored: Settings.SettingNotFoundException) {
        }
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                colonSplitter.setString(settingValue)
                while (colonSplitter.hasNext()) {
                    val accessibilityService = colonSplitter.next()
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onEndState(lambda: () -> Unit) {
        lambda()
    }


}