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

@WebServlet("/new-vendor")
public class NewVendor extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      System.out.println("User not logged in");
      response.sendRedirect("/");
      return;
    }
    Vendor newVendor = getVendorData(request, userService);
  
    // DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Entity entity = new Entity("Vendor", id);
    // entity.setProperty("id", id);
    // entity.setProperty("nickname", nickname);
    // // The put() function automatically inserts new data or updates existing data based on ID
    // datastore.put(entity);

    response.sendRedirect("/");
  }

  private Vendor getVendorData(HttpServletRequest request, UserService userService) throws IOException {
    String firstName = request.getParameter("first_name");
    String lastName = request.getParameter("last_name");
    String phoneNumber = request.getParameter("phone_number");
    String email = userService.getCurrentUser().getEmail();
    String id = userService.getCurrentUser().getUserId();

    return new Vendor(id, firstName, lastName, email, phoneNumber, null, null);
  }

  /**
   * Returns the nickname of the user with id, or empty String if the user has not set a nickname.
   */
  private String getUserNickname(String id) {
    System.out.println("Calling DataStore");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    System.out.println("Getting Query");
    Query query =
        new Query("Vendor")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    System.out.println("Preparing Query");
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      System.out.println("Is null");
      return "";
    }
    String nickname = (String) entity.getProperty("nickname");
    return nickname;
  }
}