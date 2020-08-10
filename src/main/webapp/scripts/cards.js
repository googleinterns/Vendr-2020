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
 * Function that retrive vendors given query parameters
 * TODO: Add fetch from servelt & retrieve picture from blobstore
 */
querySalecards = () => {
    const salecardTemplate = document.getElementById('salecard-template');
    const salecardsContainer = document.getElementById('salecards-container');

    // Clean previously retrieved cards.
    salecardsContainer.textContent = '';

    Object.keys(vendors).forEach(vendorNumber => {
        let vendor = vendors[vendorNumber];
        let salecard = vendor.saleCard;
        let salecardCloned = salecardTemplate.content.cloneNode(true);
        salecardCloned.getElementById('business-name').textContent = salecard.businessName;
        salecardCloned.getElementById('business-description').textContent = salecard.description;
        salecardCloned.getElementById('vendor-name').textContent = `${vendor.firstName} ${vendor.lastName}`;
        salecardCloned.getElementById('vendor-phone').textContent = vendor.phoneNumber;
        salecardCloned.getElementById('vendor-distance').textContent = `${salecard.distanceFromClient}m`;
        salecardCloned.getElementById('vendor-salecard-btn').setAttribute('href', `viewCard.html?id=${salecard.id}`);

        salecardsContainer.appendChild(salecardCloned);
    });

    // Display number of vendor found.
    displayNumberOfVendors(vendors);
};