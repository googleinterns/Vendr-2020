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
 * Function to load the navbar on the web app 
 */

$(() => {
  // After the navbar finishes loading, handle user's login status
  $('#navbar_container').load('common/navbar.html', handleLogIn); 
});

const handleLogIn = () => {
  const fileName = getActiveTab();

  getLogStatus(fileName);
};

function getActiveTab() {
  const windowPath = window.location.pathname;
  const loadedFile = windowPath.split('/').pop();
  const fileName = loadedFile.split('.');

  return fileName[0];
}

function setActiveTab(fileName) {
  const activeTabElement = document.getElementById(`${fileName}_tab`);
  /** 
  * The only view thaht is not included on the navbar is ViewCard
  * which opens when a more info button on a SaleCard is clicked
  * if the tab is not found then don't activate in the navbar
  */
  if (!activeTabElement) {
    return;
  }
  activeTabElement.classList.add('active');
}

function insertEditCardTab() {
  const navBarList = document.getElementById('navbar-list');
  const tabElement = document.createElement('li');
  const navLink = document.createElement('a');

  tabElement.classList.add('nav-item');
  tabElement.setAttribute('id','editCard_tab');

  navLink.classList.add('nav-link');
  navLink.setAttribute('href', './editCard.html');
  navLink.innerHTML = 'My Business';

  tabElement.appendChild(navLink);
  navBarList.appendChild(tabElement);
}
