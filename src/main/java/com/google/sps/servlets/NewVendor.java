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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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
    response.setContentType("text/html");

    if (!userService.isUserLoggedIn()) {
      response.getWriter().println("Error: You're not logged in");
      return;
    }

    if (isUserRegistered(userService.getCurrentUser().getUserId())) {
      response.getWriter().println("You're already registered!");
      return;
    }

    final Vendor newVendor = getVendorData(request, userService);

    if (!isValidInput(newVendor)) {
      response.getWriter().println("Error: You can't send null or empty inputs");
      return;
    }

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
    vendorEntity.setProperty("saleCard", newVendor.getSaleCard());

    return vendorEntity;
  }

  // Adds a new vendor entity in Datastore
  private void toDatastore(Vendor newVendor) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(createVendorEntity(newVendor)); 
  }

  // Checks if any of the input values is empty
  private boolean isValidInput(Vendor newVendor) throws IOException {
    final String firstName = newVendor.getFirstName();
    final String lastName =  newVendor.getLastName();
    final String phoneNumber = newVendor.getPhoneNumber();

    return !(
      (firstName == null || firstName.isEmpty()) ||
      (lastName == null || lastName.isEmpty()) ||
      (phoneNumber == null || phoneNumber.isEmpty())
    );
  }

  public boolean isUserRegistered(String id) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key vendorKey = KeyFactory.createKey("Vendor", id);
    
    try {
      Entity vendor = datastore.get(vendorKey);
      String phoneNumber = (String) vendor.getProperty("phoneNumber");
      
      return (phoneNumber == null || phoneNumber.isEmpty()) ? false : true;
    } catch (EntityNotFoundException e) {
      return false;
    }
  }
}
