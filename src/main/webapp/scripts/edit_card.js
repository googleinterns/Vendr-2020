// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Query salecard from servlet and insert values in the form
 * TODO: fetch from servlet
 * TODO: handle case where a vendor does not have a card registered
 */
const querySalecard = () => {
  let salecard = vendor.saleCard;

  document.getElementById('business-name').value = salecard.businessName;
  document.getElementById('business-description').value = salecard.description;
  document.getElementById('business-startTime').value = salecard.startTime;
  document.getElementById('business-endTime').value = salecard.endTime;
  document.getElementById('delivery').checked = salecard.hasDelivery;
  document.getElementById('business-distanceOfDelivery').value = salecard.location.radius;
  document.getElementById('photo-description').value = salecard.picture.altText;
  document.getElementById('business-lat').value = salecard.location.salePoint.latitude;
  document.getElementById('business-lng').value = salecard.location.salePoint.longitude;
  
  // After the card is inserted on the body we can init the map
  initMap();
};

/**
 * Update location to inserts current vendor's location
 */
const updateLocation = () => {
  // Get client current location
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((position) => {
      document.getElementById('business-lat').value = position.coords.latitude;
      document.getElementById('business-lng').value = position.coords.longitude;
      
      // Refresh current map
      drawMap();
    },
      (error) => {
        if (error.code == error.PERMISSION_DENIED)
          alert('Permission denied to access location');
      });
  } else {
    alert('This browser does not support location');
  }
};

/**
 * Function that submits business information
 */
const updateBusiness = () => {
  const postParams = {
    businessName: document.getElementById('business-name').value,
    description: document.getElementById('business-description').value,
    startTime: document.getElementById('business-startTime').value,
    endTime: document.getElementById('business-endTime').value,
    hasDelivery: document.getElementById('delivery').value,
    radius: 
      document.getElementById('business-distanceOfDelivery').value,
      altText: document.getElementById('photo-description').value,
    lat: document.getElementById('business-lat').value,
    long: document.getElementById('business-lng').value
  };
  fetch('/add-saleCard', {method: 'POST', body: postParams})
  .then(response => {
    if (response.redirected) {
      window.location.href = response.url;
      return;
    } else {
      // Show error message to the user.
      let errorMessage = document.getElementById('error-message');
      errorMessage.innerText = 'An error occurred updating business' +
        ' information, please try again.';
      $('#errorModal').modal('show');
    }
  });
};

/**
 * When window.load retrieve the card
 */
window.onload = () => {
  querySalecard();
};