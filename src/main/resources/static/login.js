// ---------- Skip this page if already logged in ----------
// Ask the server who is logged in
fetch("/api/auth/me")
  .then(function (response) {
    if (!response.ok) {
      throw new Error("Server error");
    }
    return response.text();
  })
  .then(function (name) {
    if (name !== "") {
      window.location.href = "dashboard.html";
    }
  })
  .catch(function () {
    // Server not reachable. Stay on the login page
  });

// ---------- Enter key works like the Login button ----------
document
  .getElementById("password")
  .addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
      login();
    }
  });

// ---------- Login button ----------
function login() {
  var username = document.getElementById("username").value.trim();
  var password = document.getElementById("password").value;
  var message = document.getElementById("login-message");

  // Check the inputs before sending
  if (username === "" || password === "") {
    message.innerText = "Please enter username and password";
    return;
  }

  // Send the login details to the server
  fetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: username, password: password }),
  })
    .then(function (response) {
      if (!response.ok) {
        throw new Error("Server error");
      }
      return response.text(); // the server replies "OK" or an error text
    })
    .then(function (text) {
      if (text === "OK") {
        window.location.href = "dashboard.html";
      } else {
        message.innerText = text;
      }
    })
    .catch(function () {
      message.innerText = "Server not reachable";
    });
}
