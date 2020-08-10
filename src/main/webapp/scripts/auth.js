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

// Call to Auth servlet to get user log information
/** @param {string} fileName*/
function getLogStatus(fileName) {
  fetch('/log-status').then(response => response.json()).then((logStatus) => {
    if (!logStatus.isRegistered && logStatus.isLogged) {
      redirectToRegistrationForm();
    }
    
    if (fileName === 'home') {
      handleTutorialContent(logStatus.isLogged);
    }

    setLogURLInNavBar(logStatus);
  });
}

function redirectToRegistrationForm() {
  window.location.replace('./registration.html');
}

// Sets URL from Auth API in the navbar log button
/** @param {{url:string, isLogged:boolean, isRegistered:boolean}} logStatus */
function setLogURLInNavBar(logStatus) {
  const logButton = document.getElementById('log_button');
  const logForm = document.getElementById('log_submit_form');

  logButton.innerText = (logStatus.isLogged) ? 'Log Out' : 'Log In';
  logButton.href = logStatus.url;
}

// Valids that the user inputs are in the right format
function validateRegistrationFormInputs() {
  const firstName = document.getElementById('first_name').value;
  const lastName = document.getElementById('last_name').value;
  const phoneNumber = document.getElementById('phone_number').value;

  if (!firstName || !lastName || !isValidInput(firstName, true) || !isValidInput(lastName, true)) {
    alert('Names can\'t be empty or have special characters');
    return;
  }

  if (!phoneNumber || phoneNumber.length !== PHONE_NUMBER_LENGTH || !isValidInput(phoneNumber, false)) {
    alert(`Phone Number can't be empty, have spaces or have more than ${PHONE_NUMBER_LENGTH} numbers`);
    return;
  }

  handleRegistration(firstName, lastName, phoneNumber);
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
* @return {void} 
*/
function handleRegistration(firstName, lastName, phoneNumber) {
  const vendorsParams = new URLSearchParams();
  vendorsParams.append('first_name', firstName);
  vendorsParams.append('last_name', lastName);
  vendorsParams.append('phone_number', phoneNumber);

  fetch('/new-vendor', {method: 'POST', body: vendorsParams})
  .then(response => {
    // If the registration is complete then redirect to home
    if(response.redirected) {
      window.location.replace('./home.html');
      return;
    }

    // If an error ocurred, alert it
    response.text().then((error) => {
      alert(error);
    });
  });
}
