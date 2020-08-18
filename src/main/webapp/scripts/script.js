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
  const currentTime = { hour: today.getHours(), minute: today.getMinutes() };
  params.append('currentTime', parseTime(currentTime));

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
 * Function to prove check if business is still opened if it hasn't been updated
 * @param {Object} saleCard - Salecard object that contains its info
 * @return {boolean} 
 */
function isOpened(saleCard) {
  if (saleCard.isTemporarilyClosed) {
    return false;
  }

  const businessStartTime = saleCard.startTime;
  const businessEndTime = saleCard.endTime;

  const startTime = new Date();
  startTime.setHours(businessStartTime.hour, businessStartTime.minute, businessStartTime.second);

  const endTime = new Date();
  endTime.setHours(businessEndTime.hour, businessEndTime.minute, businessEndTime.second);

  const currentTime = new Date();
  currentTime.setHours(currentTime.getHours(), currentTime.getMinutes(), currentTime.getSeconds());

  /**
   * Returns true if the time is between the salecard's opening hours
   * E.g. if isOpenDuringNight, currentTime between (startTime, 23:59] or [00:00, endTime)
   * else, currentTime between (startTime, endTime)
   */
  return (saleCard.openDuringNight) 
    ? (startTime <= currentTime || currentTime <= endTime)
    : (startTime <= currentTime && currentTime <= endTime);
}

/**
 * Function that inserts vendor's info given an HTML container
 * @param {Element} container - HTML container
 * @param {Element} template - template HTML element.
 * @param {Object} vendor - Vendor object that contains its info.
 * @param {Boolean} isModal - Check if the container is a modal
 */
function insertVendorInfo(container, template, vendor, isModal) {
  let salecard = vendor.saleCard;
  let prefix;
  prefix = (isModal) ? 'modal' : 'card';

  if (!isModal) {
    if (!isOpened(salecard)) {
      const businessPictureContainer =
        template.getElementById('business-picture-container');
      const closedHeader = template.getElementById('closed-title');

      businessPictureContainer.classList.add('blur-picture');
      closedHeader.classList.remove('d-none');
   }
  }
  
  template.getElementById(`${prefix}-business-picture`).src
      = `/serve-blob?blobKey=${vendor.saleCard.picture.blobKey.blobKey}`;
  template.getElementById(`${prefix}-business-picture`).alt = vendor.saleCard.picture.altText;
  template.getElementById(`${prefix}-business-name`).textContent = salecard.businessName;
  template.getElementById(`${prefix}-business-description`).textContent = salecard.description;
  template.getElementById(`${prefix}-vendor-name`).textContent = `${vendor.firstName} ${vendor.lastName}`;
  template.getElementById(`${prefix}-vendor-phone`).textContent = vendor.phoneNumber;
  template.getElementById(`${prefix}-vendor-distance`).textContent = `${salecard.distanceFromClient.toFixed(2)}m`;
  template.getElementById(`${prefix}-vendor-salecard-btn`).setAttribute('href', `viewCard.html?id=${vendor.id}`);

  container.appendChild(template);
}
