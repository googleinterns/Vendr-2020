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
   * Return the request parameter or the default value if the parameter
   * was not specified by the client
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

  /** Returns a BlobKey for the uploaded file, or null if the user didn't upload a file. */
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

  /** Return true if the string contains only Unicode letters and at least one, otherwise false  */
  public static boolean hasOnlyLetters(String string) {
    Pattern validCharacters = Pattern.compile("\\p{L}+");
    Matcher validInputChecker = validCharacters.matcher(string);

    return validInputChecker.matches();
  }

  /** Return true if the string contains only numbers, otherwise false  */
  public static boolean hasOnlyNumbers(String string) {
    Pattern validCharacters = Pattern.compile("[0-9]+");
    Matcher validInputChecker = validCharacters.matcher(string);

    return validInputChecker.matches();
  }
}