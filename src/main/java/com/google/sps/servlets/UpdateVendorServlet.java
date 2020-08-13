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
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    if (!userService.isUserLoggedIn()) {
      System.out.println("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in.");
      return;
    }

    String firstName = HttpServletUtils.getParameter(request, "firstName", "");
    String lastName = HttpServletUtils.getParameter(request, "lastName", "");
    String phoneNumber = HttpServletUtils.getParameter(request, "phoneNumber", "");
      
    // Get values to add/update vendor's profile picture
    String currentBlobKey = HttpServletUtils.getParameter(request, "blobKey", "");
    BlobKey imageBlobKey = HttpServletUtils.getUploadedFileBlobKey(request, "imageFile");
    String altText = HttpServletUtils.getParameter(request, "altText", "");
    System.out.println(imageBlobKey);
    // If nothing was uploaded and there is a current picture, keep current
    if (!currentBlobKey.isEmpty() && imageBlobKey == null) {
      imageBlobKey = new BlobKey(currentBlobKey);
    }

    // Check values are not empty and valid
    if (!HttpServletUtils.hasOnlyLetters(firstName) || !HttpServletUtils.hasOnlyLetters(lastName) || 
        !HttpServletUtils.hasOnlyNumbers(phoneNumber)) {
      // Delete from blobstore if the uploaded file was a new one
      if (imageBlobKey != null && !currentBlobKey.equals(imageBlobKey.toString())) {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        blobstoreService.delete(imageBlobKey);
      }
      System.out.println("The values do not exist and/or the format is incorrect.");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Inputs do not exist and/or the format is incorrect.");
      return;
    }

    String vendorId = userService.getCurrentUser().getUserId();
    Key vendorKey = KeyFactory.createKey("Vendor", vendorId);
    Entity vendorEntity = HttpServletUtils.getVendorEntity(vendorId);
    if (vendorEntity == null) {
      vendorEntity = new Entity(vendorKey);
      datastore.put(vendorEntity);      
    }
    Vendor vendorObject = new Vendor(vendorEntity);

    // If the vendor doesn't have a picture, create completely new entity
    // Else, use the already existing entity
    Entity picture = (vendorObject.getProfilePic() == null)
        ? new Entity("Picture", vendorKey)
        : new Entity("Picture", vendorObject.getProfilePic().getId(), vendorKey);

    // If not the same, delete previous blob from blobstore
    if (vendorObject.getProfilePic() != null && imageBlobKey != null &&
        imageBlobKey.compareTo(vendorObject.getProfilePic().getBlobKey()) != 0) {
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      blobstoreService.delete(vendorObject.getProfilePic().getBlobKey());
    }

    // If there is a picture with alt text, set properties; Else if only picture, delete from blobstore 
    if (imageBlobKey != null && !altText.isEmpty()) {
      picture.setProperty("blobKey", imageBlobKey);
      picture.setProperty("altText", altText);
      datastore.put(picture);

      EmbeddedEntity picInfo = new EmbeddedEntity();
      picInfo.setKey(picture.getKey());
      picInfo.setPropertiesFrom(picture);

      vendorEntity.setProperty("profilePic", picInfo);
    } else if (imageBlobKey != null) {
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      blobstoreService.delete(imageBlobKey);
    }

    vendorEntity.setProperty("firstName", firstName);
    vendorEntity.setProperty("lastName", lastName);
    vendorEntity.setProperty("phoneNumber", phoneNumber);
    vendorEntity.setProperty("email", userService.getCurrentUser().getEmail());
    datastore.put(vendorEntity);
      
    response.sendRedirect("/");
  }
}
