package com.example.wisatajogja.Activity

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.preference.*
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.example.wisatajogja.Data.AppConfig
import com.example.wisatajogja.Data.Constant
import com.example.wisatajogja.Data.SharedPref
import com.example.wisatajogja.Data.ThisApplication
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.PermissionUtil
import com.example.wisatajogja.Utils.Tools
import com.google.android.material.snackbar.Snackbar
import com.nostra13.universalimageloader.core.ImageLoader
import androidx.annotation.Nullable as Nullable1

class ActivitySetting : AppCompatActivity() {

    private var mDelegate: AppCompatDelegate? = null
    private var actionBar: ActionBar? = null

    private val imgloader = ImageLoader.getInstance()
    private var parent_view: View? = null
    private var sharedPref: SharedPref? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.setting_notification)
        parent_view = findViewById<View>(android.R.id.content)

        sharedPref = SharedPref(applicationContext)

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_ringtone)))

        val notifPref = findPreference(getString(R.string.pref_key_notif)) as Preference
        val resetCachePref = findPreference(getString(R.string.pref_key_reset_cache)) as Preference
        val themePref = findPreference(getString(R.string.pref_key_theme)) as Preference
        val ratePref = findPreference("key_rate") as Preference
        val aboutPref = findPreference("key_about") as Preference
        val prefTerm = findPreference(getString(R.string.pref_title_term)) as Preference

        if (!AppConfig.THEME_COLOR) {
            val categoryOthers =
                findPreference(getString(R.string.pref_category_display)) as PreferenceCategory
            categoryOthers.removePreference(themePref)
        }

        if (!PermissionUtil.isStorageGranted(this)) {
            val prefCat =
                findPreference(getString(R.string.pref_category_notif)) as PreferenceCategory
            prefCat.title = Html.fromHtml(
                "<b>" + getString(R.string.pref_category_notif) + "</b><br>" + getString(
                    R.string.grant_permission_account_storage
                )
            )
            notifPref.isEnabled = false
        }

        resetCachePref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val builder = AlertDialog.Builder(this@ActivitySetting)
            builder.setTitle(getString(R.string.dialog_confirm_title))
            builder.setMessage(getString(R.string.message_clear_image_cache))
            builder.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    imgloader.clearDiskCache()
                    imgloader.clearMemoryCache()
                    Snackbar.make(
                        parent_view!!,
                        getString(R.string.message_after_clear_image_cache),
                        Snackbar.LENGTH_SHORT
                    ).show()
                })
            builder.setNegativeButton(R.string.CANCEL, null)
            builder.show()
            true
        }

        notifPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, o ->
                val flag = o as Boolean
                // analytics tracking
                ThisApplication.getInstance().trackEvent(
                    Constant.Event.NOTIFICATION.name(),
                    if (flag) "ENABLE" else "DISABLE", "-"
                )
                true
            }

        ratePref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Tools.rateAction(this@ActivitySetting)
            true
        }

        aboutPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Tools.aboutAction(this@ActivitySetting)
            true
        }

        themePref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            dialogColorChooser(this@ActivitySetting)
            // analytics tracking
            ThisApplication.getInstance().trackEvent(Constant.Event.THEME.name(), "CHANGE", "-")
            true
        }

        prefTerm.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            dialogTerm(this@ActivitySetting)
            true
        }

    }

    override fun onResume() {
        initToolbar()
        super.onResume()
    }

    fun dialogTerm(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.pref_title_term))
        builder.setMessage(activity.getString(R.string.content_term))
        builder.setPositiveButton("OK", null)
        builder.show()
    }

    private fun dialogColorChooser(activity: Activity) {

        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_color_theme)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        val list = dialog.findViewById(R.id.list_view) as ListView
        val stringArray = resources.getStringArray(R.array.arr_main_color_name)
        val colorCode = resources.getStringArray(R.array.arr_main_color_code)
        list.adapter = object :
            ArrayAdapter<String>(
                this@ActivitySetting,
                android.R.layout.simple_list_item_1,
                stringArray
            ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getView(position, convertView, parent) as TextView
                textView.width = ViewGroup.LayoutParams.MATCH_PARENT
                textView.height = ViewGroup.LayoutParams.MATCH_PARENT
                textView.setBackgroundColor(Color.parseColor(colorCode[position]))
                textView.setTextColor(Color.WHITE)
                return textView
            }
        }

        list.onItemClickListener = AdapterView.OnItemClickListener { av, v, pos, id ->
            sharedPref!!.setThemeColor(colorCode[pos])
            dialog.dismiss()
            onResume()
        }

        //global.setIntPref(global.I_PREF_COLOR, global.I_KEY_COLOR, getResources().getColor(R.color.red));
        dialog.show()
        dialog.window!!.attributes = lp
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is changed.
     */
    private fun bindPreferenceSummaryToValue(preference: Preference) {
        // Set the listener to watch for value changes.
        preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
            preference,
            PreferenceManager.getDefaultSharedPreferences(preference.context).getString(
                preference.key,
                ""
            )
        )
    }

    /**
     * A preference value change listener that updates the preference's summary to reflect its new value.
     */
    private val sBindPreferenceSummaryToValueListener =
        Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in the preference's 'entries' list.
                val index = preference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                    if (index >= 0) preference.entries[index] else null
                )

            } else if (preference is RingtonePreference) {
                // For ringtone preferences, look up the correct display value using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent)

                } else {
                    val ringtone =
                        RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue))

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null)
                    } else {
                        // Set the summary to reflect the new ringtone display name.
                        val name = ringtone.getTitle(preference.getContext())
                        preference.setSummary(name)
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's simple string representation.
                preference.summary = stringValue
            }
            true
        }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        getDelegate().onPostCreate(savedInstanceState)
    }

    private fun initToolbar() {
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        actionBar!!.setTitle(R.string.activity_title_settings)
        // for system bar in lollipop
        Tools.systemBarLolipop(this)
        Tools.setActionBarColor(this, actionBar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    /*
     * Support for Activity : DO NOT CODE BELOW ----------------------------------------------------
     */

    override fun getSupportActionBar(): ActionBar {
        return getDelegate().supportActionBar!!
    }

    override fun setSupportActionBar(@Nullable1 toolbar: Toolbar?) {
        getDelegate().setSupportActionBar(toolbar)
    }

    override fun getMenuInflater(): MenuInflater {
        return getDelegate().menuInflater
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        getDelegate().setContentView(layoutResID)
    }

    override fun setContentView(view: View) {
        getDelegate().setContentView(view)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        getDelegate().setContentView(view, params)
    }

    override fun addContentView(view: View, params: ViewGroup.LayoutParams) {
        getDelegate().addContentView(view, params)
    }

    override fun onPostResume() {
        super.onPostResume()
        getDelegate().onPostResume()
    }

    override fun onTitleChanged(title: CharSequence, color: Int) {
        super.onTitleChanged(title, color)
        getDelegate().setTitle(title)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        getDelegate().onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        super.onStop()
        getDelegate().onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        getDelegate().onDestroy()
    }

    override fun invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu()
    }

    override fun getDelegate(): AppCompatDelegate {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null)
        }
        return mDelegate!!
    }
}
