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
      .then((response) => {
        if (response.redirected) {
          window.location.href = response.url;
          return;
        }
        // Response status 200 = Servlet response OK
        if (response.status === 200) {
          return response.json();
        } else {
          alert(response.statusText);
          return;
        }
      })
      .then(vendorEntity => {
        vendor = vendorEntity;
      });

  if (!vendor) {
    window.location.href = '/';
    return;
  }

  if (!('saleCard' in vendor)) {
    await updateLocation();
    document.getElementById('salecard-btns').appendChild(createButton('create'));
  } else {
    showVendorData(vendor);
    addSaleCardButtons(vendor.saleCard);
  }

  drawMap(getVendorInfo());
}

/**
 * Function that shows vendor information in the form
 * @param {Object} vendor - Vendor object to show information
 */
function showVendorData(vendor) {
  let salecard = vendor.saleCard;

  document.getElementById('business-picture').src
      = `/serve-blob?blobKey=${vendor.saleCard.picture.blobKey.blobKey}`;
  document.getElementById('business-picture').alt
      = `Business picture: ${vendor.saleCard.picture.altText}`;
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
  document.getElementById('isTemporarilyClosed').value = salecard.isTemporarilyClosed;
}

/**
 * Function that adds salecard's buttons to the container in the DOM.
 * Adds Open/Close, Update and Delete buttons.
 * @param {Object} salecard - A vendor's salecard
 */
function addSaleCardButtons(salecard) {
  const buttonsContainer = document.getElementById('salecard-btns');

  const openCloseButton = (salecard.isTemporarilyClosed) 
      ? createButton('open')
      : createButton('close');
  const updateButton = createButton('update');
  const deleteButton = createButton('delete');  

  buttonsContainer.append(openCloseButton, updateButton, deleteButton);
}

/**
 * Function that returns a button according to the type provided
 * @param {string} buttonType - The type of button to create
 */
function createButton(buttonType) {
  const template = document.getElementById('button-template');
  const button = template.content.querySelector('button').cloneNode(true);
  switch (buttonType.toLowerCase()) {
    case 'open':
      button.classList.add('btn-primary');
      button.setAttribute('data-toggle', 'modal');
      button.setAttribute('data-target', '#open-business-modal');
      button.textContent = 'Open my business now';
      button.addEventListener('click', displayBusinessHours);
      break;
    case 'close':
      button.classList.add('btn-danger');
      button.setAttribute('data-toggle', 'modal');
      button.setAttribute('data-target', '#close-business-modal');
      button.textContent = 'Close my business now';
      break;
    case 'create':
      button.setAttribute('type', 'submit');
      button.classList.add('btn-success');
      button.textContent = 'Create business';
      break;
    case 'update':
      button.setAttribute('type', 'submit');
      button.classList.add('btn-warning');
      button.textContent = 'Update business information';
      break;
    case 'delete':
      button.classList.add('btn-danger');
      button.setAttribute('data-toggle', 'modal');
      button.setAttribute('data-target', '#delete-business-modal');
      button.textContent = 'Delete my business';
      break;
    default:
      throw new Error('No button option');
  }

  return button;
}

/**
 * Function that display the business hours in the open biz modal
 */
function displayBusinessHours() {
  const start = document.getElementById('startTime').value;
  const end = document.getElementById('endTime').value;

  const modalBody = document.getElementById('business-hours-modal');
  modalBody.textContent = `From ${start} to ${end}.`;
}

/**
 * Function that updates the salecard to be open or temporarily close
 * @param {boolean} isClosed - Indicates if the business is closed or not
 */
function updateSaleCard(isClosed) {
  document.getElementById('isTemporarilyClosed').value = isClosed;
  const form = document.getElementById('form-card');
  const formData = new FormData(form);

  fetch(form.action, {method: 'POST', body: formData}).then(response => {
    if (!response.redirected) {
      alert(response.statusText);
    }
    location.reload();
  });
}

/**
 * Function that deletes the vendor's salecard
 */
function deleteSaleCard() {
  fetch('/delete-salecard', {method: 'POST'}).then(response => {
    if (!response.redirected) {
      alert(response.statusText);
    }
    location.reload();
  });
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
  fetch('/blobstore-upload-url?formHandler=/update-salecard')
      .then((response) => {
        return response.text();
      }).then((formUrl) => {
    document.getElementById('form-card').action = formUrl;
  });

  initMap();
};