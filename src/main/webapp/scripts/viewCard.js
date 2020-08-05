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
 * Query salecard from servlet
 * TODO: fetch from servlet
 */
const querySalecard = () => {
  let salecard = vendor.saleCard;

  const salecardTemplate = document.getElementById('salecard-template');
  const salecardContainer = document.getElementById('salecard-container');

  //Clean previously retrieved cards
  salecardContainer.innerHTML = '';

  let salecardCloned = salecardTemplate.content.cloneNode(true);

  salecardCloned.getElementById('salecard-id').innerHTML = salecard.id;
  salecardCloned.getElementById('business-name').innerHTML = salecard.businessName;
  salecardCloned.getElementById('business-description').innerHTML = salecard.description;
  salecardCloned.getElementById('openedFrom').innerHTML = salecard.startTime;
  salecardCloned.getElementById('openedTo').innerHTML = salecard.endTime;
  salecardCloned.getElementById('deliveryDistance').innerHTML = salecard.location.radius;
  salecardCloned.getElementById('vendor-name').innerHTML = `${vendor.firstName} ${vendor.lastName}`;
  salecardCloned.getElementById('vendor-phone').innerHTML = vendor.phoneNumber;
  salecardCloned.getElementById('vendor-distance').innerHTML = `${salecard.location.distance}m`;

  salecardContainer.appendChild(salecardCloned);

  // After the card is inserted on the body we can init the map
  initMap();
};

/**
 * Callback function once the map is retrieved from Maps' API
 */
drawMap = () => {
  const map = new google.maps.Map(
    document.getElementById('card-map'), { zoom: 15 });

  // Insert vendor location
  const vendorLocation = {
    lat: vendor.saleCard.location.salepoint.lat,
    lng: vendor.saleCard.location.salepoint.lng
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
 * When window.load retrieve the card
 */
 window.onload = () => {
  querySalecard();
 };