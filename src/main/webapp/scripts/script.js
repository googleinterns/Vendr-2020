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
 * Function that returns parameters to query neraby vendors
 * @returns {URLSearchParams} with parameters to query
 */
function getQueryParams() {
  const params = new URLSearchParams();
  updateDeliverySelection();

  params.append('hasDelivery', document.getElementById('hasDelivery').value);
  params.append('distance', document.getElementById('distance').value);
  params.append('lat', document.getElementById('lat').value);
  params.append('lng', document.getElementById('lng').value);

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
 * @param {object} time Time retrieved from servlet
 * @returns {String} time in String format.
 */
function parseTime(time){
  let timeString = '';

  if (time.hour < 10) timeString += '0' + time.hour + ':';
  else timeString += time.hour + ':';

  if (time.minute < 10) timeString += '0' + time.minute;
  else timeString += time.minute;

  return timeString;
}

