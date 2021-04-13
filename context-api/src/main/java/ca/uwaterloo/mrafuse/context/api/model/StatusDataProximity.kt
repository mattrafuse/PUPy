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

class StatusDataProximity : Parcelable {
    enum class Status {
        TRAINING, OPERATIONAL, UNKNOWN
    }

    var status: Status? = Status.UNKNOWN
        private set
    var proximity = 0
        private set
    var info: String? = null
        private set

    constructor() {}

    fun info(info: String?): StatusDataProximity {
        this.info = info
        return this
    }

    fun status(status: Status?): StatusDataProximity {
        this.status = status
        return this
    }

    fun proximity(proximity: Int): StatusDataProximity {
        this.proximity = proximity
        return this
    }

    private constructor(parcel: Parcel) {
        val parcelableVersion = parcel.readInt()
        val parcelableSize = parcel.readInt()
        // Version 1 below
        if (parcelableVersion >= 1) {
            status = Status.valueOf(parcel.readString()!!)
            proximity = parcel.readInt()
        }

        // Version 2 below
        if (parcelableVersion >= 2) {
            info = parcel.readString()
        }

        // Skip any fields we don't know about. For example, if our current
        // version's PARCELABLE_SIZE is 6 and the input parcelableSize is 12, skip the 6
        // fields we haven't read yet (from above) since we don't know about them.
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
        parcel.writeInt(proximity)

        // Version 2 below
        parcel.writeString(info)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as StatusDataProximity
        if (java.lang.Double.compare(that.proximity.toDouble(), proximity.toDouble()) != 0) return false
        if (status != that.status) return false
        return if (info != null) info == that.info else that.info == null
    }

    override fun hashCode(): Int {
        var result: Int
        val temp: Long
        result = if (status != null) status.hashCode() else 0
        temp = java.lang.Double.doubleToLongBits(proximity.toDouble())
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        result = 31 * result + if (info != null) info.hashCode() else 0
        return result
    }

    fun clean() {
        if (status == null) status = Status.UNKNOWN
    }

    companion object {
        const val PARCELABLE_VERSION = 2

        /**
         * The number of fields in this version of the parcelable.
         */
        const val PARCELABLE_SIZE = 3

        /**
         * @see Parcelable
         */
        @JvmField
        val CREATOR: Parcelable.Creator<StatusDataProximity?> = object : Parcelable.Creator<StatusDataProximity?> {
            override fun createFromParcel(`in`: Parcel): StatusDataProximity {
                return StatusDataProximity(`in`)
            }

            override fun newArray(size: Int): Array<StatusDataProximity?> {
                return arrayOfNulls(size)
            }
        }
    }
}