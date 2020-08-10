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
 * TODO: fetch from servlet
 */
const querySalecard = () => {
  let salecard = vendor.saleCard;

  const salecardTemplate = document.getElementById('salecard-template');
  const salecardContainer = document.getElementById('salecard-container');

  //Clean previously retrieved cards
  salecardContainer.textContent = '';

  let salecardCloned = salecardTemplate.content.cloneNode(true);

  salecardCloned.getElementById('salecard-id').textContent = salecard.id;
  salecardCloned.getElementById('business-name').textContent = salecard.businessName;
  salecardCloned.getElementById('business-description').textContent = salecard.description;
  salecardCloned.getElementById('openedFrom').textContent = 
    `${salecard.startTime.hour}:${salecard.startTime.minute}:${salecard.startTime.second}`;
  salecardCloned.getElementById('openedTo').textContent = 
    `${salecard.endTime.hour}:${salecard.endTime.minute}:${salecard.endTime.second}`;
  salecardCloned.getElementById('deliveryDistance').textContent = salecard.location.radius;
  salecardCloned.getElementById('vendor-name').textContent = `${vendor.firstName} ${vendor.lastName}`;
  salecardCloned.getElementById('vendor-phone').textContent = vendor.phoneNumber;
  salecardCloned.getElementById('vendor-distance').textContent = `${salecard.distanceFromClient}m`;
  salecardCloned.getElementById('business-lat').value = salecard.location.salePoint.latitude;
  salecardCloned.getElementById('business-lng').value = salecard.location.salePoint.longitude;
  salecardContainer.appendChild(salecardCloned);

  // After the card is inserted on the body we can init the map
  initMap();
};

/**
 * When window.load retrieve the card
 */
 window.onload = () => {
  querySalecard();
 };