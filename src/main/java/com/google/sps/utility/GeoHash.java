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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class GeoHash {

  /**
   * Encodes latitude and longitude to geoHash given determined distance to set the
   * precision of the hash and creates a list of geohashed neighbour cells in order
   * to create a valid query list.
   * @param lat {double} Latitude in degrees.
   * @param lng {double} longitude in degrees.
   * @param distance {int} distance to determine precision in meters
   * @return {List<String>} List of adjacent geohashes of supplied latitude/longitude.
   */
  public static List<String> getHashesToQuery(double lat, double lng, int distance) {
    String geohash = encode(lat, lng, distance);
    List<String> hashesToQuery = neighbourCells(geohash);

    // Append center geohash cell (current)
    hashesToQuery.add(geohash);

    return hashesToQuery;
  }

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
    int index = 0; // index into BASE32 map.
    int bit = 0; // each char holds 5 bits.
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
    int precision = 0;
    int initialDistance = COMMONS.MAX_GEOHASH_HEIGHT;
    while(initialDistance >= distance && precision < COMMONS.MAX_GEOHASH_PRECISION) {
      if (precision % 2 == 0) initialDistance /= 4;
      else initialDistance /= 8;
      precision++;
    }
    return precision;
  }

  /**
   * Determines adjacent cell in given direction,
   * @param geohash - Cell to which adjacent cell is required,
   * @param direction - Direction from geohash (N/S/E/W).
   * @return {String} GeoHash of adjacent cell,
   */
  private static String adjacentCell(String geohash, char direction) {
    geohash = geohash.toLowerCase();
    direction = Character.toLowerCase(direction);

    if (geohash.length() == 0) return "";
    if ("nsew".indexOf(direction) == -1) return "";

    HashMap<Character, List<String>> neighbour = new HashMap<Character, List<String>>();
    neighbour.put('n', Arrays.asList("p0r21436x8zb9dcf5h7kjnmqesgutwvy",
            "bc01fg45238967deuvhjyznpkmstqrwx"));
    neighbour.put('s', Arrays.asList("14365h7k9dcfesgujnmqp0r2twvyx8zb",
            "238967debc01fg45kmstqrwxuvhjyznp"));
    neighbour.put('e', Arrays.asList("bc01fg45238967deuvhjyznpkmstqrwx",
            "p0r21436x8zb9dcf5h7kjnmqesgutwvy"));
    neighbour.put('w', Arrays.asList("238967debc01fg45kmstqrwxuvhjyznp",
            "14365h7k9dcfesgujnmqp0r2twvyx8zb"));

    HashMap<Character, List<String>> border = new HashMap<Character, List<String>>();
    border.put('n', Arrays.asList("prxz","bcfguvyz"));
    border.put('s', Arrays.asList("028b","0145hjnp"));
    border.put('e', Arrays.asList("bcfguvyz","prxz"));
    border.put('w', Arrays.asList("0145hjnp","028b"));

    // Last character of hash
    char lastChar = geohash.charAt(geohash.length() - 1);
    // Hash without last character
    String parent = geohash.substring(0, geohash.length() - 1);

    int type = geohash.length() % 2;

    // Check for edge-cases which don't share common prefix
    if (border.get(direction).get(type).indexOf(lastChar) != -1 && !parent.equals("")) {
      parent = adjacentCell(parent, direction);
    }

    // append letter for direction to parent
    return parent +
            COMMONS.BASE32.charAt(neighbour
                    .get(direction)
                    .get(type)
                    .indexOf(lastChar));
  }

  /**
   * Returns all 8 adjacent cells to specified geohash.
   * @param geohash {String} Geohash neighbours are required of.
   * @return List of neighbours from a geohashed cell.
   */
  public static List<String> neighbourCells(String geohash) {
    List<String> neighbours = new ArrayList<>();

    for (String direction : COMMONS.GEOHASH_DIRECTIONS) {
      if (direction.length() == 2) {
        neighbours.add(adjacentCell(
                adjacentCell(geohash, direction.charAt(0)),
                direction.charAt(1)));
      } else {
        neighbours.add(adjacentCell(geohash, direction.charAt(0)));
      }
    }
    return neighbours;
  }
}
