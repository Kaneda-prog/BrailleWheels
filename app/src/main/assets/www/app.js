<!DOCTYPE html>
<html>
  <head>
    <title>Simple Map</title>
    <meta name="viewport" content="initial-scale=1.0">
    <meta charset="utf-8">
    <style>
      /* Always set the map height explicitly to define the size of the div
       * element that contains the map. */
      #map {
        height: 100%;
      }
      /* Optional: Makes the sample page fill the window. */
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;
      }
    </style>
  </head>
  <body>
      <div id="map"></div>
    <script type = "text/javascript">
    var loc = Android.getValue();
var map, infoWindow;
function showBanana(lo)
{
  Android.iLoveBanana(lo);
}
showBanana();
function initMap() {
        var directionsRenderer = new google.maps.DirectionsRenderer;
        var directionsService = new google.maps.DirectionsService;
        var haight = new google.maps.LatLng(37.7699298, -122.4469157);
          var oceanBeach = new google.maps.LatLng(37.7683909618184, -122.51089453697205);
        var map = new google.maps.Map(document.getElementById('map'), {
          zoom: 14,
          center: haight
        });
        directionsRenderer.setMap(map);

        calculateAndDisplayRoute(directionsService, directionsRenderer);
      }

      function calculateAndDisplayRoute(directionsService, directionsRenderer) {
        directionsService.route({
          origin: {lat: 37.77, lng: -122.447},  // Haight.
          destination: {lat: 37.768, lng: -122.511},  // Ocean Beach.
          // Note that Javascript allows us to access the constant
          // using square brackets and a string value as its
          // "property."
          travelMode: 'DRIVING',
        }, function(response, status) {
          if (status == 'OK') {
            directionsRenderer.setDirections(response);
          } else {
            window.alert('Directions request failed due to ' + status);
          }
        });
      }
    </script>
    <script async defer
    src="https://maps.googleapis.com/maps/api/js?key=AIzaSyA2n7hH6W6cHvZdRX2kBmL0b21ev6WWjag&callback=initMap">
    </script>
  </body>
</html>