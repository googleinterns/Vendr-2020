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
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.sps.COMMONS;
import com.google.sps.data.HttpServletUtils;
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
@WebServlet("/add-saleCard")
public class AddSaleCardServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // TODO: Get if vendor is logged in with auth API
    boolean vendorStatus = false;
    
    if (vendorStatus) {
      // -- The way we get these values might change. This is just an idea ---------------
      // Get parameters
      String businessName = HttpServletUtils.getParameter(request, "businessName", "");
      boolean hasDelivery = Boolean.parseBoolean(HttpServletUtils.getParameter(request, "hasDelivery", "false"));
      String startTime = HttpServletUtils.getParameter(request, "startTime", "");
      String endTime = HttpServletUtils.getParameter(request, "endTime", "");
      String description = HttpServletUtils.getParameter(request, "description", "");
      String geoHash = HttpServletUtils.getParameter(request, "geoHash", "");
      float radius = 0f;
      float latitude = 0f;
      float longitude = 0f;
      GeoPt vendorLocation = new GeoPt(0f, 0f);
      
      // Check values are valid format 
      try {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        radius = Float.parseFloat(HttpServletUtils.getParameter(request, "radius", "1000"));
        // If not provided, we set them to 360 to throw an error when trying to use them to create a GeoPt
        latitude = Float.parseFloat(HttpServletUtils.getParameter(request, "lat", "360"));
        longitude = Float.parseFloat(HttpServletUtils.getParameter(request, "long", "360"));
        vendorLocation = new GeoPt(latitude, longitude);
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

      // Get the BlobKey of the image that the user uploaded to Blobstore
      BlobKey imageBlobKey = getUploadedFileBlobKey(request, "imageFile");
      String altText = HttpServletUtils.getParameter(request, "altText", "");

      // Check values are not empty or outside range
      if (businessName.isEmpty() || description.isEmpty() || geoHash.isEmpty() || altText.isEmpty() || 
          imageBlobKey == null || radius < COMMONS.MIN_DISTANCE || radius > COMMONS.MAX_DISTANCE_VENDOR) {
        System.out.println("The values do not exist and/or are outside the range.");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      // TODO: Create vendor key using the Auth ID
      // Key vendorKey = KeyFactory.createKey("Vendor", (String) user.authID);

      // Generate Key when datastore put
      Entity saleCard = new Entity("SaleCard"/*, vendorKey*/);
      datastore.put(saleCard);      

      // TODO: Remove from datastore existing saleCard, picture and location data

      Entity picture = new Entity("Picture", saleCard.getKey());
      picture.setProperty("blobKey", imageBlobKey);
      picture.setProperty("altText", altText);
      datastore.put(picture);

      Entity locationData = new Entity("LocationData", saleCard.getKey());
      locationData.setProperty("salePoint", vendorLocation);
      locationData.setProperty("geoHash", geoHash);
      locationData.setProperty("radius", radius);
      datastore.put(locationData);

      EmbeddedEntity picInfo = new EmbeddedEntity();
      picInfo.setPropertiesFrom(picture);
      EmbeddedEntity locInfo = new EmbeddedEntity();
      locInfo.setPropertiesFrom(locationData);

      saleCard.setIndexedProperty("picture", picInfo);
      saleCard.setIndexedProperty("location", locInfo);
      datastore.put(saleCard);

      EmbeddedEntity saleInfo = new EmbeddedEntity();
      saleInfo.setPropertiesFrom(saleCard);

      /* TODO: Vendor Entity Put
        Entity vendor = datastore.get(vendorKey);
        vendor.setIndexedProperty("saleCard", saleInfo);
        datastore.put(vendor);
      */

      response.sendRedirect("/");
    } else {
      System.out.println("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  /** Returns a BlobKey for the uploaded file, or null if the user didn't upload a file. */
  private BlobKey getUploadedFileBlobKey(HttpServletRequest request, String formInput) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInput);

    // User submitted form without selecting a file (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo == null || blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // TODO: Check the uploaded file is an image file

    return blobKey;
  }
}
