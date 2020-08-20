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

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
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
 * Delete a vendor's business with all the information related to it
 */
@WebServlet("/delete-salecard")
public class DeleteSaleCardServlet extends HttpServlet {

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

    if (vendorObject.getSaleCard() == null) {
      System.out.println("Vendor does not have a salecard.");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Vendor does not have a salecard.");
      return;
    }

    // Delete salecard's picture from blobstore
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    blobstoreService.delete(vendorObject.getSaleCard().getPicture().getBlobKey());

    // Generate Keys to delete from datastore
    Key saleCardKey = KeyFactory.createKey(vendorKey, "SaleCard", vendorObject.getSaleCard().getId());
    Key pictureKey = KeyFactory.createKey(saleCardKey, "Picture", vendorObject.getSaleCard().getPicture().getId());
    Key locationKey = KeyFactory.createKey(saleCardKey, "LocationData", vendorObject.getSaleCard().getLocation().getId());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.delete(locationKey);    
    datastore.delete(pictureKey);
    datastore.delete(saleCardKey);

    // Update vendor's embedded salecard
    vendorEntity.setProperty("saleCard", null);
    datastore.put(vendorEntity);
      
    response.sendRedirect("/");
  }
}
