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

package ryey.easer.skills.event.notification;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class NotificationEventListenerService extends NotificationListenerService {
    private static final String ACTION_LISTEN = "ryey.easer.skills.event.notification.action.LISTEN";
    private static final String ACTION_CANCEL = "ryey.easer.skills.event.notification.action.CANCEL";
    private static final String EXTRA_DATA = "ryey.easer.skills.event.notification.extra.DATA";
    private static final String EXTRA_URI = "ryey.easer.skills.event.notification.extra.URI";

    List<CompoundData> dataWithSlotUriList = new ArrayList<>();

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null)
                return;
            if (!(action.equals(ACTION_LISTEN) || action.equals(ACTION_CANCEL)))
                return;
            Logger.d("broadcast received");
            NotificationEventData eventData = intent.getParcelableExtra(EXTRA_DATA);
            Uri uri = intent.getParcelableExtra(EXTRA_URI);
            if (action.equals(ACTION_LISTEN)) {
                addListenToNotification(eventData, uri);
            } else if (action.equals(ACTION_CANCEL)) {
                delListenToNotification(eventData, uri);
            }
        }
    };

    static void listen(Context context,
                       NotificationEventData eventData,
                       Uri uri) {
        Intent intent = new Intent(ACTION_LISTEN);
        intent.putExtra(EXTRA_DATA, eventData);
        intent.putExtra(EXTRA_URI, uri);
        Logger.d("informing 'listen'");
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    static void cancel(Context context,
                       NotificationEventData eventData,
                       Uri uri) {
        Intent intent = new Intent(ACTION_CANCEL);
        intent.putExtra(EXTRA_DATA, eventData);
        intent.putExtra(EXTRA_URI, uri);
        Logger.d("informing 'listen'");
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    private static boolean match(StatusBarNotification sbn, String t_app, String t_title, String t_content) {
        Logger.d("app: <%s> <%s>", t_app, sbn.getPackageName());
        if (t_app != null) {
            if (!t_app.equals(sbn.getPackageName()))
                return false;
        }
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getCharSequence(Notification.EXTRA_TITLE) != null
                ? extras.getCharSequence(Notification.EXTRA_TITLE).toString()
                : null;
        String contentText = extras.getCharSequence(Notification.EXTRA_TEXT) != null
                ? extras.getCharSequence(Notification.EXTRA_TEXT).toString()
                : null;
        if (t_title != null) {
            if (title == null || !title.contains(t_title))
                return false;
        }
        if (t_content != null) {
            if (contentText == null || !contentText.contains(t_content))
                return false;
        }
        return true;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        for (CompoundData dataWithSlotUri : dataWithSlotUriList) {
            NotificationEventData eventData = dataWithSlotUri.notificationEventData;
            Intent intent = match(sbn, eventData.app, eventData.title, eventData.content)
                    ? NotificationSlot.NotifyIntentPrototype.obtainPositiveIntent(dataWithSlotUri.slotUri)
                    : NotificationSlot.NotifyIntentPrototype.obtainNegativeIntent(dataWithSlotUri.slotUri);
            intent.putExtra(NotificationEventData.AppDynamics.id, sbn.getPackageName());
            Bundle extras = sbn.getNotification().extras;
            String title = extras.getCharSequence(Notification.EXTRA_TITLE) != null
                    ? extras.getCharSequence(Notification.EXTRA_TITLE).toString()
                    : null;
            String contentText = extras.getCharSequence(Notification.EXTRA_TEXT) != null
                    ? extras.getCharSequence(Notification.EXTRA_TEXT).toString()
                    : null;
            intent.putExtra(NotificationEventData.TitleDynamics.id, title);
            intent.putExtra(NotificationEventData.ContentDynamics.id, contentText);
            sendBroadcast(intent);
        }
        super.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    @Override
    public void onCreate() {
        Logger.i("NotificationEventListenerService onCreate()");
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LISTEN);
        filter.addAction(ACTION_CANCEL);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        Logger.i("NotificationEventListenerService onDestroy()");
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
        if (dataWithSlotUriList.size() > 0) {
            Logger.w("Listener to notifications not cleaned up completely: %d left",
                    dataWithSlotUriList.size());
        }
    }

    private void addListenToNotification(
            NotificationEventData notificationEventData,
            Uri slotUri) {
        dataWithSlotUriList.add(new CompoundData(
                notificationEventData,
                slotUri));
    }

    private void delListenToNotification(
            NotificationEventData notificationEventData,
            Uri slotUri) {
        CompoundData compoundData = new CompoundData(
                notificationEventData,
                slotUri);
        dataWithSlotUriList.remove(compoundData);
    }

    private static class CompoundData {
        final NotificationEventData notificationEventData;
        final Uri slotUri;
        private CompoundData(
                NotificationEventData notificationEventData,
                Uri slotUri) {
            this.notificationEventData = notificationEventData;
            this.slotUri = slotUri;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof CompoundData))
                return false;
            return notificationEventData.equals(((CompoundData) obj).notificationEventData) &&
                    slotUri.equals(((CompoundData) obj).slotUri);
        }
    }
}
