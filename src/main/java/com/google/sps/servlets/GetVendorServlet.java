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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.HttpServletUtils;
import com.google.sps.data.Vendor;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Retrieve all the vendor information, including saleCard and profile picture
 * GET: For clients wanting info of a specific vendor
 * POST: For vendors wanting to see their info
 */
@WebServlet("/get-vendor")
public class GetVendorServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String vendorId = HttpServletUtils.getParameter(request, "vendorId", "");
    float latitude, longitude;
    GeoPt clientLocation;
    try {
      // If not provided, we set them to 360 to throw an error when trying to use them to create a GeoPt
      latitude = Float.parseFloat(HttpServletUtils.getParameter(request, "lat", "360"));
      longitude = Float.parseFloat(HttpServletUtils.getParameter(request, "lng", "360"));
      clientLocation = new GeoPt(latitude, longitude);
    } catch (NumberFormatException e) {
      System.out.println("The string is not a parsable float: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong format of parameters");
      return;
    } catch (IllegalArgumentException e) {
      System.out.println(e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Latitude and/or longitude outside range");
      return;
    }

    if (vendorId.isEmpty()) {
      System.out.println("No vendor provided");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No vendor provided.");
      return;
    }

    Entity vendorEntity = HttpServletUtils.getVendorEntity(vendorId);
    if (vendorEntity == null) {
      System.out.println("Vendor Account does not exist");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Vendor Account does not exist");
      return;
    }

    Vendor vendorObject = new Vendor(vendorEntity);
    // Add the distance between client and vendor's business
    if (vendorObject.getSaleCard() != null) {
      float distanceClientVendor = HttpServletUtils.computeGeoDistance(
          clientLocation, vendorObject.getSaleCard().getLocation().getSalePoint());
      vendorObject.getSaleCard().setDistanceFromClient(distanceClientVendor);
    }

    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(new Gson().toJson(vendorObject));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    
    if (!userService.isUserLoggedIn()) {
      System.out.println("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in.");
      return;
    }

    Entity vendorEntity = HttpServletUtils.getVendorEntity(userService.getCurrentUser().getUserId());
    if (vendorEntity == null) {
      System.out.println("Vendor Account does not exist");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Vendor Account does not exist");
      return;
    }

    Vendor vendorObject = new Vendor(vendorEntity);
    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(new Gson().toJson(vendorObject));
  }
}
