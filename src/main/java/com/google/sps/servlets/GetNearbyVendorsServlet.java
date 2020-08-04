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
public class GetNearbyVendorsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {    
    String prefixGeoHash = HttpServletUtils.getParameter(request, "prefixGeoHash", "");
    boolean hasDelivery = Boolean.parseBoolean(HttpServletUtils.getParameter(request, "hasDelivery", "false"));
    float latitude = 0f;
    float longitude = 0f;
    float distance = 0f;
    GeoPt clientLocation = new GeoPt(0f, 0f);
    try {
      distance = Float.parseFloat(HttpServletUtils.getParameter(request, "distance", "1000"));
      // If not provided, we set them to 360 to throw an error when trying to use them to create a GeoPt
      latitude = Float.parseFloat(HttpServletUtils.getParameter(request, "lat", "360"));
      longitude = Float.parseFloat(HttpServletUtils.getParameter(request, "long", "360"));
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
    
    // Check values exist and are in the range
    if (prefixGeoHash.isEmpty() || distance > COMMONS.MAX_DISTANCE_CLIENT || distance < COMMONS.MIN_DISTANCE) {
      System.out.println("The values do not exist and/or are outside the range.");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Query query = buildGeoQuery(prefixGeoHash, hasDelivery);
    Iterable<Entity> vendorsRetrieved = fetchVendors(query);
    List<Vendor> nearbyVendors = createVendorsList(vendorsRetrieved, clientLocation, distance);
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
    Filter lowerBound = new FilterPredicate("saleCard.location.geoHash",
        FilterOperator.GREATER_THAN_OR_EQUAL, prefixGeoHash);
    Filter upperBound = new FilterPredicate("saleCard.location.geoHash",
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

  /** Returns a list with the retrieved vendors and check they are within the requested distance*/
  private List<Vendor> createVendorsList(Iterable<Entity> vendors, GeoPt clientLocation, float distanceLimit) {
    List<Vendor> nearbyVendors = new ArrayList<>();
    for (Entity vendorEntity : vendors) {
      Vendor vendor = new Vendor(vendorEntity);
      GeoPt vendorLocation = vendor.getSaleCard().getLocation().getSalePoint();
      if (computeDistance(clientLocation, vendorLocation) <= distanceLimit) {
        nearbyVendors.add(vendor);
      }    
    }

    return nearbyVendors;
  }

  /** Computes the distance between two geographical points using Haversine formula */
  private float computeDistance(GeoPt pointA, GeoPt pointB) {
    double latitudeRadiansA = Math.toRadians(pointA.getLatitude());
    double latitudeRadiansB = Math.toRadians(pointB.getLatitude());

    double latitudeDifference = Math.toRadians(pointB.getLatitude() - pointA.getLatitude());
    double longitudeDifference = Math.toRadians(pointB.getLongitude() - pointA.getLongitude());

    double a = Math.pow(Math.sin(latitudeDifference / 2), 2) + 
        Math.cos(latitudeRadiansA) * Math.cos(latitudeRadiansB) * Math.pow(Math.sin(longitudeDifference / 2), 2);
    double c = 2 * Math.asin(Math.sqrt(a));

    return (float) (GeoPt.EARTH_RADIUS_METERS * c);
  }
}
