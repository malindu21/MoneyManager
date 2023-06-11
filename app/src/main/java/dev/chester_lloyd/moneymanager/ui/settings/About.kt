package dev.chester_lloyd.moneymanager.ui.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.chester_lloyd.moneymanager.MainActivity
import dev.chester_lloyd.moneymanager.R
import kotlinx.android.synthetic.main.activity_about.*

/**
 * An [AppCompatActivity] subclass to show details about the app.
 *
 * @author Chester Lloyd
 * @since 1.4
 */
class About : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.hideInMultitasking(window, applicationContext)
        setContentView(R.layout.activity_about)

        // Setup toolbar name and show a back button
        this.supportActionBar?.title = getString(R.string.about)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Open email launcher
        ivEmail.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)

            emailIntent.type = "plain/text"
            emailIntent.putExtra(
                Intent.EXTRA_EMAIL, arrayOf(
                    applicationContext.resources.getText(
                        R.string.author_email
                    ) as String
                )
            )
            emailIntent.putExtra(
                Intent.EXTRA_SUBJECT, applicationContext.resources.getText(
                    R.string.app_name
                )
            )

            startActivity(
                Intent.createChooser(
                    emailIntent, applicationContext.resources.getText(
                        R.string.email_icon
                    )
                )
            )
        }

        // Open website link
        ivWebsite.setOnClickListener {
            val websiteIntent = Intent(Intent.ACTION_VIEW)

            websiteIntent.data = Uri.parse(
                applicationContext.resources.getText(
                    R.string.project_url
                ) as String
            )

            startActivity(websiteIntent)
        }

        // Open GitHub link
        ivGithub.setOnClickListener {
            val githubIntent = Intent(Intent.ACTION_VIEW)

            githubIntent.data = Uri.parse(
                applicationContext.resources.getText(
                    R.string.github_url
                ) as String
            )

            startActivity(githubIntent)
        }

        // Set version number
        try {
            val packageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            tvVersion.text = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            tvVersion.visibility = View.GONE
        }
    }

    /**
     * An [onResume] method that launches the PIN code lock if enabled.
     */
    override fun onResume() {
        super.onResume()
        MainActivity.launchPinLock(this, applicationContext)
    }

    /**
     * An [onTrimMemory] method that sets the authenticated variable to false, as the app has been
     * sent to the background.
     */
    override fun onTrimMemory(level: Int) {
        MainActivity.authenticated = false
    }

    /**
     * An [onSupportNavigateUp] method that closes this activity (goes to previous page) once
     * toolbar back button is pressed.
     *
     * @return true if Up navigation completed successfully and this Activity was finished, false
     * otherwise.
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}