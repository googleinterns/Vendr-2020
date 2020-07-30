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

const MAP_THEME = [
  {
    "featureType": "poi.business",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text",
    "stylers": [
      {
        "visibility": "off"
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
    let API_KEY = textString;

    // Create the script tag, set the appropriate attributes.
    var script = document.createElement('script');
    script.src = `https://maps.googleapis.com/maps/api/js?key=${API_KEY}&callback=initMap`;
    script.defer = true;

    // Append the 'script' element to 'head'
    document.head.appendChild(script);
  });
});

/**
 * Callback function once the map is retrieved from Maps' API
 */
window.initMap = function () {
  var map = new google.maps.Map(
    document.getElementById('map'), { zoom: 15 });

  // Get client current location
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function (position) {
      var userPos = {
        lat: position.coords.latitude,
        lng: position.coords.longitude
      };
      
      map.setCenter(userPos);
      map.setOptions({styles: MAP_THEME});
      
      // The marker, positioned at client's location
      var marker = new google.maps.Marker({ position: userPos, map: map });

      // Declare circle that match user's query parameters
      const userCircle = new google.maps.Circle({
        strokeColor: "#FF0000",
        strokeOpacity: 0.8,
        strokeWeight: 2,
        fillColor: "#FF0000",
        fillOpacity: 0.20,
        map,
        center: userPos,
        radius: parseInt(document.getElementById('distance').value)
      });
    });
  } else {
    alert("This browser does not support location");
  }
};