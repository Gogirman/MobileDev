package com.example.googlemap

import com.google.android.gms.maps.model.LatLng
import io.realm.RealmList
import io.realm.RealmObject

open class MarkerModel(): RealmObject() {
    public var latitude = 0.0
    public var longitude = 0.0
    public var pictures: RealmList<String> = RealmList()

    public fun getPosition():LatLng
    {
        return LatLng(latitude, longitude)
    }
    public fun setPosition(latLng: LatLng)
    {
        latitude = latLng.latitude
        longitude = latLng.longitude
    }
}