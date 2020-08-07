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
 */
const displayNumberOfVendors = (vendors) => {
  const numberOfVendorsElement = document.createElement('h3');
  numberOfVendorsElement.textContent = (vendors.length === 1) 
    ? '1 vendor found.'
    : `${vendors.length} vendors found.`;
  const containerElement = document.getElementById('numberOfVendors');
  containerElement.textContent = '';
  containerElement.appendChild(numberOfVendorsElement);
};