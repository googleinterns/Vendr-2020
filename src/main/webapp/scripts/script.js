// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Function to load the search vendors around bar
 */
$(() => {
  $('#searchBarVendors-placeholder').load('common/searchBarVendors.html');
});

/**
 * Function that creates h3 element to display the number of vendors
 * @param {int} vendorsLength the length of the array of vendors.
 */
const displayNumberOfVendors = (vendorsLength) => {
  const numberOfVendorsElement = document.createElement('h3');
  numberOfVendorsElement.textContent = (vendorsLength === 1)
    ? '1 vendor found.'
    : `${vendorsLength} vendors found.`;
  const containerElement = document.getElementById('numberOfVendors');
  containerElement.textContent = '';
  containerElement.appendChild(numberOfVendorsElement);
};

/**
 * Function that returns parameters to query nearby vendors
 * @returns {URLSearchParams} with parameters to query
 */
function getQueryParams() {
  const params = new URLSearchParams();

  params.append('hasDelivery', document.getElementById('hasDelivery').checked);
  params.append('distance', document.getElementById('distance').value);
  params.append('lat', document.getElementById('lat').value);
  params.append('lng', document.getElementById('lng').value);
  params.append('onlyOpenNow', document.getElementById('onlyOpenNow').checked);
  const today = new Date();
  params.append('currentTime', `${today.getHours()}:${today.getMinutes()}`);

  return params;
}

/**
 * Function that assign value when delivery is checked
 */
const updateDeliverySelection = () => {
  const DeliveryCheckBox = document.getElementById('hasDelivery');
  DeliveryCheckBox.value = DeliveryCheckBox.checked;
};

/**
 * Get vendor id in the URL
 * @returns {String} vendor's ID.
 */
function getVendorId() {
  const url = window.location.href;
  const id = url.split('id=');
  return id[1];
}

/**
 * Function that parse hours object to string
 * @param {Object} time Time retrieved from servlet
 * @returns {String} time in String format.
 */
function parseTime(time){
  let timeString = '';

  timeString += (time.hour < 10)
      ? '0' + time.hour + ':'
      : time.hour + ':';

  timeString += (time.minute < 10)
      ? '0' + time.minute
      : time.minute;

  return timeString;
}

/**
 * Function that inserts vendor's info given an HTML container
 * @param {Element} container - HTML container
 * @param {Element} template - template HTML element.
 * @param {Object} vendor - Vendor object that contains its info.
 */
function insertVendorInfo(container, template, vendor) {
  let salecard = vendor.saleCard;
  template.getElementById('business-picture').src
      = `/serve-blob?blobKey=${vendor.saleCard.picture.blobKey.blobKey}`;
  template.getElementById('business-picture').alt = vendor.saleCard.picture.altText;
  template.getElementById('business-name').textContent = salecard.businessName;
  template.getElementById('business-description').textContent = salecard.description;
  template.getElementById('vendor-name').textContent = `${vendor.firstName} ${vendor.lastName}`;
  template.getElementById('vendor-phone').textContent = vendor.phoneNumber;
  template.getElementById('vendor-distance').textContent = `${salecard.distanceFromClient.toFixed(2)}m`;
  template.getElementById('vendor-salecard-btn').setAttribute('href', `viewCard.html?id=${vendor.id}`);

  container.appendChild(template);
}
