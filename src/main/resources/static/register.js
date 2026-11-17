// ---------- Enter key works like the Register button ----------
document
  .getElementById("confirm-password")
  .addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
      register();
    }
  });

// ---------- Register button ----------
function register() {
  var username = document.getElementById("username").value.trim();
  var password = document.getElementById("password").value;
  var confirmPassword = document.getElementById("confirm-password").value;
  var message = document.getElementById("register-message");

  // Message starts red. It turns green only on success
  message.className = "error-text";

  // Check the inputs before sending
  if (username.length < 3 || username.length > 50) {
    message.innerText = "Username must be 3 to 50 characters";
    return;
  }
  if (password.length < 6 || password.length > 50) {
    message.innerText = "Password must be 6 to 50 characters";
    return;
  }
  if (password !== confirmPassword) {
    message.innerText = "Passwords do not match";
    return;
  }

  // Send the new account details to the server
  fetch("/api/auth/register", {
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
        message.className = "success-text";
        message.innerText = "Account created. Going to login...";
        // Wait 1.5 seconds, then open the login page
        setTimeout(function () {
          window.location.href = "index.html";
        }, 1500);
      } else {
        message.innerText = text;
      }
    })
    .catch(function () {
      message.innerText = "Server not reachable";
    });
}
