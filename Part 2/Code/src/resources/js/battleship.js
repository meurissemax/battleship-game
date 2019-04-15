/**
 * This function is used to recover the content of a cookie.
 *
 * @param cname the cookie name
 * @return the content of the cookie 'cname'
 */
function getCookie(cname) {
	var name = cname + "=";
	var decodedCookie = decodeURIComponent(document.cookie);
	var ca = decodedCookie.split(';');

	for(var i = 0; i < ca.length; i++) {
		var c = ca[i];

		while(c.charAt(0) == ' ')
			c = c.substring(1);

		if (c.indexOf(name) == 0)
			return c.substring(name.length, c.length);
	}

	return "";
}

var playerID = getCookie(cookieName);

document.getElementById('jshow').style.display = "block";
document.getElementById('player').innerHTML = "Player ID : <b>" + playerID + "</b>";

/**
 * This function is used to send an AJAX request to the server.
 * The AJAX request contains the position hit by the player.
 * This function also read the response of the server and update the content of the HTML page.
 * If the player win or lose the game, he's redirected to an other page.
 *
 * @param pos the position hit by the player
 */
function hitPos(pos) {
	var xhr = new XMLHttpRequest();

	xhr.open('GET', pagePlay + '?pos=' + pos, true);
	xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

	xhr.addEventListener('readystatechange', function() {
		if(xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
			var result = JSON.parse(xhr.responseText);

			var posStatus = result['posStatus'];
			var numberTries = result['numberTries'];
			var remainingShips = result['remainingShips'];

			var posHTML = document.getElementById(pos);
			var triesHTML = document.getElementById('numberTries');
			var shipsHTML = document.getElementById('remainingShips');

			if(posStatus == 0)
				posHTML.innerHTML = "<td id='" + pos + "' onclick='hitPos(" + pos + ")'><img src='" + imgMiss + "' width='50' height='50'></td>";

			if(posStatus > 0)
				posHTML.innerHTML = "<td id='" + pos + "' onclick='hitPos(" + pos + ")'><img src='" + imgHit + "' width='50' height='50'></td>";

			triesHTML.innerHTML = "<td class='text-center' id='numberTries'><b>" + numberTries + "</b></td>";
			shipsHTML.innerHTML = "<td class='text-center' id='remainingShips'><b>" + remainingShips + "</b></td>";

			if(result['isWin'] == "true")
				setTimeout(function () {
					document.location = "/win.html";
				}, 750);

			if(result['isLose'] == "true")
				setTimeout(function () {
					document.location = "/lose.html";
				}, 750);
		}
	});

	xhr.send(null);
}
