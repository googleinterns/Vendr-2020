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
import com.google.appengine.api.datastore.Entity;
import java.util.Objects;

/** A vendor. */
public final class Vendor {
  private final String id;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Picture profilePic;
  private SaleCard saleCard;

  public Vendor(String id, String firstName, String lastName, String email, String phoneNumber,
      Picture profilePic, SaleCard saleCard) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.profilePic = profilePic;
    this.saleCard = saleCard;
  }

  public Vendor(Entity entity) {
    this.id = (String) entity.getKey().getName();
    this.firstName = (String) entity.getProperty("firstName");
    this.lastName = (String) entity.getProperty("lastName");
    this.email = (String) entity.getProperty("email");
    this.phoneNumber = (String) entity.getProperty("phoneNumber");

    EmbeddedEntity embeddedPic = (EmbeddedEntity) entity.getProperty("profilePic");
    this.profilePic = (embeddedPic == null) ? null : new Picture(embeddedPic);
    EmbeddedEntity embeddedBusiness = (EmbeddedEntity) entity.getProperty("saleCard");
    this.saleCard = (embeddedBusiness == null) ? null : new SaleCard(embeddedBusiness);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof Vendor)) {
      return false;
    }
    Vendor vendor = (Vendor) obj;
    return Objects.equals(id, vendor.id) && Objects.equals(firstName, vendor.firstName) &&
        Objects.equals(lastName, vendor.lastName) && Objects.equals(email, vendor.email) &&
        Objects.equals(phoneNumber, vendor.phoneNumber) && Objects.equals(profilePic, vendor.profilePic) &&
        Objects.equals(saleCard, vendor.saleCard);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, email, phoneNumber, profilePic, saleCard);
  }

  public String getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public Picture getProfilePic() {
    return profilePic;
  }

  public SaleCard getSaleCard() {
    return saleCard;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void setProfilePic(Picture profilePic) {
    this.profilePic = profilePic;
  }

  public void setSaleCard(SaleCard saleCard) {
    this.saleCard = saleCard;
  }
}