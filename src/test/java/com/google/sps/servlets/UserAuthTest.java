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

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.google.gson.Gson;
import com.google.sps.data.AuthStatus;
import com.google.sps.data.Vendor;
import com.google.sps.servlets.AuthServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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
  
  private static final String URL_LOGIN = "/_ah/login?continue\u003d%2F";
  private static final String URL_LOGOUT = "/_ah/logout?continue\u003d%2F";

  // AuthStatus Expected Values
  private static final AuthStatus USER_NOT_LOGGED_IN = 
    new AuthStatus(URL_LOGIN, false, false);
  private static final AuthStatus USER_NOT_REGISTERED = 
    new AuthStatus(URL_LOGOUT, true, false);
  private static final AuthStatus USER_REGISTERED = 
    new AuthStatus(URL_LOGOUT, true, true);

  // JSON of AuthStatus expected Values
  private static final String CONTENT_JSON = "application/json;";
  private static final String AUTH_JSON_USER_NOT_REGISTERED = new Gson().toJson(USER_NOT_REGISTERED);
  private static final String AUTH_JSON_USER_REGISTERED = new Gson().toJson(USER_REGISTERED);
  private static final String AUTH_JSON_USER_NOT_LOGGED_IN = new Gson().toJson(USER_NOT_LOGGED_IN);

  // Mock Vendors to Verify
  private static final Vendor VENDOR_VERIFIED = 
    new Vendor("1", "Vendor", "A", null, "8118022379", null, null);
  private static final Vendor VENDOR_NOT_VERIFIED = 
    new Vendor("2", "Vendor", "B", null, null, null, null);

  private static final AuthServlet authServlet = new AuthServlet();

  private static HttpServletRequest mockedRequest;
  private static HttpServletResponse mockedResponse;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(),
          new LocalUserServiceTestConfig())
          .setEnvEmail("test@gmail.com")
          .setEnvAuthDomain("gmail.com")
          .setEnvAttributes(new HashMap<String, Object>(){{
            put("com.google.appengine.api.users.UserService.user_id_key", "1");
          }});

  @After
  public void tearDownAuth() {
    helper.tearDown();
  }

  @Before
  public void setUp() {
    helper.setUp();
    fillDatastore();
    mockedRequest = mock(HttpServletRequest.class);
    mockedResponse = mock(HttpServletResponse.class);
  }

  @Test
  public void userIsNotLoggedIn() throws IOException {
    helper.setEnvIsLoggedIn(false);

    PrintWriter mockedWriter = mock(PrintWriter.class);
    when(mockedResponse.getWriter()).thenReturn(mockedWriter);
    authServlet.doGet(mockedRequest, mockedResponse);
    verifyGoodResponse(mockedResponse, mockedWriter, AUTH_JSON_USER_NOT_LOGGED_IN);
  }

  @Test
  public void registredUser() throws IOException {
    helper.setEnvIsLoggedIn(true);

    PrintWriter mockedWriter = mock(PrintWriter.class);
    when(mockedResponse.getWriter()).thenReturn(mockedWriter);
    authServlet.doGet(mockedRequest, mockedResponse);
    verifyGoodResponse(mockedResponse, mockedWriter, AUTH_JSON_USER_REGISTERED);
  }

  @Test
  public void notRegistredUser() throws IOException {
    helper = new LocalServiceTestHelper(
        new LocalDatastoreServiceTestConfig(),
        new LocalUserServiceTestConfig())
        .setEnvIsLoggedIn(true)
        .setEnvEmail("test@gmail.com")
        .setEnvAuthDomain("gmail.com")
        .setEnvAttributes(new HashMap<String, Object>(){{
          put("com.google.appengine.api.users.UserService.user_id_key", "2");
        }}).setUp();

    PrintWriter mockedWriter = mock(PrintWriter.class);
    when(mockedResponse.getWriter()).thenReturn(mockedWriter);
    authServlet.doGet(mockedRequest, mockedResponse);
    verifyGoodResponse(mockedResponse, mockedWriter, AUTH_JSON_USER_NOT_REGISTERED);
  }

  @Test
  public void userDoesNotExist() throws IOException{
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

  private void verifyGoodResponse(
      HttpServletResponse mockedResponse, PrintWriter mockedWriter, String AuthJson) {
    verify(mockedResponse).setContentType(CONTENT_JSON);
    verify(mockedWriter).println(AuthJson);
  }
}
