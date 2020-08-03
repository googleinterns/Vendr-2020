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

/**
* Class object define the Authentication status of a user
*/
public class AuthStatus {
  private String url;
  private boolean isLogged;
  private boolean isRegistered;
  
  /** Constructor() */
  public AuthStatus() {
    this.url = "";
    this.isLogged = false;
    this.isRegistered = false;
  }
  
  /** Constructor(String, Boolean, Boolean) */
  public AuthStatus(String url, boolean isLogged, boolean isRegistered) {
    this.url = url;
    this.isLogged = isLogged;
    this.isRegistered = isRegistered;
  }

  public String getUrl() {
    return this.url;
  }

  public boolean getVerificationStatus() {
    return this.isLogged;
  }

  public boolean getRegistrationStatus() {
    return this.isRegistered;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setLoggedInStatus(boolean isLogged) {
    this.isLogged = isLogged;
  }

  public void setRegistrationStatus(boolean isRegistered) {
    this.isRegistered = isRegistered;
  }
}