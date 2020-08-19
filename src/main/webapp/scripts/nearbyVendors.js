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

// Global list of markers placed on the map
let MARKERS = new Map();

// Green dot mark for the map.
const HOVER_MARKER = 'https://mt.google.com/vt/icon?psize=30&font=fonts/arialuni_t.ttf&color=ff304C13&name=icons/spotlight/spotlight-waypoint-a.png&ax=43&ay=48&text=%E2%80%A2&scale=1.5';

// Vendor's default dot mark for the map.
const DEFAULT_MARKER = 'http://mt.googleapis.com/vt/icon/name=icons/spotlight/spotlight-poi.png';

/**
 * Function that queries salecards from servlet.
 */
async function querySalecard() {
  await updateLocation();

  // Object to draw the map.
  const clientInfo = {
    latitude: parseFloat(document.getElementById('lat').value),
    longitude: parseFloat(document.getElementById('lng').value),
    radius: Number(document.getElementById('distance').value),
    markerName: 'Your Position',
    type: 'client',
    drawCircleRadius: true
  }

  // Draw client information.
  const map = drawMap(clientInfo);

  // Add vendor's markers.
  fetchVendors(map);
}

/**
 * Function that adds vendor marks to the map
 * @param {Object} map
 */
const fetchVendors = (map) => {
  const params = getQueryParams();

  fetch('/get-nearby-vendors', {
    method: 'POST',
    body: params
  })
      .then((response) => {
        if (response.redirected) {
          window.location.href = response.url;
          return;
        }
        // Response status 200 = Servlet response OK
        if (response.status === 200) {
          return response.json();
        } else {
          alert(response);
          return;
        }
      })
      .then(nearbyVendors => {
        displayNumberOfVendors(nearbyVendors.length);

        // Add vendor's marker and modal to the map.
        addVendorsToMap(map, nearbyVendors);
      });
};

/**
 * Draws vendor's marker on the map
 * @param {Object} map where the markers will be placed.
 * @param {List<Object>} List of nearby vendors retrieved.
 */
const addVendorsToMap = (map, nearbyVendors) => {
  // Template and container for vendor's modal on the map.
  const salecardModalTemplate = document.getElementById('salecard-modal-template');
  const salecardsModalContainer = document.getElementById('salecards-modal-container');
  // Template and container for vendor's saleCard on the map.
  const salecardTemplate = document.getElementById('salecard-template');
  const salecardsContainer = document.getElementById('salecards-container');

  // Clean previously retrieved cards.
  salecardsModalContainer.textContent = '';
  salecardsContainer.textContent = '';

  Object.keys(nearbyVendors).forEach(vendorNumber => {
    const vendor = nearbyVendors[vendorNumber];
    const salecard = vendor.saleCard;

    // Create modal for vendor.
    const salecardModalCloned = salecardModalTemplate.content.cloneNode(true);
    salecardModalCloned.getElementById('myModal').id = `modal${vendor.id}`;
    insertVendorInfo(salecardsModalContainer, salecardModalCloned, vendor, true);

    // Create salecard for vendor.
    const salecardCloned = salecardTemplate.content.cloneNode(true);
    salecardCloned.getElementById('mySalecard').id = `salecard-${vendor.id}`;
    insertVendorInfo(salecardsContainer, salecardCloned, vendor, false);

    const marker = new google.maps.Marker({
      map: map,
      position: {
        lat: salecard.location.salePoint.latitude,
        lng: salecard.location.salePoint.longitude
      },
      icon: DEFAULT_MARKER,
      title: salecard.businessName
    });
    MARKERS[vendor.id] = marker;

    // Toggles salecard's modal when vendor's marker is clicked.
    marker.addListener('click', () => {
      $(`#modal${vendor.id}`).modal('toggle');
    });

    // Highlights marker of the vendor in the map when mouse enter.
    marker.addListener('mouseover', () => {
      marker.setIcon(HOVER_MARKER);
    });
    
    // unhighlights marker of the vendor in the map when mouse is out.
    marker.addListener('mouseout', () => {
      marker.setIcon(DEFAULT_MARKER);
    });
  });
};

/**
 * Highlights marker of the vendor in the map
 * @param {Element} Card element container of the vendor
 */
function highlightMarker(cardContainer) {
  const vendorId = getVendorId(cardContainer);
  MARKERS[vendorId].setIcon(HOVER_MARKER);
}

/**
 * Unhighlights marker of the vendor in the map
 * @param {Element} Card element container of the vendor
 */
function unhighlightMarker(cardContainer) {
  const vendorId = getVendorId(cardContainer);
  MARKERS[vendorId].setIcon(DEFAULT_MARKER);
}

/**
 * Get vendor id given a salecard element.
 * @param {Element} HTML container of the salecard
 * @returns {String} vendor's id.
 */
function getVendorId(cardContainer) {
  const id = cardContainer.id.split('-');
  return id[1];
}

/**
 * Initalize the map of the webpage
 */
window.onload = () => {
  initMap();
};
