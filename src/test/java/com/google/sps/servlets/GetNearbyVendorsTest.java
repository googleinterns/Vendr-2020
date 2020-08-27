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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.COMMONS;
import com.google.sps.servlets.GetNearbyVendorsServlet;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class GetNearbyVendorsTest {
  
  // Request parameters names
  private static final String PARAM_DELIVERY = "hasDelivery";
  private static final String PARAM_OPEN = "onlyOpenNow";
  private static final String PARAM_TIME = "currentTime";
  private static final String PARAM_DISTANCE = "distance";
  private static final String PARAM_LATITUDE = "lat";
  private static final String PARAM_LONGITUDE = "lng";

  // Delivery and open values
  private static final String TRUE = "true";

  // Time values
  private static final String TIME_1200 = "12:00";
  private static final String TIME_BAD_FORMAT = "26:00";

  // Distance values
  private static final String DISTANCE_1_KM = "1000";
  private static final String DISTANCE_BAD_FORMAT = "1000.5";
  private static final String DISTANCE_MORE_THAN_MAX = Integer.toString(COMMONS.MAX_DISTANCE_CLIENT + 1);
  private static final String DISTANCE_LESS_THAN_MIN = Integer.toString(COMMONS.MIN_DISTANCE - 1);

  // Latitude and longitude values (NL = NULL ISLAND {lat: 0, lng: 0})
  private static final String LAT_LNG_NULL_ISLAND = "0";
  private static final String LAT_BAD_VALUE = "95";
  private static final String LNG_BAD_VALUE = "195";

  // Response expected values
  private static final String CONTENT_JSON = "application/json;";
  private static final String UTF_8 = "UTF-8";
  private static final String EMPTY_GSON = "[]";

  private static final String EMPTY_STRING = "";

  private static final GetNearbyVendorsServlet servlet = new GetNearbyVendorsServlet();
  private static final LocalServiceTestHelper datastoreHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private static HttpServletRequest mockedRequest;
  private static HttpServletResponse mockedResponse;

  @BeforeClass
  public static void setUpDatastore() {
    datastoreHelper.setUp();
  }

  @AfterClass
  public static void tearDownDatastore() {
    datastoreHelper.tearDown();
  }

  @Before
  public void setUp() {
    mockedRequest = mock(HttpServletRequest.class);
    mockedResponse = mock(HttpServletResponse.class);
    when(mockedRequest.getParameter(PARAM_DELIVERY)).thenReturn(TRUE);
    when(mockedRequest.getParameter(PARAM_OPEN)).thenReturn(TRUE);
    when(mockedRequest.getParameter(PARAM_TIME)).thenReturn(TIME_1200);
    when(mockedRequest.getParameter(PARAM_DISTANCE)).thenReturn(DISTANCE_1_KM);
    when(mockedRequest.getParameter(PARAM_LATITUDE)).thenReturn(LAT_LNG_NULL_ISLAND);
    when(mockedRequest.getParameter(PARAM_LONGITUDE)).thenReturn(LAT_LNG_NULL_ISLAND);
  }

  private void verifyBadRequest(HttpServletResponse mockedResponse) throws IOException {
    verify(mockedResponse, only()).sendError(HttpServletResponse.SC_BAD_REQUEST);
  }

  private void verifyGoodResponse(HttpServletResponse mockedResponse, PrintWriter mockedWriter) {
    verify(mockedResponse).setContentType(CONTENT_JSON);
    verify(mockedResponse).setCharacterEncoding(UTF_8);
    verify(mockedWriter).println(EMPTY_GSON);
  }

  @Test
  public void emptyParamDelivery() throws IOException {
    // Empty parameter delivery is set to an arbitrary default value (true | false)
    when(mockedRequest.getParameter(PARAM_DELIVERY)).thenReturn(EMPTY_STRING);
    PrintWriter mockedWriter = mock(PrintWriter.class);
    when(mockedResponse.getWriter()).thenReturn(mockedWriter);

    servlet.doPost(mockedRequest, mockedResponse);

    verifyGoodResponse(mockedResponse, mockedWriter);
  }

  @Test
  public void emptyParamOpen() throws IOException {
    // Empty parameter open now is set to an arbitrary default value (true | false)
    when(mockedRequest.getParameter(PARAM_OPEN)).thenReturn(EMPTY_STRING);
    PrintWriter mockedWriter = mock(PrintWriter.class);
    when(mockedResponse.getWriter()).thenReturn(mockedWriter);

    servlet.doPost(mockedRequest, mockedResponse);

    verifyGoodResponse(mockedResponse, mockedWriter);
  }

  @Test
  public void emptyParamTime() throws IOException {
    when(mockedRequest.getParameter(PARAM_TIME)).thenReturn(EMPTY_STRING);
    servlet.doPost(mockedRequest, mockedResponse);
    verifyBadRequest(mockedResponse);
  }

  @Test
  public void emptyParamDistance() throws IOException {
    // Empty parameter distance is set to an arbitrary default value
    when(mockedRequest.getParameter(PARAM_DISTANCE)).thenReturn(EMPTY_STRING);
    PrintWriter mockedWriter = mock(PrintWriter.class);
    when(mockedResponse.getWriter()).thenReturn(mockedWriter);

    servlet.doPost(mockedRequest, mockedResponse);

    verifyGoodResponse(mockedResponse, mockedWriter);
  }

  @Test
  public void emptyParamLatLng() throws IOException {
    when(mockedRequest.getParameter(PARAM_LATITUDE)).thenReturn(EMPTY_STRING);
    when(mockedRequest.getParameter(PARAM_LONGITUDE)).thenReturn(EMPTY_STRING);
    servlet.doPost(mockedRequest, mockedResponse);
    verifyBadRequest(mockedResponse);
  }

  @Test
  public void badParamTime() throws IOException {
    when(mockedRequest.getParameter(PARAM_TIME)).thenReturn(TIME_BAD_FORMAT);
    servlet.doPost(mockedRequest, mockedResponse);
    verifyBadRequest(mockedResponse);
  }

  @Test
  public void badParamDistance() throws IOException {
    when(mockedRequest.getParameter(PARAM_DISTANCE)).thenReturn(DISTANCE_BAD_FORMAT);
    servlet.doPost(mockedRequest, mockedResponse);
    verifyBadRequest(mockedResponse);
  }

  @Test
  public void badParamLatitude() throws IOException {
    when(mockedRequest.getParameter(PARAM_LATITUDE)).thenReturn(LAT_BAD_VALUE);
    servlet.doPost(mockedRequest, mockedResponse);
    verifyBadRequest(mockedResponse);
  }

  @Test
  public void badParamLongitude() throws IOException {
    when(mockedRequest.getParameter(PARAM_LONGITUDE)).thenReturn(LNG_BAD_VALUE);
    servlet.doPost(mockedRequest, mockedResponse);
    verifyBadRequest(mockedResponse);
  }  

  @Test
  public void lessThanMinDistance() throws IOException {
    when(mockedRequest.getParameter(PARAM_DISTANCE)).thenReturn(DISTANCE_LESS_THAN_MIN);
    servlet.doPost(mockedRequest, mockedResponse);
    verifyBadRequest(mockedResponse);
  }

  @Test
  public void moreThanMaxDistance() throws IOException {
    when(mockedRequest.getParameter(PARAM_DISTANCE)).thenReturn(DISTANCE_MORE_THAN_MAX);
    servlet.doPost(mockedRequest, mockedResponse);
    verifyBadRequest(mockedResponse);
  }

  @Test
  public void allGoodParams() throws IOException {
    PrintWriter mockedWriter = mock(PrintWriter.class);
    when(mockedResponse.getWriter()).thenReturn(mockedWriter);

    servlet.doPost(mockedRequest, mockedResponse);

    verifyGoodResponse(mockedResponse, mockedWriter);
  }
}