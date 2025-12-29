package com.example.androidapp.utils

object Util {
    //longitude , latitude je vrstni red
    private val SLOVENIA_POLYGON = listOf(
        Pair(13.38, 46.88), // NW
        Pair(14.10, 46.88),
        Pair(15.30, 46.80),
        Pair(16.61, 46.50), // NE
        Pair(16.61, 45.42), // SE
        Pair(15.60, 45.42),
        Pair(14.20, 45.50),
        Pair(13.38, 45.70)  // SW
    )

    private fun isPointInPolygon(
        point: Pair<Double, Double>,
        polygon: List<Pair<Double, Double>>
    ): Boolean {
        var intersectCount = 0
        for (i in polygon.indices) {
            val j = (i + 1) % polygon.size

            val xi = polygon[i].first
            val yi = polygon[i].second
            val xj = polygon[j].first
            val yj = polygon[j].second

            val px = point.first
            val py = point.second

            val intersect = ((yi > py) != (yj > py)) &&
                    (px < (xj - xi) * (py - yi) / (yj - yi) + xi)

            if (intersect) intersectCount++
        }
        return intersectCount % 2 == 1
    }


    fun getRandomSloveniaLatLng(): Pair<Double, Double> {
        val minLat = 45.42
        val maxLat = 46.88
        val minLng = 13.38
        val maxLng = 16.61

        repeat(1000) {
            val lat = minLat + Math.random() * (maxLat - minLat)
            val lng = minLng + Math.random() * (maxLng - minLng)

            val point = Pair(lng, lat)
            if (isPointInPolygon(point, SLOVENIA_POLYGON)) {
                return Pair(lat, lng)
            }
        }

        return Pair(46.05, 14.51)
    }


    fun convertToMin(days: Int, hours: Int, min: Int): Int {
        return days * 24 * 60 + hours * 60 + min
    }
}