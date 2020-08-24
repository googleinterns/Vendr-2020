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

import com.google.appengine.api.datastore.GeoPt;
import com.google.gson.Gson;
import com.google.sps.COMMONS;
import com.google.sps.data.HttpServletUtils;
import com.google.sps.data.Vendor;
import com.google.sps.utility.GeoHash;
import com.google.sps.utility.NearbyVendorsQuery;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
    } catch (DateTimeParseException e) {
      System.err.println("Bad format to parse: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } catch (NumberFormatException e) {
      System.err.println("The string is not a parsable int/float: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } catch (IllegalArgumentException e) {
      System.err.println("Latitude and/or longitude outside legal range: " + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // Check values exist and are in the range
    if (geoHashesToQuery.isEmpty() || distance > COMMONS.MAX_DISTANCE_CLIENT || distance < COMMONS.MIN_DISTANCE) {
      System.err.println("The values do not exist and/or are outside the range.");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    NearbyVendorsQuery nearbyVendorsQuery = new NearbyVendorsQuery();
    List<Vendor> nearbyVendors = new ArrayList<>();
    for(String prefixGeoHash : geoHashesToQuery) {
      List<Vendor> vendorsRetrieved = nearbyVendorsQuery.query(
          prefixGeoHash, hasDelivery, onlyOpenNow, clientLocation, distance, currentTime);
      nearbyVendors.addAll(vendorsRetrieved);
    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(gson.toJson(nearbyVendors));
  }
}
