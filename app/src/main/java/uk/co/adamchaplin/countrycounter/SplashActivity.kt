package uk.co.adamchaplin.countrycounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val otherSharedPref = applicationContext.getSharedPreferences(getString(R.string.other_prefs_file), Context.MODE_PRIVATE)
        val completePp = otherSharedPref.getBoolean(getString(R.string.privacy_policy_complete), false)

        if(completePp) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}