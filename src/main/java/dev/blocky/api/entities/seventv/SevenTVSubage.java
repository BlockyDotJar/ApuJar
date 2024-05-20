/**
 * ApuJar - A useful bot for Twitch with many cool utility features.
 * Copyright (C) 2024 BlockyDotJar (aka. Dominic R.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.blocky.api.entities.seventv;

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Date;

public class SevenTVSubage
{
    @SerializedName("active")
    boolean isActive;

    @SerializedName("age")
    int age;

    @SerializedName("started_at")
    Date startedAt;

    @SerializedName("unit")
    String unit;

    @SerializedName("status")
    String status;

    @SerializedName("renew")
    boolean willRenew;

    @SerializedName("end_at")
    Date endAt;

    @SerializedName("gifted_by")
    String giftedBy;

    public boolean isActive()
    {
        return isActive;
    }

    public int getAge()
    {
        return age;
    }

    @Nullable
    public Date getStartedAt()
    {
        return startedAt;
    }

    @Nullable
    public String getUnit()
    {
        return unit;
    }

    @Nullable
    public String getStatus()
    {
        return status;
    }

    public boolean willRenew()
    {
        return willRenew;
    }

    @Nullable
    public Date getEndAt()
    {
        return endAt;
    }

    @Nullable
    public String getGiftedBy()
    {
        return giftedBy;
    }

    SevenTVSubage()
    {
    }
}
