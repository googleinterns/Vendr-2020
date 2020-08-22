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
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.sps.servlets.GetNearbyVendorsServlet;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public final class GetNearbyVendorsTest {
  
  // Request parameters names
  private static final String PARAM_DELIVERY = "hasDelivery";
  private static final String PARAM_OPEN = "onlyOpenNow";
  private static final String PARAM_TIME = "currentTime";
  private static final String PARAM_DISTANCE = "distance";
  private static final String PARAM_LATITUDE = "lat";
  private static final String PARAM_LONGITUDE = "lng";

  // Deliery and open values
  private static final String TRUE = "true";
  private static final String FALSE = "false";

  // Time values
  private static final String TIME_0000 = "00:00";
  private static final String TIME_0400 = "04:00";
  private static final String TIME_0800 = "08:00";
  private static final String TIME_1200 = "12:00";
  private static final String TIME_1600 = "16:00";
  private static final String TIME_2000 = "20:00";

  // Distance values
  private static final String DISTANCE_100_M = "100";
  private static final String DISTANCE_500_M = "500";
  private static final String DISTANCE_1_KM = "1000";
  private static final String DISTANCE_20_KM = "20000";

  // Latitude and longitude values (NL = NULL ISLAND {lat: 0, lng: 0})
  private static final String LAT_LNG_NULL_ISLAND = "0";
  private static final String LAT_LNG_50_M_FROM_NL = "0.0003179";
  private static final String LAT_LNG_250_M_FROM_NL = "0.001589";
  private static final String LAT_LNG_500_M_FROM_NL = "0.003179";
  private static final String LAT_LNG_750_M_FROM_NL = "0.004769";
  private static final String LAT_LNG_15_KM_FROM_NL = "0.095299";

  private final GetNearbyVendorsServlet servlet = new GetNearbyVendorsServlet();
  private final LocalServiceTestHelper datastoreHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
          // .setEnvIsAdmin(true).setEnvIsLoggedIn(true);

  @Before
  public void setUp() {
    datastoreHelper.setUp();
  }

  @After
  public void tearDown() {
    datastoreHelper.tearDown();
  }

  @Test
  public void emptyParamTime() throws IOException {
    HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse mockedResponse = Mockito.mock(HttpServletResponse.class);
    Mockito.when(mockedRequest.getParameter(PARAM_DELIVERY)).thenReturn(TRUE);
    Mockito.when(mockedRequest.getParameter(PARAM_OPEN)).thenReturn(TRUE);
    Mockito.when(mockedRequest.getParameter(PARAM_TIME)).thenReturn("");
    Mockito.when(mockedRequest.getParameter(PARAM_DISTANCE)).thenReturn(DISTANCE_1_KM);
    Mockito.when(mockedRequest.getParameter(PARAM_LATITUDE)).thenReturn(LAT_LNG_NULL_ISLAND);
    Mockito.when(mockedRequest.getParameter(PARAM_LONGITUDE)).thenReturn(LAT_LNG_NULL_ISLAND);

    servlet.doPost(mockedRequest, mockedResponse);

    Mockito.verify(mockedResponse).sendError(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void emptyParamLatLng() throws IOException {
    HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse mockedResponse = Mockito.mock(HttpServletResponse.class);
    Mockito.when(mockedRequest.getParameter(PARAM_DELIVERY)).thenReturn(TRUE);
    Mockito.when(mockedRequest.getParameter(PARAM_OPEN)).thenReturn(TRUE);
    Mockito.when(mockedRequest.getParameter(PARAM_TIME)).thenReturn(TIME_1200);
    Mockito.when(mockedRequest.getParameter(PARAM_DISTANCE)).thenReturn(DISTANCE_1_KM);
    Mockito.when(mockedRequest.getParameter(PARAM_LATITUDE)).thenReturn("");
    Mockito.when(mockedRequest.getParameter(PARAM_LONGITUDE)).thenReturn("");

    servlet.doPost(mockedRequest, mockedResponse);

    Mockito.verify(mockedResponse).sendError(HttpServletResponse.SC_BAD_REQUEST);
  }
}