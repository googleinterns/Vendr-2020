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
 * Query salecard from servlet, draw map and show vendor's information.
 */
async function querySalecard() {
  await updateLocation();

  const params = new URLSearchParams();
  params.append('vendorId', getVendorId());
  params.append('lat', document.getElementById('lat').value);
  params.append('lng', document.getElementById('lng').value);

  let vendor;
  await fetch('/get-vendor?' + params)
      .then(response => response.json())
      .then(vendorEntity => {
        vendor = vendorEntity;
      });

  // Create object to draw the map.
  const vendorInfo = {
    latitude: parseFloat(vendor.saleCard.location.salePoint.latitude),
    longitude: parseFloat(vendor.saleCard.location.salePoint.longitude),
    radius: Number(vendor.saleCard.location.radius),
    markerName: vendor.saleCard.businessName,
    type: 'vendor',
    drawCircleRadius: vendor.saleCard.hasDelivery
  };

  showVendorInfo(vendor);
  drawMap(vendorInfo);
}

/**
 * Shows vendor's information in the page.
 * @param {object} vendor Retrieved from servlets.
 */
const showVendorInfo = (vendor) => {
  const salecardTemplate = document.getElementById('salecard-template');
  const salecardContainer = document.getElementById('salecard-container');

  // Clean previously retrieved cards.
  salecardContainer.textContent = '';

  let salecard = vendor.saleCard;
  let salecardCloned = salecardTemplate.content.cloneNode(true);

  salecardCloned.getElementById('business-picture').src
      = `/serve-blob?blobKey=${vendor.saleCard.picture.blobKey.blobKey}`;
  salecardCloned.getElementById('business-picture').alt = vendor.saleCard.picture.altText;
  salecardCloned.getElementById('salecard-id').textContent = salecard.id;
  salecardCloned.getElementById('business-name').textContent = salecard.businessName;
  salecardCloned.getElementById('business-description').textContent = salecard.description;
  salecardCloned.getElementById('openedFrom').textContent = parseTime(salecard.startTime);
  salecardCloned.getElementById('openedTo').textContent = parseTime(salecard.endTime);
  salecardCloned.getElementById('vendor-distance').textContent = `${salecard.distanceFromClient.toFixed(2)}m`;
  salecardCloned.getElementById('vendor-name').textContent = `${vendor.firstName} ${vendor.lastName}`;
  salecardCloned.getElementById('vendor-phone').textContent = vendor.phoneNumber;
  salecardCloned.getElementById('business-lat').value = salecard.location.salePoint.latitude;
  salecardCloned.getElementById('business-lng').value = salecard.location.salePoint.longitude;

  if (salecard.hasDelivery) {
    salecardCloned.getElementById('deliveryDistance').textContent =
        `${salecard.location.radius}m`;
  } else {
    const deliveryElement = salecardCloned.getElementById('deliveryElement');
    deliveryElement.classList.remove('alert-primary');
    deliveryElement.classList.add('alert-danger');
    deliveryElement.textContent = 'This vendor does not offer delivery service';
  }

  salecardContainer.appendChild(salecardCloned);
};

/**
 * When window.load retrieve the card
 */
window.onload = () => {
  initMap();
};
