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
package ryey.easer.skills.operation.send_notification

import android.app.NotificationChannel
import ryey.easer.skills.operation.OperationLoader
import ryey.easer.skills.operation.send_notification.SendNotificationOperationData
import ryey.easer.skills.operation.send_notification.SendNotificationLoader
import ryey.easer.commons.local_skill.ValidData
import ryey.easer.commons.local_skill.operationskill.Loader.OnResultCallback
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import ryey.easer.R
import java.util.concurrent.ThreadLocalRandom

class SendNotificationLoader(context: Context?) : OperationLoader<SendNotificationOperationData?>(
    context!!
) {
    companion object {
        private var NOTIFICATION_ID = 0

        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                NOTIFICATION_ID = ThreadLocalRandom.current().nextInt()
            }
        }
    }

    override fun _load(@ValidData data: SendNotificationOperationData, callback: OnResultCallback) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "easer_skill_operation_send_notification"
            val channelName = "Easer Send Notification Operation"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager!!.createNotificationChannel(notificationChannel)
            builder = NotificationCompat.Builder(context, channelId)
            builder.setAutoCancel(true)
        } else {
            @Suppress("DEPRECATION")
            builder = NotificationCompat.Builder(context)
                .setPriority(NotificationCompat.PRIORITY_MIN)
        }

        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(data.title)
        builder.setContentText(data.content)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
        NOTIFICATION_ID++
        callback.onResult(true)
    }
}