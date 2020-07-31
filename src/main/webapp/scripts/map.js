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
$(function () {
  jQuery.get('../../../../API_KEY.txt', function (textString) {
    const API_KEY = textString;

    // Create the script tag, set the appropriate attributes.
    const scriptMapTag = document.createElement('script');
    scriptMapTag.src = `https://maps.googleapis.com/maps/api/js?key=${API_KEY}&callback=initMap`;
    scriptMapTag.defer = true;

    document.head.appendChild(scriptMapTag);
  });
});

/**
 * Callback function once the map is retrieved from Maps' API
 */
window.initMap = function () {
  const map = new google.maps.Map(
    document.getElementById('map'), { zoom: 15 });

  // Get client current location
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((position) => {
      const userPosition = {
        lat: position.coords.latitude,
        lng: position.coords.longitude
      };
      map.setCenter(userPosition);
      map.setOptions({styles: MAP_THEME});
      
      // The marker, positioned at client's location
      const marker = new google.maps.Marker({ position: userPosition, map: map });

      // Declare circle that match user's query parameters
      const userCircle = new google.maps.Circle({
        center: userPosition,
        fillColor: '#FF0000',
        fillOpacity: 0.20,
        map,
        radius: parseInt(document.getElementById('distance').value),
        strokeColor: '#FF0000',
        strokeOpacity: 0.8,
        strokeWeight: 2
      });
    },
    (error) => {
      if (error.code == error.PERMISSION_DENIED)
        alert('Permission denied to access location');
    });
  } else {
    alert('This browser does not support location');
  }
};