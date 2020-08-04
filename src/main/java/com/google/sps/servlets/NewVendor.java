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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Vendor;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet to register a new vendor in Datastore */
@WebServlet("/new-vendor")
public class NewVendor extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final UserService userService = UserServiceFactory.getUserService();
    
    if (!userService.isUserLoggedIn()) {
      response.setContentType("text/html");
      response.getWriter().println("Error: You're not logged in");
      return;
    }

    final Vendor newVendor = getVendorData(request, userService);
    toDatastore(newVendor);
  
    response.sendRedirect("/");
  }

  // Retrieve Vendors data from input form
  private Vendor getVendorData(HttpServletRequest request, UserService userService) throws IOException {
    final String firstName = request.getParameter("first_name");
    final String lastName = request.getParameter("last_name");
    final String phoneNumber = request.getParameter("phone_number");
    final String email = userService.getCurrentUser().getEmail();
    final String id = userService.getCurrentUser().getUserId();

    return new Vendor(id, firstName, lastName, email, phoneNumber, null, null);
  }

  private Entity createVendorEntity(Vendor newVendor) throws IOException {
    Entity vendorEntity = new Entity("Vendor", newVendor.getId());

    vendorEntity.setProperty("firstName", newVendor.getFirstName());
    vendorEntity.setProperty("lastName", newVendor.getLastName());
    vendorEntity.setProperty("email", newVendor.getEmail());
    vendorEntity.setProperty("phoneNumber", newVendor.getPhoneNumber());
    vendorEntity.setProperty("profilePic", newVendor.getProfilePic());
    vendorEntity.setProperty("businessInfo", newVendor.getBusinessInfo());

    return vendorEntity;
  }

  // Adds a new vendor entity in Datastore
  private void toDatastore(Vendor newVendor) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity vendor = createVendorEntity(newVendor);

    datastore.put(vendor); 
  }
}