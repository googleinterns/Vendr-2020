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

package com.google.sps.data;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

/** Servlets Utility Class. */
public final class HttpServletUtils {
  private HttpServletUtils() {
    throw new java.lang.UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Returns the request parameter or the default value if the parameter
   * was not specified by the client.
   * @param request a client request with parameter names and values for HTTP servlets
   * @param name a request parameter
   * @param defaultValue a default value for the request parameter
   * @return a String representing the value of the request parameter
   */
  public static String getParameter(HttpServletRequest request, String name, String defaultValue) {
    if (request == null) {
      return defaultValue;
    }
    
    String value = request.getParameter(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }

  /** 
   * Returns a BlobKey for the uploaded file, or null if the user didn't upload a file or 
   * it is not a image file. 
   * @param request a client request with parameter names and values for HTTP servlets
   * @param formInput the name of the input element in which the file was uploaded
   * @return a BlobKey representing the uploaded file, or null if not a valid file
   */
  public static BlobKey getUploadedFileBlobKey(HttpServletRequest request, String formInput) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInput);

    // User submitted form without selecting a file (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo == null || blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // Validate the file is an image file (.png, .jpg, .jpeg, .jfif, .pjpeg, .pjp)
    String blobMimeType = blobInfo.getContentType();
    if (blobMimeType.equals("image/png") || blobMimeType.equals("image/jpeg")) {
      return blobKey;
    } else {
      blobstoreService.delete(blobKey);
      return null;
    }
  }

  /** 
   * Return true if the string contains only Unicode letters and at least one, otherwise false.
   * @param string a String to check
   * @return a boolean indicating if the string contains only Unicode letters and at least one
   */
  public static boolean hasOnlyLetters(String string) {
    if (string == null) return false;
    Pattern validCharacters = Pattern.compile("\\p{L}+");
    Matcher validInputChecker = validCharacters.matcher(string);

    return validInputChecker.matches();
  }

  /** 
   * Returns true if the string contains only numbers and at least one, otherwise false.
   * @param string a String to check
   * @return a boolean indicating if the string contains only numbers and at least one
   */
  public static boolean hasOnlyNumbers(String string) {
    if (string == null) return false;
    Pattern validCharacters = Pattern.compile("[0-9]+");
    Matcher validInputChecker = validCharacters.matcher(string);

    return validInputChecker.matches();
  }

  /** 
   * Returns a vendor Entity if it exists in datastore, otherwise a null.
   * @param vendorId a String specifying the id of a vendor
   * @return a Entity found in datastore with the specified id, or null if not found
   */
  public static Entity getVendorEntity(String vendorId) {
    Key vendorKey = KeyFactory.createKey("Vendor", vendorId);
    Entity vendorEntity;
    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      vendorEntity = datastore.get(vendorKey);
      return vendorEntity;
    } catch (EntityNotFoundException e) {
      System.out.println(e);
      return null;
    }
  }

  /** 
   * Computes the distance between two geographical points using Haversine formula.
   * @param pointA a geographical point {latitude, longitude} representing A
   * @param pointB a geographical point {latitude, longitude} representing B
   * @return a float representing the distance between A and B
   */
  public static float computeGeoDistance(GeoPt pointA, GeoPt pointB) {
    double latitudeRadiansA = Math.toRadians(pointA.getLatitude());
    double latitudeRadiansB = Math.toRadians(pointB.getLatitude());

    double latitudeDifference = Math.toRadians(pointB.getLatitude() - pointA.getLatitude());
    double longitudeDifference = Math.toRadians(pointB.getLongitude() - pointA.getLongitude());

    double a = Math.pow(Math.sin(latitudeDifference / 2), 2) + 
        Math.cos(latitudeRadiansA) * Math.cos(latitudeRadiansB) * Math.pow(Math.sin(longitudeDifference / 2), 2);
    double c = 2 * Math.asin(Math.sqrt(a));

    return (float) (GeoPt.EARTH_RADIUS_METERS * c);
  }

  /**
   * Creates an embedded entity from the entity provided
   * @param entity an entity with a complete key
   * @return an embedded entity with the properties of the entity provided
   */
  public static EmbeddedEntity createEmbeddedEntity(Entity entity) {
    EmbeddedEntity embedded = new EmbeddedEntity();
    embedded.setKey(entity.getKey());
    embedded.setPropertiesFrom(entity);

    return embedded;
  }
}