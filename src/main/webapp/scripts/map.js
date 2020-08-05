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
$(function () {
  jQuery.get('../../../../API_KEY.txt', function (textString) {
    const API_KEY = textString;

    // Create the script tag, set the appropriate attributes.
    const scriptMapTag = document.createElement('script');
    scriptMapTag.src = `https://maps.googleapis.com/maps/api/js?key=${API_KEY}`;
    scriptMapTag.defer = true;

    document.head.appendChild(scriptMapTag);
  });
});

/**
 * Function that queries salecards from servlet.
 * TODO: implement fetch from servlet
 */
querySalecards = () => {
  const map = new google.maps.Map(
    document.getElementById('map'), { zoom: 15 });

  // Get client current location
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((position) => {
      const userPosition = {
        lat: position.coords.latitude,
        lng: position.coords.longitude
      };
      map.setCenter(userPosition);
      map.setOptions({ styles: MAP_THEME });

      // The marker, positioned at client's location
      const marker = new google.maps.Marker({
        icon: {
          url: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png'
        },
        map: map,
        position: userPosition,
        title: 'Your position'
      });

      // Declare circle that match user's query parameters
      const userCircle = new google.maps.Circle({
        center: userPosition,
        fillColor: '#FF0000',
        fillOpacity: 0.20,
        map,
        radius: parseInt(document.getElementById('distance').value),
        strokeColor: '#FF0000',
        strokeOpacity: 0.8,
        strokeWeight: 2
      });
      // Once cards are retrieved...
      // Add vendor's markers
      addVendorsToMap(map);
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
 * Function that adds vendor marks to the map
 */
const addVendorsToMap = (map) => {
  const salecardTemplate = document.getElementById('salecard-template');
  const salecardsContainer = document.getElementById('salecards-container');

  //Clean previously retrieved cards
  salecardsContainer.innerHTML = '';

  //Display number of vendor found
  displayNumberOfVendors(vendors);

  Object.keys(vendors).forEach(vendorNumber => {
    let vendor = vendors[vendorNumber];
    let salecard = vendor.saleCard;

    createModal(vendor, salecard, salecardTemplate, salecardsContainer);

    let marker = new google.maps.Marker({
      map: map,
      position: {
        lat: salecard.location.salepoint.lat,
        lng: salecard.location.salepoint.lng
      },
      title: salecard.businessName
    });

    // Toggles salecard's modal when vendor's marker is clicked
    marker.addListener('click', () => {
      $(`#modal${vendor.id}`).modal('toggle');
    })
  });
};

/**
 * Function that creates a modal given a salecard
 */
const createModal = (vendor, salecard, salecardTemplate, salecardsContainer) => {
  let salecardCloned = salecardTemplate.content.cloneNode(true);

  salecardCloned.getElementById('business-title').innerHTML = salecard.businessName;
  salecardCloned.getElementById('business-name').innerHTML = salecard.businessName;
  salecardCloned.getElementById('business-description').innerHTML = salecard.description;
  salecardCloned.getElementById('vendor-name').innerHTML = `${vendor.firstName} ${vendor.lastName}`;
  salecardCloned.getElementById('vendor-phone').innerHTML = vendor.phoneNumber;
  salecardCloned.getElementById('vendor-distance').innerHTML = `${salecard.location.distance}m`;
  salecardCloned.getElementById('vendor-salecard-btn').setAttribute('href', `viewCard.html?id=${salecard.id}`);
  salecardCloned.getElementById('myModal').id = `modal${vendor.id}`;

  salecardsContainer.appendChild(salecardCloned);
};

/**
 * Function that creates h3 element to display the number of vendors
 */
const displayNumberOfVendors = (vendors) => {
  const numberOfVendorsElement = document.createElement('h3');
  numberOfVendorsElement.innerHTML = `${vendors.length} vendors found.`;
  const containerElement = document.getElementById('numberOfVendors');
  containerElement.innerHTML = '';
  containerElement.appendChild(numberOfVendorsElement);
};