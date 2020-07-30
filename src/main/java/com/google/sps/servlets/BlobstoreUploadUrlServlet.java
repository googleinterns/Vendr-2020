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

import com.google.appengine.api.blobstore.BlobstoreFailureException;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.sps.data.HttpServletUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that returns a URL to upload a file to Blobstore and handle the form
 */
@WebServlet("/blobstore-upload-url")
public class BlobstoreUploadUrlServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String formHandler = HttpServletUtils.getParameter(request, "formHandler", "");

    if (formHandler.isEmpty()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    try {
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      String uploadUrl = blobstoreService.createUploadUrl(formHandler);

      response.setContentType("text/html");
      response.getWriter().println(uploadUrl);
    } catch (BlobstoreFailureException e) {
      System.out.println("Problems communicating with Blobstore: " + e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid URL for form handler:" + e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
  }
}
