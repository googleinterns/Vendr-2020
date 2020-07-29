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

package com.google.sps.data;

import com.google.appengine.api.datastore.GeoPt;

/** A class for the location information of a business. */
public final class LocationData {

  private final long id;
  private GeoPt salePoint;
  private String geoHash;
  private int radius;

  public LocationData(long id, GeoPt salePoint, String geoHash, int radius) {
    this.id = id;
    this.salePoint = salePoint;
    this.geoHash = geoHash;
    this.radius = radius;
  }

  public long getId() {
    return id;
  }

  public GeoPt getSalePoint() {
    return salePoint;
  }

  public String geoHash() {
    return geoHash;
  }

  public int getRadius() {
    return radius;
  }

  public void setSalePoint(GeoPt salePoint) {
    this.salePoint = salePoint;
  }

  public void setGeoHash(String geoHash) {
    this.geoHash = geoHash;
  }

  public void setRadius(int radius) {
    this.radius = radius;
  }
}