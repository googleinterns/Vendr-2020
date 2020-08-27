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

package com.google.sps.utility;

import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.datastore.GeoPt;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class GeoHashTest {
  // Distances
  private final int DISTANCE_1KM = 1000;
  private final int DISTANCE_MAX = 5000 * 1000;
  private final int DISTANCE_MIN = 0;

  // Geohash precision
  private final int GEOHASH_MAX_PRECISION = 9;
  private final int GEOHASH_MIN_PRECISION = 1;
  private final int GEOHASH_PRECISION_1KM = 6;

  // Mock Places
  private final GeoPt PLACE_INITIAL = new GeoPt(0, 0);
  private final GeoPt PLACE_0M_FROM_INITIAL = new GeoPt(0, 0);
  private final GeoPt PLACE_50M_FROM_INITIAL = new GeoPt(0, 0.00055556f);
  private final GeoPt PLACE_1KM_FROM_INITIAL = new GeoPt(0.00083333f, 0.00888889f);
  private final GeoPt PLACE_20KM_FROM_INITIAL = new GeoPt(0.00888889f, 0.17888889f);
  private final GeoPt PLACE_5000KM_FROM_INITIAL = new GeoPt(4.25138889f, 44.80750000f);

  // Expected Geohash results
  private final String GEOHASH_PLACE_INITIAL = "s00000000";
  private final String GEOHASH_PLACE_50M = "s0000002h";
  private final String GEOHASH_PLACE_1KM = "s00000nkz";
  private final String GEOHASH_PLACE_20KM = "s000h1d7b";
  private final String GEOHASH_PLACE_5000KM = "sbzb5tgy4";

  // Geohash to query results
  private final List<String> GEOHASH_INITIAL_PLACE_LIST = Arrays.asList("s00001", "s00003", "s00002",
          "kpbpbr", "kpbpbp", "7zzzzz", "ebpbpb", "ebpbpc", "s00000");
  private final List<String> GEOHASH_MIN_PRECISION_LIST = Arrays.asList("u", "v", "t", "m", "k", "7", "e", "g", "s");
  private final List<String> GEOHASH_MAX_PRECISION_LIST = Arrays.asList("s00000002", "s00000003", "s00000001",
          "kpbpbpbpc", "kpbpbpbpb", "7zzzzzzzz", "ebpbpbpbp", "ebpbpbpbr", "s00000000");

  // Instance of the GeoHash class.
  private GeoHash geoHash = new GeoHash();

  // Test of precision calculation for 1KM.
  @Test
  public void determinePrecisionNormal() {
    Assert.assertEquals(GEOHASH_PRECISION_1KM, geoHash.determinePrecision(DISTANCE_1KM));
  }

  // Test of precision calculation for 5000KM.
  @Test
  public void determinePrecisionMax() {
    Assert.assertEquals(GEOHASH_MIN_PRECISION, geoHash.determinePrecision(DISTANCE_MAX));
  }

  // Test of precision calculation for 0M.
  @Test
  public void determinePrecisionMin() {
    Assert.assertEquals(GEOHASH_MAX_PRECISION, geoHash.determinePrecision(DISTANCE_MIN));
  }

  @Test
  public void encodeInitialPlace() {
    Assert.assertEquals(GEOHASH_PLACE_INITIAL,
            geoHash.encodeVendor(PLACE_INITIAL.getLatitude(), PLACE_INITIAL.getLongitude()));
  }

  @Test
  public void encodeFarthestPlace() {
    Assert.assertEquals(GEOHASH_PLACE_5000KM,
            geoHash.encodeVendor(PLACE_5000KM_FROM_INITIAL.getLatitude(), PLACE_5000KM_FROM_INITIAL.getLongitude()));
  }

  @Test
  public void encodeNormal() {
    Assert.assertEquals(GEOHASH_PLACE_1KM,
            geoHash.encodeVendor(PLACE_1KM_FROM_INITIAL.getLatitude(), PLACE_1KM_FROM_INITIAL.getLongitude()));
  }

  @Test
  public void getGeohashesToQueryPrecisionSix() {
    Assert.assertThat(GEOHASH_INITIAL_PLACE_LIST,
            containsInAnyOrder(
                    geoHash.getHashesToQuery(PLACE_INITIAL.getLatitude(), PLACE_INITIAL.getLongitude(), DISTANCE_1KM)
                            .toArray()
            ));
  }

  @Test
  public void getGeohashesToQueryPrecisionMin() {
    Assert.assertThat(GEOHASH_MIN_PRECISION_LIST,
            containsInAnyOrder(
                    geoHash.getHashesToQuery(PLACE_INITIAL.getLatitude(), PLACE_INITIAL.getLongitude(), DISTANCE_MAX)
                            .toArray()
            ));
  }

  @Test
  public void getGeohashesToQueryPrecisionMax() {
    Assert.assertThat(GEOHASH_MAX_PRECISION_LIST,
            containsInAnyOrder(
                    geoHash.getHashesToQuery(PLACE_INITIAL.getLatitude(), PLACE_INITIAL.getLongitude(), DISTANCE_MIN)
                            .toArray()
            ));
  }
}