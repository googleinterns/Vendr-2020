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

package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.COMMONS;
import com.google.sps.data.HttpServletUtils;
import com.google.sps.data.Vendor;
import com.google.sps.utility.GeoHash;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * When the user submits the form, Blobstore processes the file upload and then forwards the request
 * to this servlet.
 */
@WebServlet("/update-salecard")
public class UpdateSaleCardServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    
    if (!userService.isUserLoggedIn()) {
      System.out.println("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in.");
      return;
    }

    String vendorId = userService.getCurrentUser().getUserId();
    Key vendorKey = KeyFactory.createKey("Vendor", vendorId);
    Entity vendorEntity = HttpServletUtils.getVendorEntity(vendorId);
    if (vendorEntity == null) {
      System.out.println("User is not registered.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not registered.");
      return;
    }
    Vendor vendorObject = new Vendor(vendorEntity);

    String businessName = HttpServletUtils.getParameter(request, "businessName", "");
    boolean hasDelivery = Boolean.parseBoolean(HttpServletUtils.getParameter(request, "hasDelivery", "false"));
    boolean isTemporarilyClosed = Boolean.parseBoolean(HttpServletUtils.getParameter(request, "isTemporarilyClosed", "false"));
    String startTime = HttpServletUtils.getParameter(request, "startTime", "");
    String endTime = HttpServletUtils.getParameter(request, "endTime", "");
    String description = HttpServletUtils.getParameter(request, "description", "");
    float radius, latitude, longitude;
    String geoHash;
    GeoPt vendorLocation = new GeoPt(0f, 0f);
    LocalTime start, end;
    // Check values are valid format
    try {
      start = LocalTime.parse(startTime);
      end = LocalTime.parse(endTime);
      radius = Float.parseFloat(HttpServletUtils.getParameter(request, "radius", "1000"));
      // If not provided, we set them to 360 to throw an error when trying to use them to create a GeoPt
      latitude = Float.parseFloat(HttpServletUtils.getParameter(request, "lat", "360"));
      longitude = Float.parseFloat(HttpServletUtils.getParameter(request, "lng", "360"));
      vendorLocation = new GeoPt(latitude, longitude);
      geoHash = GeoHash.encodeVendor(latitude, longitude);
    } catch (DateTimeParseException e) {
      System.out.println("Bad format to parse: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } catch (NumberFormatException e) {
      System.out.println("The string is not a parsable float: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } catch (IllegalArgumentException e) {
      System.out.println("Latitude and/or longitude outside legal range: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // Get values to add/update salecard's picture
    String currentBlobKey = HttpServletUtils.getParameter(request, "blobKey", "");
    BlobKey imageBlobKey = HttpServletUtils.getUploadedFileBlobKey(request, "imageFile");
    String altText = HttpServletUtils.getParameter(request, "altText", "");
    // If nothing was uploaded and there is a current picture, keep current
    if (!currentBlobKey.isEmpty() && imageBlobKey == null) {
      imageBlobKey = new BlobKey(currentBlobKey);
    }

    // Check values are not empty or outside range
    if (businessName.isEmpty() || description.isEmpty() || geoHash.isEmpty() || altText.isEmpty() || start.isAfter(end) ||
        imageBlobKey == null || radius < COMMONS.MIN_DISTANCE || radius > COMMONS.MAX_DISTANCE_VENDOR) {
      // Delete from blobstore if the uploaded file was a new one
      if (imageBlobKey != null && !currentBlobKey.equals(imageBlobKey.toString())) {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        blobstoreService.delete(imageBlobKey);
      }
      System.out.println("The values do not exist and/or are outside the range.");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity saleCard, picture, locationData;
    // If the vendor doesn't have a saleCard, create completely new Entities (datastore.put(...) to generate Key)
    // Else, use the already existing Entities
    if (vendorObject.getSaleCard() == null) {
      saleCard = new Entity("SaleCard", vendorKey);
      datastore.put(saleCard);

      picture = new Entity("Picture", saleCard.getKey());
      locationData = new Entity("LocationData", saleCard.getKey());
    } else {
      saleCard = new Entity("SaleCard",
          vendorObject.getSaleCard().getId(), vendorKey);
      picture = new Entity("Picture",
          vendorObject.getSaleCard().getPicture().getId(), saleCard.getKey());
      locationData = new Entity("LocationData",
          vendorObject.getSaleCard().getLocation().getId(), saleCard.getKey());
    }

    // If not the same, delete previous blob from blobstore
    if (vendorObject.getSaleCard() != null &&
        imageBlobKey.compareTo(vendorObject.getSaleCard().getPicture().getBlobKey()) != 0) {
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      blobstoreService.delete(vendorObject.getSaleCard().getPicture().getBlobKey());
    }
    picture.setProperty("blobKey", imageBlobKey);
    picture.setProperty("altText", altText);
    datastore.put(picture);

    locationData.setProperty("salePoint", vendorLocation);
    locationData.setProperty("geoHash", geoHash);
    locationData.setProperty("radius", radius);
    datastore.put(locationData);

    EmbeddedEntity picInfo = HttpServletUtils.createEmbeddedEntity(picture);
    EmbeddedEntity locInfo = HttpServletUtils.createEmbeddedEntity(locationData);

    saleCard.setProperty("businessName", businessName);
    saleCard.setProperty("description", description);
    saleCard.setProperty("hasDelivery", hasDelivery);
    saleCard.setProperty("isTemporarilyClosed", isTemporarilyClosed);
    saleCard.setProperty("startTime", startTime);
    saleCard.setProperty("endTime", endTime);
    saleCard.setProperty("picture", picInfo);
    saleCard.setIndexedProperty("location", locInfo);
    datastore.put(saleCard);

    EmbeddedEntity saleInfo = HttpServletUtils.createEmbeddedEntity(saleCard);

    vendorEntity.setIndexedProperty("saleCard", saleInfo);
    datastore.put(vendorEntity);

    response.sendRedirect("/views/editCard.html");
  }
}
