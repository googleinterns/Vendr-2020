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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.dev.LocalUserService;

import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
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
  private static final String PARAM_CURRENT_BLOBKEY = "blobKey";
  private static final String PARAM_CURRENT_BLOBFILE = "imageFile";
  private static final String PARAM_IMAGE_DESCRIPTION = "altText";

  // Vendor Good values
  private static final String FIRST_NAME = "Ricardo";
  private static final String LAST_NAME = "Chapa";
  private static final String PHONE_NUMBER = "8118022347";
  private static final String BLOBKEY = "image.png";
  private static final String CURRENT_BLOBKEY = "current_blobkey";
  private static final String IMAGE_DESCRIPTION = "mockImageDescription";

  // Vendor Bad Values
  private static final String BAD_FIRST_NAME = "R$icardo";
  private static final String BAD_LAST_NAME = "Chap'a";
  private static final String BAD_PHONE_NUMBER = "8118 022347";

  private static final Vendor VENDOR_VERIFIED = new Vendor("1", "Vendor", "A", null, "8118022379", null, null);
  private static final Vendor VENDOR_NOT_VERIFIED = new Vendor("2", null, null, null, null, null, null);

  private static HttpServletRequest mockedRequest;
  private static HttpServletResponse mockedResponse;

  private static final UpdateVendorServlet updateVendorServlet = new UpdateVendorServlet();

  private static final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
        new LocalDatastoreServiceTestConfig(),new LocalUserServiceTestConfig(), new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
    fillDatastore();
    mockedRequest = mock(HttpServletRequest.class);
    mockedResponse = mock(HttpServletResponse.class);
    when(mockedRequest.getParameter(PARAM_FIRST_NAME)).thenReturn(FIRST_NAME);
    when(mockedRequest.getParameter(PARAM_LAST_NAME)).thenReturn(LAST_NAME);
    when(mockedRequest.getParameter(PARAM_PHONE_NUMBER)).thenReturn(PHONE_NUMBER);
    when(mockedRequest.getParameter(PARAM_CURRENT_BLOBKEY)).thenReturn(BLOBKEY);
    when(mockedRequest.getParameter(PARAM_CURRENT_BLOBFILE)).thenReturn(CURRENT_BLOBKEY);
    when(mockedRequest.getParameter(PARAM_IMAGE_DESCRIPTION)).thenReturn(IMAGE_DESCRIPTION);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // @Test
  // public void hasBadParameters() throws IOException {
  //   when(mockedRequest.getParameter(PARAM_FIRST_NAME)).thenReturn(BAD_FIRST_NAME);

  //   updateVendorServlet.doPost(mockedRequest, mockedResponse);

  //   verifyBadRequest(mockedResponse);
  // }

  @Test
  public void userIsNotLoggedIn() throws IOException {
    helper.setEnvIsLoggedIn(false);

    updateVendorServlet.doPost(mockedRequest, mockedResponse);
    verifyBadAccess(mockedResponse);
  }

  @Test
  public void allBadParameters() throws IOException {
    final boolean expected = true;
    final boolean actual = 
        updateVendorServlet.badInputs(BAD_FIRST_NAME, BAD_LAST_NAME, BAD_PHONE_NUMBER);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void allGoodParameters() throws IOException {
    final boolean expected = false;
    final boolean actual = 
        updateVendorServlet.badInputs(FIRST_NAME, LAST_NAME, PHONE_NUMBER);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void singleBadParameter() throws IOException {
    final boolean expected = true;
    final boolean actual = 
        updateVendorServlet.badInputs(FIRST_NAME, BAD_LAST_NAME, PHONE_NUMBER);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void getRegisteredVendor() {
    final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    final String registeredVendorId = VENDOR_VERIFIED.getId();
    final Key vendorKey = KeyFactory.createKey("Vendor", registeredVendorId);
    final Vendor actual = updateVendorServlet.getVendorEntity(datastore, registeredVendorId, vendorKey);
    Assert.assertEquals(actual, VENDOR_VERIFIED);
  }

   @Test
  public void getNotRegisteredVendor() {
    final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    final String notRegisteredVendorId = VENDOR_NOT_VERIFIED.getId();
    final Key vendorKey = KeyFactory.createKey("Vendor", notRegisteredVendorId);
    final Vendor actual = updateVendorServlet.getVendorEntity(datastore, notRegisteredVendorId, vendorKey);
    Assert.assertEquals(actual, VENDOR_NOT_VERIFIED);
  }

  private void verifyBadRequest(HttpServletResponse mockedResponse) throws IOException {
    verify(mockedResponse, only()).sendError(HttpServletResponse.SC_BAD_REQUEST);
  }

  private void verifyBadAccess(HttpServletResponse mockedResponse) throws IOException {
    verify(mockedResponse, only()).sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in.");
  }

   private static void fillDatastore() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(createEntityFromVendor(VENDOR_VERIFIED));
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
