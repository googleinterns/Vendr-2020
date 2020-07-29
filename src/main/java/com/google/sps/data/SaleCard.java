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

import java.time.LocalTime;

/** A saleCard. */
public final class SaleCard {

  private final long id;
  private String businessName;
  private LocationData location;
  private LocalTime startTime;
  private LocalTime endTime;
  private String description;
  private Picture pic;

  public SaleCard(long id, String businessName, LocationData location,
      LocalTime startTime, LocalTime endTime, String description, Picture pic) {
    this.id = id;
    this.businessName = businessName;
    this.location = location;
    this.startTime = startTime;
    this.endTime = endTime;
    this.description = description;
    this.pic = pic;
  }

  public long getId() {
    return id;
  }

  public String getBusinessName() {
    return businessName;
  }

  public LocationData getLocation() {
    return location;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public String getDescription() {
    return description;
  }

  public Picture getPic() {
    return pic;
  }

  public void setBusinessName(String businessName) {
    this.businessName = businessName;
  }

  public void setLocation(LocationData location) {
    this.location = location;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setPic(Picture pic) {
    this.pic = pic;
  }
}