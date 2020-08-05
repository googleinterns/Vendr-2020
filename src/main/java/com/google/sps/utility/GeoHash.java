// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// Geohash encoding/decoding and associated functions   (c) Chris Veness 2014-2019 / MIT Licence
// https://www.movable-type.co.uk/scripts/geohash.html
// Originally in JS, transcribed to Java and added TRIM function
//
// Geohash: Gustavo Niemeyer’s geocoding system.

package com.google.sps.utility;

import com.google.sps.COMMONS;

public class GeoHash {

  /**
   * Calls the function encode with distance of 0 that will set the precision
   * to the maximum (9). That value will be stored on datastore.
   * @param lat {double} latitude of the Vendor.
   * @param lng {double} longitude of the Vendor.
   * @return {String} encoded geoHashed.
   */
  public static String encodeVendor(double lat, double lng) {
    return encode(lat, lng, 0);
  }

  /**
   * Calls the function encode given client's location.
   * @param lat {double} latitude of the Client.
   * @param lng {double} longitude of the Client.
   * @return {String} encoded geoHashed String.
   */
  public static String encodeClient(double lat, double lng, int distance) {
    return encode(lat, lng, distance);
  }

  /**
   * Encodes latitude and longitude to geoHash given determined distance to set the
   * precision of the hash.
   * @param lat {double} Latitude in degrees.
   * @param lng {double} longitude in degrees.
   * @param distance {int} distance to determine precision in meters
   * @return {string} Geohash of supplied latitude/longitude.
   */
  private static String encode(double lat, double lng, int distance) {
    String geoHash = "";
    int precision = determinePrecision(distance);
    int index = 0; // index into BASE32 map
    int bit = 0; // each char holds 5 bits
    boolean evenBit = true;
    double latMin = -90, latMax = 90;
    double lngMin = -180, lngMax = 180;

    while (geoHash.length() < precision) {
      if (evenBit) {
        double lngMid = (lngMin + lngMax) / 2;
        if (lng >= lngMid) {
          index = index * 2 + 1;
          lngMin = lngMid;
        } else {
          index = index * 2;
          lngMax = lngMid;
        }
      } else {
        double latMid = (latMin + latMax) / 2;
        if (lat >= latMid) {
          index = index * 2 + 1;
          latMin = latMid;
        } else {
          index = index * 2;
          latMax = latMid;
        }
      }

      evenBit = !evenBit;

      // 5 bits gives us a character: append it and start over
      if (++bit == 5) {
        geoHash += COMMONS.BASE32.charAt(index);
        bit = 0;
        index = 0;
      }
    }

    return geoHash;
  }

  /**
   * Calculates the precision of the geohash given distance in Meters
   * @param distance {int} Distance to determine precision
   * @return {String} precision - The number of letters of the geohash
   */
  public static int determinePrecision(int distance) {
    int precision = 1;
    int initialDistance = COMMONS.MAX_GEOHASH_HEIGHT;
    while(initialDistance > distance && precision < COMMONS.MAX_GEOHASH_PRECISION) {
      if (precision % 2 == 0) initialDistance /= 8;
      else initialDistance /= 4;
      precision++;
    }
    return precision;
  }
}
