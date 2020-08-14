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
  const salecardTemplate = document.getElementById('salecard-template');
  const salecardsContainer = document.getElementById('salecards-container');

  // Clean previously retrieved cards.
  salecardsContainer.textContent = '';

  Object.keys(nearbyVendors).forEach(vendorNumber => {
    const vendor = nearbyVendors[vendorNumber];
    const salecard = vendor.saleCard;

    const salecardCloned = salecardTemplate.content.cloneNode(true);
    salecardCloned.getElementById('myModal').id = `modal${vendor.id}`;
    insertVendorInfo(salecardsContainer, salecardCloned, vendor);

    const marker = new google.maps.Marker({
      map: map,
      position: {
        lat: salecard.location.salePoint.latitude,
        lng: salecard.location.salePoint.longitude
      },
      title: salecard.businessName
    });

    // Toggles salecard's modal when vendor's marker is clicked.
    marker.addListener('click', () => {
      $(`#modal${vendor.id}`).modal('toggle');
    })
  });
};

window.onload = () => {
  initMap();
};