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

package com.google.sps;

import java.util.Arrays;
import java.util.List;

/** Definition of constants that servlets will use. */
public interface COMMONS {
  int MAX_DISTANCE_CLIENT = 20 * 1000; // 20 kilometers
  int MAX_DISTANCE_VENDOR = 2 * 1000; // 3 kilometers
  int MIN_DISTANCE = 0;

  // Geohash
  String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"; // (geohash-specific) Base32 map
  int MAX_GEOHASH_PRECISION = 9; // Max precision stored in datastore.
  int MAX_GEOHASH_HEIGHT = 5 * 1000; // 5000km
  List<String> GEOHASH_DIRECTIONS =
          Arrays.asList("n","ne","e","se","s","sw","w","nw");
}