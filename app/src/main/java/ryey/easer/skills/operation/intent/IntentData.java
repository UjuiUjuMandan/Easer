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

package ryey.easer.skills.operation.intent;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ryey.easer.Utils;
import ryey.easer.skills.reusable.Extras;

/**
 * This class represents the data of an Intent.
 * The reason we don't use Intent directly is because Intent may have Bundle
 * as extras, but the type information for data inside the Bundle is unknown.
 *
 * TODO: Make fields final?
 * TODO: In the future, this class should be the common class for all skills related to Intent.
 */
public class IntentData implements Parcelable {

    @Nullable String target_package;
    @Nullable String target_class;
    String action;
    List<String> category;
    String type;
    Uri data;
    @Nullable
    Extras extras;

    IntentData() {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof IntentData))
            return false;
        if (!Utils.nullableEqual(target_package, ((IntentData) obj).target_package))
            return false;
        if (!Utils.nullableEqual(target_class, ((IntentData) obj).target_class))
            return false;
        if (!Utils.nullableEqual(action, ((IntentData) obj).action))
            return false;
        if (!Utils.nullableEqual(category, ((IntentData) obj).category))
            return false;
        if (!Utils.nullableEqual(type, ((IntentData) obj).type))
            return false;
        if (!Utils.nullableEqual(data, ((IntentData) obj).data))
            return false;
        if (!Utils.nullableEqual(extras, ((IntentData) obj).extras))
            return false;
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("package:%s class:%s action:%s category:%s type:%s data:%s",
                target_package, target_class, action, category, type, data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(target_package);
        dest.writeString(target_class);
        dest.writeString(action);
        dest.writeStringList(category);
        dest.writeString(type);
        dest.writeParcelable(data, 0);
        dest.writeParcelable(extras, 0);
    }

    public static final Parcelable.Creator<IntentData> CREATOR
            = new Parcelable.Creator<IntentData>() {
        public IntentData createFromParcel(Parcel in) {
            return new IntentData(in);
        }

        public IntentData[] newArray(int size) {
            return new IntentData[size];
        }
    };

    private IntentData(Parcel in) {
        target_package = in.readString();
        target_class = in.readString();
        action = in.readString();
        List<String> cat = new ArrayList<>();
        in.readStringList(cat);
        if (cat.size() > 0)
            category = cat;
        type = in.readString();
        data = in.readParcelable(Uri.class.getClassLoader());
        extras = in.readParcelable(Extras.class.getClassLoader());
    }
}
