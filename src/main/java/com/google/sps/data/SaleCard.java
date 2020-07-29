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

import com.google.appengine.api.datastore.EmbeddedEntity;
import java.time.LocalTime;

/** A saleCard. */
public final class SaleCard {

  private final long id;
  private String businessName;
  private String description;
  private LocalTime startTime;
  private LocalTime endTime;
  private LocationData location;
  private Picture picture;

  public SaleCard(long id, String businessName, String description,
      LocalTime startTime, LocalTime endTime, LocationData location, Picture picture) {
    this.id = id;
    this.businessName = businessName;
    this.description = description;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
    this.picture = picture;
  }

  public SaleCard(EmbeddedEntity embeddedSaleCard) {
    if (embeddedSaleCard == null) {
      return;
    }

    this.id = embeddedSaleCard.getKey().getId();
    this.businessName = embeddedSaleCard.getProperty("businessName");
    this.description = embeddedSaleCard.getProperty("description");
    this.startTime = LocalTime.parse(embeddedSaleCard.getProperty("startTime"));
    this.endTime = LocalTime.parse(embeddedSaleCard.getProperty("endTime"));

    EmbeddedEntity embeddedLocation = (EmbeddedEntity) embeddedSaleCard.getProperty("location");
    this.location = new Location(embeddedLocation);

    EmbeddedEntity embeddedPicture = (EmbeddedEntity) embeddedSaleCard.getProperty("picture");
    this.picture = new Picture(embeddedPicture);
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
    return picture;
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

  public void setPic(Picture picture) {
    this.picture = picture;
  }
}