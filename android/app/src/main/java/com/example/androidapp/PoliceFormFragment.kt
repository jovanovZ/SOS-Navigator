package com.example.androidapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.androidapp.databinding.FragmentPoliceFormBinding
import org.json.JSONObject
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.net.HttpURLConnection
import java.net.URL



class PoliceFormFragment : Fragment() {

    private var _binding: FragmentPoliceFormBinding? = null
    private val binding get() = _binding!!

    private var editingPoliceVehicleId: String? = null

    private lateinit var map: MapView

    private  var startMarker : Marker? = null

    private  var endMarker : Marker? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPoliceFormBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingPoliceVehicleId = arguments?.getString("event_id")

        map = binding.mapPickerPolice
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        map.controller.setZoom(15.0)
        map.controller.setCenter(GeoPoint(46.558927, 15.637981))

        //TODO Editing

        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver{
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p ?: return false

                if (startMarker == null){
                    reverseGeocodeOSM(p.latitude, p.longitude) { address ->
                        binding.etPoliceFormStartLocation.setText(address)
                        binding.etPoliceFormStartLatitude.setText(p.latitude.toString())
                        binding.etPoliceFormStartLongitude.setText(p.longitude.toString())
                    }

                    val point = GeoPoint(p.latitude, p.longitude)

                    startMarker = Marker(map)
                    startMarker?.position = point
                    startMarker?.title = "Start Position"
                    startMarker?.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )
                    map.overlays.add(startMarker)

                    return true;

                }else if(endMarker == null){
                    reverseGeocodeOSM(p.latitude, p.longitude) { address ->
                        binding.etPoliceFormEndLocation.setText(address)
                        binding.etPoliceFormEndLatitude.setText(p.latitude.toString())
                        binding.etPoliceFormEndLongitude.setText(p.longitude.toString())
                    }

                    val point = GeoPoint(p.latitude, p.longitude)

                    endMarker = Marker(map)
                    endMarker?.position = point
                    endMarker?.title = "End Position"
                    endMarker?.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )

                    map.overlays.add(endMarker)

                    return true
                }


                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                TODO("Not yet implemented")
            }

        }))





    }


    fun reverseGeocodeOSM(
        lat: Double,
        lon: Double,
        onResult: (String) -> Unit
    ) {
        Thread {
            try {
                val url =
                    "https://nominatim.openstreetmap.org/reverse" +
                            "?format=json&lat=$lat&lon=$lon"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "ProektApp")

                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                val address = json.getString("display_name")

                requireActivity().runOnUiThread {
                    onResult(address)
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    onResult("Unknown location")
                }
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}