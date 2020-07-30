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
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.sps.data.HttpServletUtils;
import com.google.sps.data.SaleCard;
import com.google.sps.data.Vendor;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that looks for nearby vendors */
@WebServlet("/get-nearby-vendors")
public class GetNearbyVendors extends HttpServlet {

  private final static float MAX_DISTANCE = 20000f; // 20 kilometers
  private final static float MIN_DISTANCE = 0f;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {    
    // -- The way we get these values might change. This is just an idea ---------------
    String prefixGeoHash = HttpServletUtils.getParameter(request, "prefixGeoHash", "");
    boolean hasDelivery = Boolean.parseBoolean(HttpServletUtils.getParameter(request, "hasDelivery", "false"));
    float latitude = 0f;
    float longitude = 0f;
    float distance = 0f;
    GeoPt clientLocation = new GeoPt(0f, 0f);
    try {
      latitude = Float.parseFloat(HttpServletUtils.getParameter(request, "lat", "360"));
      longitude = Float.parseFloat(HttpServletUtils.getParameter(request, "long", "360"));
      distance = Float.parseFloat(HttpServletUtils.getParameter(request, "distance", "1000"));
      clientLocation = new GeoPt(latitude, longitude);
    } catch (NumberFormatException e) {
      System.out.println("The string is not a parsable float: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } catch (IllegalArgumentException e) {
      System.out.println("Latitude and/or longitude outside legal range: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    // ---------------------------------------------------------------------------------
    
    // Check values exist and are in the range
    if (prefixGeoHash.isEmpty() || distance > MAX_DISTANCE || distance < MIN_DISTANCE) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // Generate query
    Query query = buildGeoQuery(prefixGeoHash, hasDelivery);
    
    // Fetch results
    Iterable<Entity> vendorsRetrieved = fetchVendors(query);

    // Create list and filter by distance
    List<Vendor> nearbyVendors = createVendorsList(vendorsRetrieved, distance);

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(gson.toJson(nearbyVendors));
  }

  /** 
   * Returns a query for a Vendor with a filter to match a substring (the prefix of a geoHash) and
   * if the vendor has or not delivery service 
   */
  private Query buildGeoQuery(String prefixGeoHash, boolean hasDelivery) {
    Filter lowerBound = new FilterPredicate("saleCard.locationData.geoHash",
        FilterOperator.GREATER_THAN_OR_EQUAL, prefixGeoHash);
    Filter upperBound = new FilterPredicate("saleCard.locationData.geoHash",
        FilterOperator.LESS_THAN, prefixGeoHash + "\ufffd");
    Filter deliveryFilter = new FilterPredicate("saleCard.hasDelivery",
        FilterOperator.EQUAL, hasDelivery);

    Filter geoFilter = CompositeFilterOperator.and(lowerBound, upperBound, deliveryFilter);

    return new Query("Vendor").setFilter(geoFilter);
  }

  /** Returns an iterable with the results retrieved from Datastore */
  private Iterable<Entity> fetchVendors(Query query) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    return datastore.prepare(query).asIterable();
  }

  /** Returns a list with the retrieved vendors and check their are within the requested distance*/
  private List<Vendor> createVendorsList(Iterable<Entity> vendors, float distanceLimit) {
    List<Vendor> nearbyVendors = new ArrayList<>();
    for (Entity vendor : vendors) {
      // TODO: Add only vendors which abs(vendorLoc - clientLoc) <= distanceLimit - Use Haversine formula
      nearbyVendors.add(new Vendor(vendor));
    }

    return nearbyVendors;
  }
}
