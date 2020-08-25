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

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static com.google.sps.data.HttpServletUtils.computeGeoDistance;
import static com.google.sps.data.HttpServletUtils.createEmbeddedEntity;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.LocationData;
import com.google.sps.data.SaleCard;
import com.google.sps.data.Vendor;
import com.google.sps.utility.GeoHash;
import com.google.sps.utility.NearbyVendorsQuery;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class NearbyVendorsQueryTest {

  // Time values
  private static final LocalTime TIME_0000 = LocalTime.parse("00:00");
  private static final LocalTime TIME_0800 = LocalTime.parse("08:00");
  private static final LocalTime TIME_1200 = LocalTime.parse("12:00");
  private static final LocalTime TIME_1600 = LocalTime.parse("16:00");

  // Distance values
  private static final int DIST_100_M = 100;
  private static final int DIST_500_M = 500;
  private static final int DIST_1_KM = 1 * 1000;
  private static final int DIST_20_KM = 20 * 1000;

  // Latitude and longitude values (NL = NULL ISLAND {lat: 0, lng: 0})
  private static final GeoPt GEO_PT_NL = new GeoPt(0, 0);

  // Location Data values
  private static final LocationData LOC_50_M_FROM_NL = createLocation(1, 0.0003179f);
  private static final LocationData LOC_250_M_FROM_NL = createLocation(2, 0.001589f);
  private static final LocationData LOC_500_M_FROM_NL = createLocation(3, 0.003179f);
  private static final LocationData LOC_750_M_FROM_NL = createLocation(4, 0.004769f);
  private static final LocationData LOC_15_KM_FROM_NL = createLocation(5, 0.095299f);

  // SaleCard values
  private static final SaleCard SCARD_50M =
      createSaleCard(1, "A", false, false, TIME_0800, TIME_1600, LOC_50_M_FROM_NL);
  private static final SaleCard SCARD_250M =
      createSaleCard(2, "B", false, false, TIME_0800, TIME_1600, LOC_250_M_FROM_NL);
  private static final SaleCard SCARD_500M =
      createSaleCard(3, "C", false, false, TIME_0800, TIME_1600, LOC_500_M_FROM_NL);
  private static final SaleCard SCARD_750M =
      createSaleCard(4, "D", false, false, TIME_0800, TIME_1600, LOC_750_M_FROM_NL);
  private static final SaleCard SCARD_15KM =
      createSaleCard(5, "E", false, false, TIME_0800, TIME_1600, LOC_15_KM_FROM_NL);
  private static final SaleCard SCARD_500M_DLV =
      createSaleCard(6, "F", true, false, TIME_0800, TIME_1600, LOC_500_M_FROM_NL);
  private static final SaleCard SCARD_500M_CLOSED =
      createSaleCard(7, "G", false, true, TIME_0800, TIME_1600, LOC_500_M_FROM_NL);
  private static final SaleCard SCARD_500M_DLV_CLOSED =
      createSaleCard(8, "H", true, true, TIME_0800, TIME_1600, LOC_500_M_FROM_NL);
  private static final SaleCard SCARD_500M_NIGHT_TIME =
      createSaleCard(9, "I", false, false, TIME_1600, TIME_0800, LOC_500_M_FROM_NL);

  // Vendors
  private static final Vendor VENDOR_50M = new Vendor("1", "Vendor", "A", null, null, null, SCARD_50M);
  private static final Vendor VENDOR_250M = new Vendor("2", "Vendor", "B", null, null, null, SCARD_250M);
  private static final Vendor VENDOR_500M = new Vendor("3", "Vendor", "C", null, null, null, SCARD_500M);
  private static final Vendor VENDOR_750M = new Vendor("4", "Vendor", "D", null, null, null, SCARD_750M);
  private static final Vendor VENDOR_15KM = new Vendor("5", "Vendor", "E", null, null, null, SCARD_15KM);
  private static final Vendor VENDOR_DLV = new Vendor("6", "Vendor", "F", null, null, null, SCARD_500M_DLV);
  private static final Vendor VENDOR_CLOSED = new Vendor("7", "Vendor", "G", null, null, null, SCARD_500M_CLOSED);
  private static final Vendor VENDOR_DLV_CLOSED = new Vendor("8", "Vendor", "H", null, null, null, SCARD_500M_DLV_CLOSED);
  private static final Vendor VENDOR_NIGHT_TIME = new Vendor("9", "Vendor", "I", null, null, null, SCARD_500M_NIGHT_TIME);

  private static final LocalServiceTestHelper datastoreHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private NearbyVendorsQuery nearbyVendorsQuery;

  @BeforeClass
  public static void setUpDatastore() {
    datastoreHelper.setUp();
    fillDatastore();
  }

  @AfterClass
  public static void tearDownDatastore() {
    datastoreHelper.tearDown();
  }

  @Before
  public void setUp() {
    nearbyVendorsQuery = new NearbyVendorsQuery();
  }

  @Test
  public void checkDatastore() {
    // Check our local datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Assert.assertEquals(9, datastore.prepare(new Query("Vendor")).countEntities(withLimit(10)));
  }

  @Test
  public void query100MetersNoFilters() {
    // Vendors within 100 meters are retrieved
    List<String> geoHashesToQuery = GeoHash.getHashesToQuery(0, 0, DIST_100_M);
    Vendor[] expected = new Vendor[]{ VENDOR_50M };
    List<Vendor> actual = new ArrayList<>();
    for(String prefixGeoHash : geoHashesToQuery) {
      actual.addAll(nearbyVendorsQuery.query(prefixGeoHash, false, false, GEO_PT_NL, DIST_100_M, TIME_1200));
    }

    Assert.assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void query500MetersNoFilters() {
    // Vendors within 500 meters are retrieved
    List<String> geoHashesToQuery = GeoHash.getHashesToQuery(0, 0, DIST_500_M);
    Vendor[] expected = new Vendor[]{ VENDOR_50M, VENDOR_250M, VENDOR_500M, VENDOR_DLV, VENDOR_CLOSED,
        VENDOR_DLV_CLOSED, VENDOR_NIGHT_TIME };
    List<Vendor> actual = new ArrayList<>();
    for(String prefixGeoHash : geoHashesToQuery) {
      actual.addAll(nearbyVendorsQuery.query(prefixGeoHash, false, false, GEO_PT_NL, DIST_500_M, TIME_1200));
    }

    Assert.assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void query1KilometerNoFilters() {
    // Vendors within 1 kilometer are retrieved
    List<String> geoHashesToQuery = GeoHash.getHashesToQuery(0, 0, DIST_1_KM);
    Vendor[] expected = new Vendor[] { VENDOR_50M, VENDOR_250M, VENDOR_500M, VENDOR_750M,
        VENDOR_DLV, VENDOR_CLOSED, VENDOR_DLV_CLOSED, VENDOR_NIGHT_TIME };
    List<Vendor> actual = new ArrayList<>();
    for(String prefixGeoHash : geoHashesToQuery) {
      actual.addAll(nearbyVendorsQuery.query(prefixGeoHash, false, false, GEO_PT_NL, DIST_1_KM, TIME_1200));
    }

    Assert.assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void query20KilometersNoFilters() {
    // Vendors within 20 kilometers are retrieved
    List<String> geoHashesToQuery = GeoHash.getHashesToQuery(0, 0, DIST_20_KM);
    Vendor[] expected = new Vendor[]{ VENDOR_50M, VENDOR_250M, VENDOR_500M, VENDOR_750M, VENDOR_15KM,
        VENDOR_DLV, VENDOR_CLOSED, VENDOR_DLV_CLOSED, VENDOR_NIGHT_TIME};
    List<Vendor> actual = new ArrayList<>();
    for(String prefixGeoHash : geoHashesToQuery) {
      actual.addAll(nearbyVendorsQuery.query(prefixGeoHash, false, false, GEO_PT_NL, DIST_20_KM, TIME_1200));
    }

    Assert.assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void query500MetersOnlyWithDelivery() {
    // Only vendors with delivery service (and within 500 meters) are retrieved
    List<String> geoHashesToQuery = GeoHash.getHashesToQuery(0, 0, DIST_500_M);
    Vendor[] expected = new Vendor[]{ VENDOR_DLV, VENDOR_DLV_CLOSED};
    List<Vendor> actual = new ArrayList<>();
    for(String prefixGeoHash : geoHashesToQuery) {
      actual.addAll(nearbyVendorsQuery.query(prefixGeoHash, true, false, GEO_PT_NL, DIST_500_M, TIME_1200));
    }

    Assert.assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void query500MetersOnlyOpenNow() {
    // Only vendors currently open (and within 500 meters) are retrieved
    List<String> geoHashesToQuery = GeoHash.getHashesToQuery(0, 0, DIST_500_M);
    Vendor[] expected = new Vendor[]{ VENDOR_50M, VENDOR_250M, VENDOR_500M, VENDOR_DLV };
    List<Vendor> actual = new ArrayList<>();
    for(String prefixGeoHash : geoHashesToQuery) {
      actual.addAll(nearbyVendorsQuery.query(prefixGeoHash, false, true, GEO_PT_NL, DIST_500_M, TIME_1200));
    }

    Assert.assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void query500MetersDeliveryAndOpenNow() {
    // Only vendors with delivery service and currently open (and within 500 meters) are retrieved
    List<String> geoHashesToQuery = GeoHash.getHashesToQuery(0, 0, DIST_500_M);
    Vendor[] expected = new Vendor[]{ VENDOR_DLV };
    List<Vendor> actual = new ArrayList<>();
    for(String prefixGeoHash : geoHashesToQuery) {
      actual.addAll(nearbyVendorsQuery.query(prefixGeoHash, true, true, GEO_PT_NL, DIST_500_M, TIME_1200));
    }

    Assert.assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void query500MetersNightTime() {
    // Only vendors currently open at midnight (and within 500 meters) are retrieved
    List<String> geoHashesToQuery = GeoHash.getHashesToQuery(0, 0, DIST_500_M);
    Vendor[] expected = new Vendor[]{ VENDOR_NIGHT_TIME };
    List<Vendor> actual = new ArrayList<>();
    for(String prefixGeoHash : geoHashesToQuery) {
      actual.addAll(nearbyVendorsQuery.query(prefixGeoHash, false, true, GEO_PT_NL, DIST_500_M, TIME_0000));
    }

    Assert.assertThat(actual, containsInAnyOrder(expected));
  }

  private static void fillDatastore() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(createEntityFromVendor(VENDOR_50M));
    datastore.put(createEntityFromVendor(VENDOR_250M));
    datastore.put(createEntityFromVendor(VENDOR_500M));
    datastore.put(createEntityFromVendor(VENDOR_750M));
    datastore.put(createEntityFromVendor(VENDOR_15KM));
    datastore.put(createEntityFromVendor(VENDOR_DLV));
    datastore.put(createEntityFromVendor(VENDOR_CLOSED));
    datastore.put(createEntityFromVendor(VENDOR_DLV_CLOSED));
    datastore.put(createEntityFromVendor(VENDOR_NIGHT_TIME));
  }

  private static Entity createEntityFromVendor(Vendor vendor) {
    SaleCard salecard = vendor.getSaleCard();
    LocationData location = salecard.getLocation();

    Entity locationEntity = new Entity("Location", location.getId());
    locationEntity.setProperty("salePoint", location.getSalePoint());
    locationEntity.setProperty("geoHash", location.getGeoHash());
    locationEntity.setProperty("radius", 0f);
    
    Entity salecardEntity = new Entity("SaleCard", salecard.getId());
    salecardEntity.setProperty("businessName", salecard.getBusinessName());
    salecardEntity.setProperty("description", salecard.getDescription());
    salecardEntity.setProperty("hasDelivery", salecard.hasDelivery());
    salecardEntity.setProperty("isTemporarilyClosed", salecard.isTemporarilyClosed());
    salecardEntity.setProperty("startTime", salecard.getStartTime().toString());
    salecardEntity.setProperty("endTime", salecard.getEndTime().toString());
    salecardEntity.setProperty("picture", salecard.getPicture());
    salecardEntity.setIndexedProperty("location", createEmbeddedEntity(locationEntity));

    Entity vendorEntity = new Entity("Vendor", vendor.getId());
    vendorEntity.setProperty("firstName", vendor.getFirstName());
    vendorEntity.setProperty("lastName", vendor.getLastName());
    vendorEntity.setProperty("phoneNumber", vendor.getPhoneNumber());
    vendorEntity.setProperty("email", vendor.getEmail());
    vendorEntity.setProperty("profilePic", vendor.getProfilePic());
    vendorEntity.setIndexedProperty("saleCard", createEmbeddedEntity(salecardEntity));
    return vendorEntity;
  }

  private static LocationData createLocation(long id, float latLng) {
    return new LocationData(id, new GeoPt(latLng, latLng), GeoHash.encodeVendor(latLng, latLng), 0f);
  }

  private static SaleCard createSaleCard(long id, String name, boolean delivery, boolean closed,
      LocalTime start, LocalTime end, LocationData location) {
    SaleCard salecard = new SaleCard(id, name, "xyz", delivery, closed, start, end, location, null);
    salecard.setDistanceFromClient(computeGeoDistance(GEO_PT_NL, location.getSalePoint())); 
    return salecard;
  }
}