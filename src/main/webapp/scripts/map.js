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

  // Object to draw the map
  const clientInfo = {
    latitude: parseFloat(document.getElementById('lat').value),
    longitude: parseFloat(document.getElementById('lng').value),
    radius: Number(document.getElementById('distance').value),
    markerName: 'Your Position',
    type: 'client'
  }

  // Draw client information
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
      .then(response => response.json())
      .then(nearbyVendors => {
        displayNumberOfVendors(nearbyVendors.length);

        // Add vendor's marker and modal to the map
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
    let vendor = nearbyVendors[vendorNumber];
    let salecard = vendor.saleCard;

    createModal(vendor, salecard, salecardTemplate, salecardsContainer);

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

/**
 * Function that creates a modal given a salecard
 * @param {Object} vendor
 * @param {Object} salecard
 * @param {Element} salecardTemplate
 * @param {Element} salecardsContainer
 */
const createModal = (vendor, salecard, salecardTemplate, salecardsContainer) => {
  let salecardCloned = salecardTemplate.content.cloneNode(true);

  salecardCloned.getElementById('business-picture').src
      = `/serve-blob?blobKey=${vendor.saleCard.picture.blobKey.blobKey}`;
  salecardCloned.getElementById('business-picture').alt = vendor.saleCard.picture.altText;
  salecardCloned.getElementById('business-title').textContent = salecard.businessName;
  salecardCloned.getElementById('business-name').textContent = salecard.businessName;
  salecardCloned.getElementById('business-description').textContent = salecard.description;
  salecardCloned.getElementById('vendor-name').textContent = `${vendor.firstName} ${vendor.lastName}`;
  salecardCloned.getElementById('vendor-phone').textContent = vendor.phoneNumber;
  salecardCloned.getElementById('vendor-distance').textContent = `${salecard.distanceFromClient.toFixed(2)}m`;
  salecardCloned.getElementById('vendor-salecard-btn').setAttribute('href', `viewCard.html?id=${vendor.id}`);
  salecardCloned.getElementById('myModal').id = `modal${vendor.id}`;

  salecardsContainer.appendChild(salecardCloned);
};

window.onload = () => {
  initMap();
};