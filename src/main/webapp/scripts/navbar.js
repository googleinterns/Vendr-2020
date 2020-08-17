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
  // After the navbar finishes loading, set the active tab
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
  activeTabElement.classList.add('active');
}

function insertEditCardTab() {
  const navBarList = document.getElementById('navbar-list');
  const LiTag = document.createElement('li');
  const aTag = document.createElement('a');

  LiTag.classList.add('nav-item');
  LiTag.setAttribute('id','editCard_tab');

  aTag.classList.add('nav-link');
  aTag.setAttribute('href', './editCard.html');
  aTag.innerHTML = 'My Business';

  LiTag.appendChild(aTag);
  navBarList.appendChild(LiTag);
}
