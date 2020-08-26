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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.dev.LocalUserService;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;

import com.google.sps.data.AuthStatus;
import com.google.sps.data.Vendor;
import com.google.sps.servlets.AuthServlet;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class UserAuthTest {

  // private static final String REDIRECTION_URL = "/";

  // private static final AuthStatus USER_NOT_REGISTERED = 
  //     new AuthStatus("/_ah/login?continue\u003d%2F", false, false);

  // private static final AuthStatus USER_REGISTERED = 
  //     new AuthStatus("/_ah/login?continue\u003d%2F", false, false);

  // // Response expected values
  // private static final String CONTENT_JSON = "application/json;";
  // private String AUTH_JSON_USER_NR = new Gson().toJson(USER_NOT_REGISTERED);
  //  private String AUTH_JSON_USER_R = new Gson().toJson(USER_NOT_REGISTERED);

  private static final Vendor VENDOR_VERIFIED = new Vendor("1", "Vendor", "A", null, "8118022379", null, null);
  private static final Vendor VENDOR_NOT_VERIFIED = new Vendor("2", "Vendor", "B", null, null, null, null);

  private AuthServlet authServlet = new AuthServlet();

  // private static HttpServletRequest mockedRequest;
  // private static HttpServletResponse mockedResponse;

  private static final LocalServiceTestHelper datastoreHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  // private static final LocalServiceTestHelper authHelper =
  //     new LocalServiceTestHelper(new LocalUserServiceTestConfig());

  @BeforeClass
  public static void setUpDatastore() {
    datastoreHelper.setUp();
    fillDatastore();
  }

  @AfterClass
  public static void tearDownDatastore() {
    // authHelper.tearDown();
    datastoreHelper.tearDown();
  }

  @Before
  public void setUp() {
    // authHelper.setUp();
    // mockedRequest = mock(HttpServletRequest.class);
    // mockedResponse = mock(HttpServletResponse.class);
  }

  // @Test
  // public void userIsNotLoggedIn() throws IOException {
  //   authHelper.setEnvIsLoggedIn(false);

  //   PrintWriter mockedWriter = mock(PrintWriter.class);
  //   when(mockedResponse.getWriter()).thenReturn(mockedWriter);
  //   authServlet.doGet(mockedRequest, mockedResponse);
  //   verifyGoodResponse(mockedResponse, mockedWriter, AUTH_JSON_USER_NR);
  // }

  //  @Test
  // public void userIsLoggedIn() throws IOException {
  //   authHelper
  //     .setEnvIsLoggedIn(true)
  //     .setEnvEmail("ricardchr@google.com")
  //     .setEnvAppId("1");

  //   PrintWriter mockedWriter = mock(PrintWriter.class);
  //   when(mockedResponse.getWriter()).thenReturn(mockedWriter);
  //   authServlet.doGet(mockedRequest, mockedResponse);
  //   verifyGoodResponse(mockedResponse, mockedWriter, AUTH_JSON_USER_R);
  // }

  @Test
  public void userDoNotExist() throws IOException{
    final boolean expected = false;
    final boolean actual = authServlet.isUserRegistered(VENDOR_NOT_VERIFIED.getId());
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void userDoesExist() throws IOException{
    final boolean expected = true;
    final boolean actual = authServlet.isUserRegistered(VENDOR_VERIFIED.getId());
    Assert.assertEquals(expected, actual);
  }

   private static void fillDatastore() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(createEntityFromVendor(VENDOR_VERIFIED));
    datastore.put(createEntityFromVendor(VENDOR_NOT_VERIFIED));
  }

  private static Entity createEntityFromVendor(Vendor vendor) {
    Entity vendorEntity = new Entity("Vendor", vendor.getId());
    vendorEntity.setProperty("firstName", vendor.getFirstName());
    vendorEntity.setProperty("lastName", vendor.getLastName());
    vendorEntity.setProperty("phoneNumber", vendor.getPhoneNumber());
    vendorEntity.setProperty("email", vendor.getEmail());
    vendorEntity.setProperty("profilePic", vendor.getProfilePic());
    vendorEntity.setIndexedProperty("saleCard", null);
    return vendorEntity;
  }

  // private void verifyGoodResponse(
  //     HttpServletResponse mockedResponse, PrintWriter mockedWriter, String AuthJson) {
  //   verify(mockedResponse).setContentType(CONTENT_JSON);
  //   verify(mockedWriter).println(AuthJson);
  // }
}