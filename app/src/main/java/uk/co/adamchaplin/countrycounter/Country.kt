package uk.co.adamchaplin.countrycounter

import android.os.Parcel
import android.os.Parcelable

class Country(private var continentName: String, var countryName: String, var colour: Int, var unsavedVisited: Boolean) : Comparable<Country>, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun compareTo(other: Country): Int {
        if(this.countryName > other.countryName) return 1
        if(this.countryName < other.countryName) return -1
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(continentName)
        parcel.writeString(countryName)
        parcel.writeInt(colour)
        parcel.writeByte(if (unsavedVisited) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Country> {
        override fun createFromParcel(parcel: Parcel): Country {
            return Country(parcel)
        }

        override fun newArray(size: Int): Array<Country?> {
            return arrayOfNulls(size)
        }
    }

}