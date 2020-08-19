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

/** A saleCard containing the information of a business (e.g. description, opening hours, location) */
public final class SaleCard {
  private final long id;
  private String businessName;
  private String description;
  private boolean hasDelivery;
  private boolean isTemporarilyClosed;
  private LocalTime startTime;
  private LocalTime endTime;
  private LocationData location;
  private Picture picture;
  private float distanceFromClient;

  public SaleCard(long id, String businessName, String description, boolean hasDelivery, boolean isTemporarilyClosed,
    LocalTime startTime, LocalTime endTime, LocationData location, Picture picture) {
    this.id = id;
    this.businessName = businessName;
    this.description = description;
    this.hasDelivery = hasDelivery;
    this.isTemporarilyClosed = isTemporarilyClosed;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
    this.picture = picture;
    this.distanceFromClient = 0;
  }

  public SaleCard(EmbeddedEntity embeddedSaleCard) {
    if (embeddedSaleCard == null) {
      throw new IllegalArgumentException("SaleCard cannot be initialized with null EmbeddedEntity");
    }

    this.id = (long) embeddedSaleCard.getKey().getId();
    this.businessName = (String) embeddedSaleCard.getProperty("businessName");
    this.description = (String) embeddedSaleCard.getProperty("description");
    this.hasDelivery = (boolean) embeddedSaleCard.getProperty("hasDelivery");
    this.isTemporarilyClosed = (boolean) embeddedSaleCard.getProperty("isTemporarilyClosed");

    CharSequence start = (CharSequence) embeddedSaleCard.getProperty("startTime");
    this.startTime = LocalTime.parse(start);
    CharSequence end = (CharSequence) embeddedSaleCard.getProperty("endTime");
    this.endTime = LocalTime.parse(end);
    
    EmbeddedEntity embeddedLocation = (EmbeddedEntity) embeddedSaleCard.getProperty("location");
    this.location = (embeddedLocation == null) ? null : new LocationData(embeddedLocation);
    EmbeddedEntity embeddedPicture = (EmbeddedEntity) embeddedSaleCard.getProperty("picture");
    this.picture = (embeddedPicture == null) ? null : new Picture(embeddedPicture);
    this.distanceFromClient = 0;
  }

  public long getId() {
    return id;
  }

  public String getBusinessName() {
    return businessName;
  }

  public String getDescription() {
    return description;
  }

  public boolean hasDelivery() {
    return hasDelivery;
  }

  public boolean isTemporarilyClosed() {
    return isTemporarilyClosed;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public LocationData getLocation() {
    return location;
  }

  public Picture getPicture() {
    return picture;
  }

  public float getDistanceFromClient() {
    return distanceFromClient;
  }

  public void setBusinessName(String businessName) {
    this.businessName = businessName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setHasDelivery(boolean hasDelivery) {
    this.hasDelivery = hasDelivery;
  }

  public void setIsTemporarilyClosed(boolean isTemporarilyClosed) {
    this.isTemporarilyClosed = isTemporarilyClosed;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public void setLocation(LocationData location) {
    this.location = location;
  }

  public void setPicture(Picture picture) {
    this.picture = picture;
  }

  public void setDistanceFromClient(float distanceFromClient) {
    this.distanceFromClient = distanceFromClient;
  }
}