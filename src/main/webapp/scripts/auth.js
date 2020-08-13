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
* File to handle Google's Auth API requests
*/

const PHONE_NUMBER_LENGTH = 10;
const TUTORIAL_FILENAME = 'home';

// Call to Auth servlet to get user log information
/** @param {string} fileName */
function getLogStatus(fileName) {
  fetch('/log-status').then(response => response.json()).then((logStatus) => {
    if (!logStatus.isRegistered && logStatus.isLogged) {
      showRegistrationForm(logStatus.isRegistered);
    }
    
    if (fileName === TUTORIAL_FILENAME) {
      handleTutorialContent(logStatus.isLogged);
    }

    handleLogForm(logStatus);
  });
}

/** @param {boolean} isRegistered */
function showRegistrationForm(isRegistered) {
  $('#registration-modal').load('common/registration.html', () => {
    setUploadedImaged();
    if (isRegistered) {
      getUserInformation();
    }
    $('#exampleModalCenter').modal('show');
  });
}

function getUserInformation() {
  fetch('/get-vendor', {method: 'POST'}).then(
    response => {
      if (response.status === 401) {
        response.text().then((error) => {
          $('#log-button').trigger('click');
          alert(error);
          return;
        });
      } else if (response.status === 200) {
        response.json()
        .then((vendorInformation) => {
          setVendorInformationInModal(vendorInformation);
        });
      }
    }
  );
}

function setUploadedImaged() {
  $("#imageFile").change(function () {
    if (this.files && this.files[0]) {
      const reader = new FileReader();
      reader.onload = function (e) {
        $('#profile-picture').attr('src', e.target.result);
      }
      reader.readAsDataURL(this.files[0]);
    } else {
      $('#profile-picture').attr('src', 'images/placeholderImage.png');
    }
  });
}

function setVendorInformationInModal(vendorInformation) {
  console.log(vendorInformation);
  const firstName = document.getElementById('first_name');
  const lastName = document.getElementById('last_name');
  const phoneNumber = document.getElementById('phone_number');
  const blobKey = document.getElementById('blobKey');
  const registerButton = document.getElementById('registerButton');

  firstName.value = vendorInformation.firstName;
  lastName.value = vendorInformation.lastName;
  phoneNumber.value = vendorInformation.phoneNumber;
  registerButton.innerHTML = 'Update Account'

  if (vendorInformation.profilePic){
    const profilePic = document.getElementById('profile-picture');
    const altText = document.getElementById('altText');

    blobKey.value = vendorInformation.profilePic.blobKey.blobKey;
    profilePic.src = `/serve-blob?blobKey=${blobKey.value}`;
    altText.value = vendorInformation.profilePic.altText;
  }
}

// Sets the dropdown menu or URL link
/** @param {{url:string, isLogged:boolean, isRegistered:boolean}} logStatus */
function handleLogForm(logStatus) {
  if (logStatus.isLogged) {
    setDropdownMenuInDOM(logStatus.url);
  } else {
    setLogURL(logStatus.url);
  }
}

// Sets the log URL link on the DOM
/** @param {string} logURL */
function setLogURL(logURL) {
  const logButton = document.getElementById('log-button');
  logButton.href = logURL;
}

// Valids that the user inputs are in the right format
function validateRegistrationFormInputs() {
  const firstName = document.getElementById('first_name').value;
  const lastName = document.getElementById('last_name').value;
  const phoneNumber = document.getElementById('phone_number').value;
  const profilePictureFile = document.getElementById('imageFile');
  const blobKey = document.getElementById('blobKey').value;
  const altText = document.getElementById('altText').value;

  if (!firstName || !lastName || !isValidInput(firstName, true) || !isValidInput(lastName, true)) {
    alert('Names can\'t be empty or have special characters');
    return;
  }

  if (!phoneNumber || phoneNumber.length !== PHONE_NUMBER_LENGTH || !isValidInput(phoneNumber, false)) {
    alert(`Phone Number can't be empty, have spaces or have more than ${PHONE_NUMBER_LENGTH} numbers`);
    return;
  }

  if (!altText) {
    alert('Alt text can\'t be empty');
    return;
  }

  handleRegistration(firstName, lastName, phoneNumber,profilePictureFile.files[0], blobKey, altText);
}

// Checks if input is valid
/** 
* @param {string} vendorInput
* @param {boolean} isNameInput
* @return {boolean}
*/
function isValidInput(vendorInput, isNameInput) {
  // Regex for Valid Characters i.e. Alphabets, Numbers and Space
  const validCharacters = /^[A-Za-z0-9 ]+$/;
  // Regex for Valid Numbers
  const validNumbers = /^[0-9]+$/;

  const regexCheck = (isNameInput) ? validCharacters : validNumbers;

  // Returns true if the input is clean of special characters
  return regexCheck.test(vendorInput);
}

// Fetch vendor data to add it to datastore
/** 
* @param {string} firstName
* @param {string} lastName
* @param {string} phoneNumber
* @param {file} profilePictureImg
* @return {void} 
*/
async function handleRegistration(firstName, lastName, phoneNumber, profilePictureImg, blobKey, altText) {
  const blobURL = await getBlobstoreURL();
  const vendorsParams = new FormData();

  vendorsParams.append('firstName', firstName);
  vendorsParams.append('lastName', lastName);
  vendorsParams.append('phoneNumber', phoneNumber);
  vendorsParams.append('imageFile', profilePictureImg);
  vendorsParams.append('blobKey', blobKey);
  vendorsParams.append('altText', altText);

  await fetch(blobURL, {method: 'POST', body: vendorsParams})
  .then(response => {
    // If the registration is complete then hide the modal
    if(response.redirected) {
      alert('Successful Registration');
      $('#exampleModalCenter').modal('hide');
      return;
    }

    // If an error ocurred, alert it
    response.text().then((error) => {
      alert(error);
    });
  });
}

async function getBlobstoreURL() {
  let blobURL;
  await fetch('/blobstore-upload-url?formHandler=/update-vendor')
    .then((response) => {
      return response.text();
    }).then((formUrl) => {
      blobURL = formUrl;
    });
  return blobURL;
}
