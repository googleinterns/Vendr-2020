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

package com.google.sps.utility;

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
import com.google.sps.data.HttpServletUtils;
import com.google.sps.data.Vendor;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/** Query to find all the vendors with the requested parameters */
public final class NearbyVendorsQuery {
  /**
   * Returns the vendors found with the requested parameters
   * @param prefixGeoHash a String representing a geohash
   * @param hasDelivery a boolean to show only vendors with delivery service
   * @param onlyOpenNow a boolean to show only vendors currently open
   * @param requestLocation a GeoPoint representing the location where the request was made
   * @param distanceLimit the max allowed distance a vendor can be
   * @param requestTime the time when the request was made
   */
  public List<Vendor> query(String prefixGeoHash, boolean hasDelivery, boolean onlyOpenNow, 
      GeoPt requestLocation, int distanceLimit, LocalTime requestTime) {
    Query datastoreQuery = buildGeoQuery(prefixGeoHash, hasDelivery, onlyOpenNow);
    Iterable<Entity> vendorsRetrieved = fetchVendors(datastoreQuery);
    return createVendorsList(vendorsRetrieved, requestLocation, distanceLimit, onlyOpenNow, requestTime);
  }

  /** 
   * Returns a query for a Vendor with a filter to match a substring (the prefix of a geoHash) and
   * if needed add filters for only vendors with delivery service and only currently open
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
  private List<Vendor> createVendorsList(Iterable<Entity> vendors, GeoPt requestLocation, int distanceLimit,
      boolean onlyOpenNow, LocalTime requestTime) {
    List<Vendor> nearbyVendors = new ArrayList<>();
    for (Entity vendorEntity : vendors) {
      Vendor vendor = new Vendor(vendorEntity);
      GeoPt vendorLocation = vendor.getSaleCard().getLocation().getSalePoint();
      float distanceClientVendor = HttpServletUtils.computeGeoDistance(requestLocation, vendorLocation);
      boolean timeCheck = (onlyOpenNow) 
          ? isBetweenOpeningHours(vendor.getSaleCard().getStartTime(), vendor.getSaleCard().getEndTime(), requestTime)
          : true;

      if (distanceClientVendor <= distanceLimit && timeCheck) {
        vendor.getSaleCard().setDistanceFromClient(distanceClientVendor);
        nearbyVendors.add(vendor);
      }    
    }

    return nearbyVendors;
  }

  /**
   * Returns true if the time is between the salecard's opening hours
   * E.g. if startTime after endTime, requestTime between (startTime, 23:59] or [00:00, endTime)
   * else, requestTime between (startTime, endTime)
   */
  private boolean isBetweenOpeningHours(LocalTime start, LocalTime end, LocalTime requestTime) {
    return (start.isAfter(end))
        ? requestTime.isAfter(start) || requestTime.isBefore(end)
        : requestTime.isAfter(start) && requestTime.isBefore(end);
  }
}