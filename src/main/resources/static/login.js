// Runs when the Login button is clicked
function login() {
  var username = document.getElementById("username").value;
  var password = document.getElementById("password").value;

  // Demo login: one fixed user, no real security
  if (username === "admin" && password === "1234") {
    // Remember the login inside the browser
    localStorage.setItem("loggedIn", "yes");
    // Go to the dashboard page
    window.location.href = "dashboard.html";
  } else {
    document.getElementById("login-message").innerText =
      "Wrong username or password";
  }
}
