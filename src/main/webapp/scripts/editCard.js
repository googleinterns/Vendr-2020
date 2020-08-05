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

const MAP_THEME = [
  {
    featureType: 'poi.business',
    stylers: [
      {
        visibility: 'off'
      }
    ]
  },
  {
    featureType: 'poi.park',
    elementType: 'labels.text',
    stylers: [
      {
        visibility: 'off'
      }
    ]
  }
];

/**
 * Map declaration and initial setup
 * At the beginning, retrieves the API_KEY from an external file
 */
const initMap = () => {
  jQuery.get('../../../../API_KEY.txt', function (textString) {
    const API_KEY = textString;

    // Create the script tag, set the appropriate attributes.
    const scriptMapTag = document.createElement('script');
    scriptMapTag.src = `https://maps.googleapis.com/maps/api/js?key=${API_KEY}&callback=drawMap`;
    scriptMapTag.defer = true;

    document.head.appendChild(scriptMapTag);
  });
};

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
  document.getElementById('photo-description').value = salecard.picture.alt;
  document.getElementById('business-lat').value = salecard.location.salepoint.lat;
  document.getElementById('business-lng').value = salecard.location.salepoint.lng;
  
  // After the card is inserted on the body we can init the map
  initMap();
};

/**
 * Callback function once the map is retrieved from Maps' API
 */
drawMap = () => {
  const map = new google.maps.Map(
    document.getElementById('edit-card-map'), { zoom: 15 });

  // Insert vendor location
  const vendorLocation = {
    lat: parseFloat(document.getElementById('business-lat').value),
    lng: parseFloat(document.getElementById('business-lng').value)
  };

  map.setCenter(vendorLocation);
  map.setOptions({ styles: MAP_THEME });

  // The marker, positioned at vendor's location
  const marker = new google.maps.Marker({
    map: map,
    position: vendorLocation,
    title: vendor.saleCard.businessName,
  });

  // Declare circle with radius of the delivery service of the vendor
  const vendorCircle = new google.maps.Circle({
    center: vendorLocation,
    fillColor: '#FF0000',
    fillOpacity: 0.20,
    map,
    radius: vendor.saleCard.hasDelivery
      ? vendor.saleCard.location.radius : 0,
    strokeColor: '#FF0000',
    strokeOpacity: 0.8,
    strokeWeight: 2
  });
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
    name: document.getElementById('business-name').value,
    description: document.getElementById('business-description').value,
    startTime: document.getElementById('business-startTime').value,
    endTime: document.getElementById('business-endTime').value,
    delivery: document.getElementById('delivery').value,
    distanceOfDelivery: 
      document.getElementById('business-distanceOfDelivery').value,
    photoDescription: document.getElementById('photo-description').value,
    lat: document.getElementById('business-lat').value,
    lng: document.getElementById('business-lng').value
  };
  fetch('/update-card', {method: 'POST', body: postParams})
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