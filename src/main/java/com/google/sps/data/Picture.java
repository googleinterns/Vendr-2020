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

import com.google.appengine.api.blobstore.BlobKey;

/** A picture. */
public final class Picture {

  private final long id;
  private BlobKey blobKey;
  private String altText;

  public Picture(long id, BlobKey blobKey, String altText) {
    this.id = id;
    this.blobKey = blobKey;
    this.altText = altText;
  }

  public long getId() {
    return id;
  }

  public BlobKey getBlobKey() {
    return blobKey;
  }

  public String getAltText() {
    return altText;
  }

  public void setBlobKey(BlobKey blobKey) {
    this.blobKey = blobKey;
  }

  public void setAltText(String altText) {
    this.altText = altText;
  }
}