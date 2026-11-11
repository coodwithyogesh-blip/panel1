package com.momsafe.duallock

import android.app.Activity
import android.app.ActivityOptions
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.momsafe.duallock.admin.AdminReceiver

class MainActivity : Activity() {
    private lateinit var pins: PinStore
    private lateinit var dpm: DevicePolicyManager
    private lateinit var admin: ComponentName
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pins = PinStore(this)
        dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        admin = ComponentName(this, AdminReceiver::class.java)
        showEntry()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showEntry()
    }

    private fun showEntry() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
        }
        setContentView(root)

        addText("DUAL LOCK", 28f)
        addText("Owner Mode  •  Mom Safe Mode", 15f)

        if (!pins.isConfigured()) {
            addText("First-time setup: create two different PINs.", 16f)
            val owner = pinField("Owner PIN (4–12 digits)")
            val mom = pinField("Mom PIN (4–12 digits)")
            addButton("Save PINs") {
                val a = owner.text.toString()
                val b = mom.text.toString()
                if (a.length !in 4..12 || !a.all(Char::isDigit) ||
                    b.length !in 4..12 || !b.all(Char::isDigit) || a == b
                ) {
                    toast("Use two different PINs, each 4–12 digits.")
                } else {
                    pins.save(a, b)
                    toast("PINs saved. Enter a PIN to continue.")
                    showEntry()
                }
            }
        } else {
            addText("Enter Owner PIN or Mom PIN", 17f)
            val pin = pinField("Enter PIN")
            addButton("Unlock") {
                val value = pin.text.toString()
                when {
                    pins.isOwner(value) -> enterOwnerMode()
                    pins.isMom(value) -> enterMomMode()
                    else -> toast("Wrong PIN")
                }
            }
        }
    }

    private fun enterMomMode() {
        if (!dpm.isDeviceOwnerApp(packageName)) {
            toast("Device Owner setup is required before Mom Safe can lock the phone.")
            addText("Provisioning required: see README.md in the GitHub project.", 14f)
            return
        }

        val youtube = packageManager.getLaunchIntentForPackage("com.google.android.youtube")
        val whatsapp = packageManager.getLaunchIntentForPackage("com.whatsapp")
        val dialer = packageManager.resolveActivity(
            Intent(Intent.ACTION_DIAL),
            PackageManager.MATCH_DEFAULT_ONLY
        )?.activityInfo?.packageName

        if (youtube == null || whatsapp == null || dialer.isNullOrBlank()) {
            toast("Install YouTube and WhatsApp, and set a default Phone app first.")
            return
        }

        try {
            val allowed = arrayOf(packageName, youtube.component!!.packageName, dialer, whatsapp.component!!.packageName)
                .distinct().toTypedArray()
            dpm.setLockTaskPackages(admin, allowed)
            dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_HOME)

            val homeFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            dpm.addPersistentPreferredActivity(
                admin,
                homeFilter,
                ComponentName(this, LimitedHomeActivity::class.java)
            )

            val intent = Intent(this, LimitedHomeActivity::class.java)
            val options = ActivityOptions.makeBasic().apply { setLockTaskEnabled(true) }
            startActivity(intent, options.toBundle())
            finish()
        } catch (e: SecurityException) {
            toast("Kiosk permission error: ${e.message ?: "Device Owner not active"}")
        } catch (e: Exception) {
            toast("Could not start Mom Safe: ${e.message ?: "unknown error"}")
        }
    }

    private fun enterOwnerMode() {
        if (dpm.isDeviceOwnerApp(packageName)) {
            try {
                dpm.clearPackagePersistentPreferredActivities(admin, packageName)
                dpm.setLockTaskPackages(admin, emptyArray())
                stopLockTask()
            } catch (e: Exception) {
                toast("Owner unlock warning: ${e.message ?: "policy could not be cleared"}")
            }
        }
        toast("Owner Mode")
        startActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    private fun pinField(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            textSize = 18f
            setSingleLine(true)
            root.addView(this, matchWidth())
        }
    }

    private fun addText(value: String, size: Float) {
        root.addView(TextView(this).apply {
            text = value
            textSize = size
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 12)
        }, matchWidth())
    }

    private fun addButton(value: String, action: () -> Unit) {
        root.addView(Button(this).apply {
            text = value
            setOnClickListener { action() }
        }, matchWidth())
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
