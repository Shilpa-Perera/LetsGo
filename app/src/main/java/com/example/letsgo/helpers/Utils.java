package com.example.letsgo.helpers;

import android.content.Context;

import com.example.letsgo.models.MapInfo;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static final double EARTH_RADIUS = 6371000;

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public static List<MapInfo> filterCoordinatesByRadius(List<MapInfo> mapInfoList,
                                                          double referenceLat, double referenceLon,
                                                          double radius) {
        List<MapInfo> result = new ArrayList<>();
        for (MapInfo mapInfo : mapInfoList) {
            double distance = calculateDistance(referenceLat, referenceLon, mapInfo.getGpsLatitude(),
                    mapInfo.getGpsLongitude());
            if (distance <= radius) {
                result.add(mapInfo);
            }
        }
        return result;
    }
}
