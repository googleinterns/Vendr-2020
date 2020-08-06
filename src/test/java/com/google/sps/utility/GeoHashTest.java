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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class GeoHashTest {
  private GeoHash geoHash = new GeoHash();
  @Test
  public void determinePrecisionNormal() {
    int distance = 1000;
    int expected = 6; // Precision of the geoHash for 1000m
    int actual = geoHash.determinePrecision(distance);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void determinePrecisionMax() {
    int distance = 5000000;
    int expected = 1; // Precision of the geoHash for 5000km
    int actual = geoHash.determinePrecision(distance);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void determinePrecisionMin() {
    int distance = 0;
    int expected = 9; // Precision of the geoHash for 5000km
    int actual = geoHash.determinePrecision(distance);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void encode() {
    double lat = 25.650413;
    double lng = -100.289855;
    String expected = "9u89vve0m";
    String actual = geoHash.encodeVendor(lat, lng);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void getHasheshToQuery() {
    double lat = 25.650413;
    double lng = -100.289855;
    int distance = 1000;
    List<String> expected = Arrays.asList("9u89vy", "9u89yn", "9u89yj", "9u89yh",
            "9u89vu", "9u89vs", "9u89vt", "9u89vw", "9u89vv");
    List<String> actual = geoHash.getHashesToQuery(lat, lng, distance);

    Assert.assertEquals(expected, actual);
  }
}