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

// User's blue dot mark for the map.
const USER_MARKER = {
  url: 'https://mt.google.com/vt/icon/name=icons/spotlight/directions_decision_point_walking_large.png&scale=2'
};

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
    scriptMapTag.src = `https://maps.googleapis.com/maps/api/js?key=${API_KEY}&callback=querySalecard`;
    scriptMapTag.defer = true;

    document.head.appendChild(scriptMapTag);
  });
};

/**
 * Callback function once the map is retrieved from Maps' API
 * @param {Object} person - Vendor or Client to display on map.
 * @returns {Object} map - The map object for future map modification.
 */
function drawMap(person) {
  const map = new google.maps.Map(
      document.getElementById('card-map'), {zoom: 15});

  const personLocation = {
    lat: person.latitude,
    lng: person.longitude
  };

  map.setCenter(personLocation);
  map.setOptions({styles: MAP_THEME});

  // The marker, positioned at person's location.
  const marker = new google.maps.Marker({
    map: map,
    icon: '',
    position: personLocation,
    title: person.markerName,
  });

  // Display blue marker to differentiate markers on map.
  if (person.type === 'client') {
    marker.icon = USER_MARKER;
  }

  if (person.drawCircleRadius) {
    // Declare circle with radius of the delivery service of the person.
    const vendorCircle = new google.maps.Circle({
      center: personLocation,
      fillColor: '#F00',
      fillOpacity: 0.20,
      map,
      radius: person.radius,
      strokeColor: '#F00',
      strokeOpacity: 0.8,
      strokeWeight: 2
    });
  }

  return map;
}

const getCurrentPositionPromise = geolocation => new Promise((resolve, reject) => {
  geolocation.getCurrentPosition((position) => {
        resolve(position);
      },
      (error) => {
        reject(error)
      });
});

async function updateLocation() {
  // Get client current location.
  if (navigator.geolocation) {
    try {
      const position = await getCurrentPositionPromise(navigator.geolocation);
      document.getElementById('lat').value = position.coords.latitude;
      document.getElementById('lng').value = position.coords.longitude;
    } catch (error) {
      alert(error.message);
    }
  } else {
    alert('This browser does not support location');
  }
}