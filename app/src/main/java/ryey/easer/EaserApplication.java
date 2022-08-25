/*
 * Copyright (c) 2016 - 2019 Rui Zhao <renyuneyun@gmail.com>
 *
 * This file is part of Easer.
 *
 * Easer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Easer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Easer.  If not, see <http://www.gnu.org/licenses/>.
 */

package ryey.easer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDexApplication;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.Logger;
import com.zeugmasolutions.localehelper.LocaleHelperApplicationDelegate;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.DialogConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;

import ryey.easer.core.log.ActivityLogService;

public class EaserApplication extends MultiDexApplication {

    static final int[] THEME_NIGHT_MODE = {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES,
    };
    private final LocaleHelperApplicationDelegate localeAppDelegate = new LocaleHelperApplicationDelegate();

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.addLogAdapter(new AndroidLogAdapter());

        if (SettingsUtils.logging(this)) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Logger.addLogAdapter(new DiskLogAdapter());
            } else {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putBoolean(getString(R.string.key_pref_logging), false)
                        .apply();
            }
        }

        final int setting_theme = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(getString(R.string.key_pref_theme), "0"));
        AppCompatDelegate.setDefaultNightMode(THEME_NIGHT_MODE[setting_theme]);

        ContextCompat.startForegroundService(this, new Intent(this, ActivityLogService.class));

        Logger.log(Logger.ASSERT, null, "======Easer started======", null);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        localeAppDelegate.onConfigurationChanged(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(localeAppDelegate.attachBaseContext(base));

        ACRA.init(this, new CoreConfigurationBuilder()
                .withPluginConfigurations(
                        new MailSenderConfigurationBuilder()
                                .withMailTo("bug-report@ryey.icu")
                                .withReportAsFile(true)
                                .build(),
                        new DialogConfigurationBuilder()
                                .withText(getString(R.string.prompt_acra_dialog_text))
                                .build()
                )
        );
    }
}
