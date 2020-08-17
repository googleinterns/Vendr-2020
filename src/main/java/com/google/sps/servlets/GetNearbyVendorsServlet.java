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
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.sps.COMMONS;
import com.google.sps.data.HttpServletUtils;
import com.google.sps.data.SaleCard;
import com.google.sps.data.Vendor;
import com.google.sps.utility.GeoHash;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that looks for nearby vendors */
@WebServlet("/get-nearby-vendors")
public class GetNearbyVendorsServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    boolean hasDelivery = Boolean.parseBoolean(HttpServletUtils.getParameter(request, "hasDelivery", "false"));
    boolean onlyOpenNow = Boolean.parseBoolean(HttpServletUtils.getParameter(request, "onlyOpenNow", "false"));
    LocalTime currentTime;
    float latitude ,longitude;
    int distance = 0;
    GeoPt clientLocation = new GeoPt(0f, 0f);
    List<String> geoHashesToQuery;
    try {
      currentTime = LocalTime.parse(HttpServletUtils.getParameter(request, "currentTime", ""));
      distance = Integer.parseInt(HttpServletUtils.getParameter(request, "distance", "1000"));
      // If not provided, we set them to 360 to throw an error when trying to use them to create a GeoPt
      latitude = Float.parseFloat(HttpServletUtils.getParameter(request, "lat", "360"));
      longitude = Float.parseFloat(HttpServletUtils.getParameter(request, "lng", "360"));
      clientLocation = new GeoPt(latitude, longitude);
      geoHashesToQuery = GeoHash.getHashesToQuery(latitude, longitude, distance);
    } catch (NumberFormatException e) {
      System.out.println("The string is not a parsable float: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } catch (IllegalArgumentException e) {
      System.out.println("Latitude and/or longitude outside legal range: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // Check values exist and are in the range
    if (geoHashesToQuery.isEmpty() || distance > COMMONS.MAX_DISTANCE_CLIENT || distance < COMMONS.MIN_DISTANCE) {
      System.out.println("The values do not exist and/or are outside the range.");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    List<Vendor> nearbyVendors = new ArrayList<>();
    for(String prefixGeoHash : geoHashesToQuery) {
      Query query = buildGeoQuery(prefixGeoHash, hasDelivery, onlyOpenNow);
      Iterable<Entity> vendorsRetrieved = fetchVendors(query);
      nearbyVendors.addAll(createVendorsList(vendorsRetrieved, clientLocation, distance, onlyOpenNow, currentTime));
    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(gson.toJson(nearbyVendors));
  }

  /** 
   * Returns a query for a Vendor with a filter to match a substring (the prefix of a geoHash) and
   * if the vendor has or not delivery service 
   */
  private Query buildGeoQuery(String prefixGeoHash, boolean hasDelivery, boolean onlyOpenNow) {
    Filter lowerBound = new FilterPredicate("saleCard.location.geoHash",
        FilterOperator.GREATER_THAN_OR_EQUAL, prefixGeoHash);
    Filter upperBound = new FilterPredicate("saleCard.location.geoHash",
        FilterOperator.LESS_THAN, prefixGeoHash + "\ufffd");

    // Mandatory GeoHash Filters
    List<Filter> filtersList = new ArrayList<>(Arrays.asList(lowerBound, upperBound));

    // Only with delivery
    if (hasDelivery) {
      filtersList.add(
          new FilterPredicate("saleCard.hasDelivery", FilterOperator.EQUAL, hasDelivery));
    }
    // Only open now
    if (onlyOpenNow) {
      filtersList.add(
          new FilterPredicate("saleCard.isTemporarilyClosed", FilterOperator.EQUAL, !onlyOpenNow));
    }

    Filter geoFilter = CompositeFilterOperator.and(filtersList);
    return new Query("Vendor").setFilter(geoFilter);
  }

  /** Returns an iterable with the results retrieved from Datastore */
  private Iterable<Entity> fetchVendors(Query query) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    return datastore.prepare(query).asIterable();
  }

  /** Returns a list with the retrieved vendors and check they are within the requested distance*/
  private List<Vendor> createVendorsList(Iterable<Entity> vendors, GeoPt clientLocation, int distanceLimit,
      boolean onlyOpenNow, LocalTime currentTime) {
    List<Vendor> nearbyVendors = new ArrayList<>();
    for (Entity vendorEntity : vendors) {
      Vendor vendor = new Vendor(vendorEntity);
      GeoPt vendorLocation = vendor.getSaleCard().getLocation().getSalePoint();
      float distanceClientVendor = HttpServletUtils.computeGeoDistance(clientLocation, vendorLocation);
      boolean hourCheck = (onlyOpenNow) ? isBetweenOpeningHours(vendor, currentTime) : true;

      if (distanceClientVendor <= distanceLimit && hourCheck) {
        vendor.getSaleCard().setDistanceFromClient(distanceClientVendor);
        nearbyVendors.add(vendor);
      }    
    }

    return nearbyVendors;
  }

  /** Returns true if the time is between the vendor's business opening hours */
  private boolean isBetweenOpeningHours(Vendor vendor, LocalTime currentTime) {
    return currentTime.isAfter(vendor.getSaleCard().getStartTime()) && 
      currentTime.isBefore(vendor.getSaleCard().getEndTime());
  }
}
