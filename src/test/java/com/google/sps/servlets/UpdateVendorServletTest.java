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
import com.google.sps.servlets.UpdateVendorServlet;

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
public final class UpdateVendorServletTest {

  // Request parameters names
  private static final String PARAM_FIRST_NAME = "firstName";
  private static final String PARAM_LAST_NAME = "lastName";
  private static final String PARAM_PHONE_NUMBER = "phoneNumber";
  private static final String PARAM_CURRENT_BLOBKEY = "distance";
  private static final String PARAM_CURRENT_BLOBFILE = "";
  private static final String PARAM_IMAGE_DESCRIPTION = "";


  // Time values
  private static final String FIRST_NAME = "Ricardo";
  private static final String LAST_NAME = "Chapa";
  private static final String PHONE_NUMBER = "8118022347";

  // Blobstore Values
  private static final String BLOBKEY = "mock_key";

  private static final Vendor VENDOR_VERIFIED = new Vendor("1", "Vendor", "A", null, "8118022379", null, null);
  private static final Vendor VENDOR_NOT_VERIFIED = new Vendor("2", "Vendor", "B", null, null, null, null);

  private static HttpServletRequest mockedRequest;
  private static HttpServletResponse mockedResponse;

  private static final UpdateVendorServlet updateVendorServlet = new UpdateVendorServlet();

  private static final LocalServiceTestHelper datastoreHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private static final LocalServiceTestHelper authHelper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig());


  @Before
  public void setUp() {
    authHelper.setUp();
    mockedRequest = mock(HttpServletRequest.class);
    mockedResponse = mock(HttpServletResponse.class);
    when(mockedRequest.getParameter(PARAM_FIRST_NAME)).thenReturn(FIRST_NAME);
    when(mockedRequest.getParameter(PARAM_LAST_NAME)).thenReturn(LAST_NAME);
    when(mockedRequest.getParameter(PARAM_PHONE_NUMBER)).thenReturn(PHONE_NUMBER);
    when(mockedRequest.getParameter(PARAM_CURRENT_BLOBKEY)).thenReturn(BLOBKEY);
    when(mockedRequest.getParameter(PARAM_CURRENT_BLOBFILE)).thenReturn("");
    when(mockedRequest.getParameter(PARAM_IMAGE_DESCRIPTION)).thenReturn("");
  }

  @BeforeClass
  public static void setUpDatastore() {
    //datastoreHelper.setUp();
    //fillDatastore();
  }

  @After
  public void tearDown() {
    authHelper.tearDown();
  }

  @AfterClass
  public static void tearDownDatastore() {
    //datastoreHelper.tearDown();
  }

  @Test
  public void userIsNotLoggedIn() throws IOException {
    authHelper.setEnvIsLoggedIn(false);

    updateVendorServlet.doPost(mockedRequest, mockedResponse);
    verifyBadAccess(mockedResponse);
  }

  private void verifyBadAccess(HttpServletResponse mockedResponse) throws IOException {
    verify(mockedResponse, only()).sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in.");
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
}