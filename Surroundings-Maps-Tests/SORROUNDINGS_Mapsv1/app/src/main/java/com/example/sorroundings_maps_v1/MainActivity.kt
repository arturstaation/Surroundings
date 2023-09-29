package com.example.sorroundings_maps_v1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity() {

    // Coordenada Mackenzie: -23.5480455,-46.6529449 ;
    // R. da Consolação, 930 - Consolação, São Paulo - SP, 01302-907

    // Coordenada Praça da República: -23.5435014,-46.6435142
    // "Praça da República - República, São Paulo - SP, 01045-000"
    private val places = arrayListOf(
        Place("Universidade Presbiteriana Mackenzie", LatLng(-23.5480455,-46.6529449), "R. da Consolação, 930 - Consolação, São Paulo - SP, 01302-907", 5.9f),
        Place("Praça da República", LatLng(-23.5435014,-46.6435142), "Praça da República - República, São Paulo - SP, 01045-000", 4.8f)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val map_fragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        //Só chama o mapa quando o mapa estiver pronto/renderizado
        map_fragment.getMapAsync { googleMap ->
            addMarkers(googleMap)
        }
    }

    private fun addMarkers(googleMap: GoogleMap){
        places.forEach {place ->
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .title(place.name)
                    .snippet(place.address)
                    .position(place.latLng)
                    .icon(
                        BitMapHelper.vectorToBitmap(this, R.drawable.baseline_pin_drop_24, ContextCompat.getColor(this, com.google.android.material.R.color.material_deep_teal_200))
                    )
            )
        }
    }
}

data class Place(
    val name: String,
    val latLng: LatLng,
    val address: String,
    val rating: Float
)