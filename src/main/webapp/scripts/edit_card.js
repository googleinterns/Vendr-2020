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
 * Query salecard from servlet
 */
async function querySalecard() {
  let vendor;
  await fetch('/get-vendor', {method: 'POST'})
      .then(response => response.json())
      .then(vendorEntity => {
        vendor = vendorEntity;
      });
  showVendorData(vendor);

  drawMap(getVendorInfo());
}

/**
 * Function that shows vendor information in the form
 */
function showVendorData(vendor) {
  let salecard = vendor.saleCard;

  document.getElementById('business-picture').src
      = `/serve-blob?blobKey=${vendor.saleCard.picture.blobKey.blobKey}`;
  document.getElementById('altText').alt
      = vendor.saleCard.picture.altText;
  document.getElementById('blobKey').value = salecard.picture.blobKey.blobKey;
  document.getElementById('businessName').value = salecard.businessName;
  document.getElementById('description').value = salecard.description;
  document.getElementById('startTime').value = parseTime(salecard.startTime);
  document.getElementById('endTime').value = parseTime(salecard.endTime);
  document.getElementById('hasDelivery').checked = salecard.hasDelivery;
  document.getElementById('hasDelivery').value = salecard.hasDelivery;
  document.getElementById('radius').value = salecard.location.radius;
  document.getElementById('altText').value = salecard.picture.altText;
  document.getElementById('lat').value = salecard.location.salePoint.latitude;
  document.getElementById('lng').value = salecard.location.salePoint.longitude;
}

/**
 * Update Location and refreshMap
 */
async function refreshLocation() {
  await updateLocation();
  drawMap(getVendorInfo());
}

/**
 * Function that returns vendor information object to draw a map.
 */
function getVendorInfo() {
  // Create object to draw the map.
  const vendorInfo = {
    latitude: parseFloat(document.getElementById('lat').value),
    longitude: parseFloat(document.getElementById('lng').value),
    radius: Number(document.getElementById('radius').value),
    markerName: document.getElementById('businessName').value,
    type: 'vendor',
    drawCircleRadius: document.getElementById('hasDelivery').checked
  };

  return vendorInfo;
}

/**
 * When window.load retrieve the card
 */
window.onload = () => {
  fetch('/blobstore-upload-url?formHandler=/add-saleCard')
      .then((response) => {
        return response.text();
      }).then((formUrl) => {
    document.getElementById('form-card').action = formUrl;
  });

  initMap();
};