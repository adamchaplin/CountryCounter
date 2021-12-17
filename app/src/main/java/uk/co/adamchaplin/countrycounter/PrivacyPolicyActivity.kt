package uk.co.adamchaplin.countrycounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import uk.co.adamchaplin.countrycounter.databinding.ActivityPrivacyPolicyBinding
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

class PrivacyPolicyActivity: AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        binding.policyText.text = loadStringFromAsset(R.raw.privacy_policy)
        binding.policyButton.setOnClickListener {
            val sharedPref = applicationContext.getSharedPreferences(getString(R.string.other_prefs_file),
                Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean(resources.getString(R.string.privacy_policy_complete), true)
                apply()
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadStringFromAsset(resourceId: Int): String {
        return try {
            val inputStream: InputStream = resources.openRawResource(resourceId)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ""
        }
    }
}