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


// Map theme (removes nearby business).
const MAP_THEME = [
    {
      featureType: 'poi.business',
      stylers: [
        {
          visibility: 'off'
        }
      ]
    },
    {
      featureType: 'poi.park',
      elementType: 'labels.text',
      stylers: [
        {
          visibility: 'off'
        }
      ]
    }
  ];
  
  /**
   * Map declaration and initial setup
   * At the beginning, retrieves the API_KEY from an external file
   */
  const initMap = () => {
    jQuery.get('../API_KEY.txt', function (textString) {
      const API_KEY = textString;
  
      // Create the script tag, set the appropriate attributes.
      const scriptMapTag = document.createElement('script');
      scriptMapTag.src = `https://maps.googleapis.com/maps/api/js?key=${API_KEY}&callback=drawMap`;
      scriptMapTag.defer = true;
  
      document.head.appendChild(scriptMapTag);
    });
  };
  
  /**
   * Callback function once the map is retrieved from Maps' API
   */
  function drawMap() => {
    const map = new google.maps.Map(
      document.getElementById('card-map'), { zoom: 15 });
  
    // Insert vendor location
    const vendorLocation = {
      lat: parseFloat(document.getElementById('business-lat').value),
      lng: parseFloat(document.getElementById('business-lng').value)
    };
  
    map.setCenter(vendorLocation);
    map.setOptions({ styles: MAP_THEME });
  
    // The marker, positioned at vendor's location
    const marker = new google.maps.Marker({
      map: map,
      position: vendorLocation,
      title: vendor.saleCard.businessName,
    });
  
    // Declare circle with radius of the delivery service of the vendor
    const vendorCircle = new google.maps.Circle({
      center: vendorLocation,
      fillColor: '#F00',
      fillOpacity: 0.20,
      map,
      radius: vendor.saleCard.hasDelivery
        ? vendor.saleCard.location.radius : 0,
      strokeColor: '#F00',
      strokeOpacity: 0.8,
      strokeWeight: 2
    });
  }