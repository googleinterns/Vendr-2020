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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.HttpServletUtils;
import com.google.sps.data.Vendor;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * When the user submits the form, Blobstore processes the file upload and then forwards the request
 * to this servlet to update a vendor information
 */
@WebServlet("/update-vendor")
public class UpdateVendorServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    
    if (userService.isUserLoggedIn()) {
      String firstName = HttpServletUtils.getParameter(request, "firstName", "");
      String lastName = HttpServletUtils.getParameter(request, "lastName", "");
      String phoneNumber = HttpServletUtils.getParameter(request, "phoneNumber", "");
      
      // Get values to add/update vendor's profile picture
      String currentBlobKey = HttpServletUtils.getParameter(request, "blobKey", "");
      BlobKey imageBlobKey = HttpServletUtils.getUploadedFileBlobKey(request, "imageFile");
      String altText = HttpServletUtils.getParameter(request, "altText", "");
      // If nothing was uploaded and there is a current picture, keep current
      if (!currentBlobKey.isEmpty() && imageBlobKey == null) {
        imageBlobKey = new BlobKey(currentBlobKey);
      }

      // Check values are not empty or outside range
      if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || altText.isEmpty() || 
          imageBlobKey == null) {
        System.out.println("The values do not exist and/or are outside the range.");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      Key vendorKey = KeyFactory.createKey("Vendor", userService.getCurrentUser().getUserId());
      Entity vendorEntity;
      try {
        vendorEntity = datastore.get(vendorKey);
      } catch (EntityNotFoundException e) {
        System.out.println(e);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      Vendor vendorObject = new Vendor(vendorEntity);

      Entity picture;
      // If the vendor doesn't have a picture, create completely new entity
      // Else, use the already existing entity
      if (vendorObject.getProfilePic() == null) {
        picture = new Entity("Picture", vendorKey);
      } else {
        picture = new Entity("Picture",
            vendorObject.getProfilePic().getId(), vendorKey);
      }

      // If not the same, delete previous blob from blobstore
      if (vendorObject.getProfilePic() != null && 
          imageBlobKey.compareTo(vendorObject.getProfilePic().getBlobKey()) != 0) {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        blobstoreService.delete(vendorObject.getProfilePic().getBlobKey());
      }
      picture.setProperty("blobKey", imageBlobKey);
      picture.setProperty("altText", altText);
      datastore.put(picture);

      EmbeddedEntity picInfo = new EmbeddedEntity();
      picInfo.setKey(picture.getKey());
      picInfo.setPropertiesFrom(picture);

      vendorEntity.setProperty("firstName", firstName);
      vendorEntity.setProperty("lastName", lastName);
      vendorEntity.setProperty("phoneNumber", phoneNumber);
      vendorEntity.setProperty("profilePic", picInfo);
      datastore.put(vendorEntity);
      
      response.sendRedirect("/");
    } else {
      System.out.println("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}
