/**
 * Copyright 2016 - 2017
 *
 * Daniel Hintze <daniel.hintze></daniel.hintze>@fhdw.de>
 * Sebastian Scholz <sebastian.scholz></sebastian.scholz>@fhdw.de>
 * Rainhard D. Findling <rainhard.findling></rainhard.findling>@fh-hagenberg.at>
 * Muhammad Muaaz <muhammad.muaaz></muhammad.muaaz>@usmile.at>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.uwaterloo.mrafuse.context.api.model

import android.os.Parcel
import android.os.Parcelable

class StatusDataPrivacy : Parcelable {
    enum class Status {
        TRAINING, OPERATIONAL, UNKNOWN
    }

    var status: Status? = Status.UNKNOWN
        private set
    var privacy = 0
        private set

    constructor() {}

    fun status(status: Status?): StatusDataPrivacy {
        this.status = status
        return this
    }

    fun privacy(privacy: Int): StatusDataPrivacy {
        this.privacy = privacy
        return this
    }

    private constructor(parcel: Parcel) {
        val parcelableVersion = parcel.readInt()
        val parcelableSize = parcel.readInt()
        // Version 1 below
        if (parcelableVersion >= 1) {
            status = Status.valueOf(parcel.readString()!!)
            privacy = parcel.readInt()
        }
        // Version 2 below

        // Skip any fields we don't know about. For example, if our current
        // version's
        // PARCELABLE_SIZE is 6 and the input parcelableSize is 12, skip the 6
        // fields we
        // haven't read yet (from above) since we don't know about them.
        parcel.setDataPosition(parcel.dataPosition() + (PARCELABLE_SIZE - parcelableSize))
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        /**
         * NOTE: When adding fields in the process of updating this API, make sure to bump [.PARCELABLE_VERSION] and modify
         * [.PARCELABLE_SIZE].
         */
        parcel.writeInt(PARCELABLE_VERSION)
        parcel.writeInt(PARCELABLE_SIZE)

        // Version 1 below
        parcel.writeString(status.toString())
        parcel.writeInt(privacy)

        // Version 2 below
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        val temp: Long
        temp = java.lang.Double.doubleToLongBits(privacy.toDouble())
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        result = prime * result + if (status == null) 0 else status.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        val result = other as StatusDataPrivacy
        return if (java.lang.Double.doubleToLongBits(privacy.toDouble()) != java.lang.Double.doubleToLongBits(result.privacy.toDouble())) false else status == result.status
    }

    fun clean() {
        if (status == null) status = Status.UNKNOWN
    }

    companion object {
        const val PARCELABLE_VERSION = 1

        /**
         * The number of fields in this version of the parcelable.
         */
        const val PARCELABLE_SIZE = 2

        /**
         * @see Parcelable
         */
        @JvmField
        val CREATOR: Parcelable.Creator<StatusDataPrivacy?> = object : Parcelable.Creator<StatusDataPrivacy?> {
            override fun createFromParcel(`in`: Parcel): StatusDataPrivacy? {
                return StatusDataPrivacy(`in`)
            }

            override fun newArray(size: Int): Array<StatusDataPrivacy?> {
                return arrayOfNulls(size)
            }
        }
    }
}