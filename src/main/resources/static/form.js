// Holds the bill id when editing. Stays empty when adding
var editId = "";

// Keeps the old paid status when editing
var existingPaidMonth = null;

// In edit mode, becomes true after the old bill has loaded
var billLoaded = false;

// Becomes true while the bill is being saved
var isSaving = false;

// ---------- Login check ----------
// Ask the server who is logged in. Go to the login page if nobody is
fetch("/api/auth/me")
  .then(function (response) {
    if (!response.ok) {
      throw new Error("Server error");
    }
    return response.text();
  })
  .then(function (name) {
    if (name === "") {
      window.location.href = "index.html";
    }
  })
  .catch(function () {
    document.getElementById("form-message").innerText = "Server not reachable";
  });

// ---------- Check the page address for an id ----------
// The address looks like bill-form.html?id=3 when editing
// Only digits are accepted. Blocks "?id=../x" and "?foo_id=3"
var rawId = new URLSearchParams(window.location.search).get("id");
if (rawId !== null) {
  if (/^[0-9]+$/.test(rawId)) {
    editId = rawId;
  } else {
    document.getElementById("form-heading").innerText = "Edit Bill";
    document.getElementById("form-message").innerText = "Invalid bill id";
    document.getElementById("save-button").disabled = true;
  }
}

// ---------- If editing, load the bill into the form ----------
if (editId !== "") {
  document.getElementById("form-heading").innerText = "Edit Bill";

  fetch("/api/bills/" + editId)
    .then(function (response) {
      // 401 means the login has expired
      if (response.status === 401) {
        window.location.href = "index.html";
        return null;
      }
      if (!response.ok) {
        throw new Error("Bill not found");
      }
      return response.json();
    })
    .then(function (bill) {
      if (bill === null) {
        return; // we are leaving this page
      }
      document.getElementById("title").value = bill.title;
      document.getElementById("bill-type").value = bill.billType;
      document.getElementById("amount").value = bill.amount;
      document.getElementById("due-day").value = bill.dueDay;
      // Notes can be empty (null), so check first
      if (bill.notes) {
        document.getElementById("notes").value = bill.notes;
      }
      // Save this so editing does not erase the paid status
      existingPaidMonth = bill.paidMonth;
      billLoaded = true;
    })
    .catch(function () {
      document.getElementById("form-message").innerText =
        "Could not load this bill";
    });
}

// ---------- Save button ----------
function saveBill() {
  // Ignore extra clicks while saving
  if (isSaving) {
    return;
  }

  var title = document.getElementById("title").value.trim();
  var billType = document.getElementById("bill-type").value;
  var amount = parseFloat(document.getElementById("amount").value); // text to number
  var dueDay = parseInt(document.getElementById("due-day").value, 10); // text to whole number
  var notes = document.getElementById("notes").value.trim();
  var message = document.getElementById("form-message");

  // In edit mode, wait until the old bill has loaded
  if (editId !== "" && billLoaded === false) {
    message.innerText = "Bill is still loading. Please wait";
    return;
  }

  // Check the inputs before sending
  if (title === "") {
    message.innerText = "Please enter a title";
    return;
  }
  if (isNaN(amount) || amount <= 0) {
    message.innerText = "Amount must be more than 0";
    return;
  }
  amount = Math.round(amount * 100) / 100; // 2 decimals, like the database
  if (amount <= 0 || amount > 99999999) {
    message.innerText = "Amount must be between 0.01 and 99999999";
    return;
  }
  if (isNaN(dueDay) || dueDay < 1 || dueDay > 31) {
    message.innerText = "Due day must be between 1 and 31";
    return;
  }

  // Pack the values into one bill object
  var bill = {
    title: title,
    billType: billType,
    amount: amount,
    dueDay: dueDay,
    paidMonth: existingPaidMonth,
    notes: notes,
  };

  // New bill uses POST. Existing bill uses PUT
  var url = "/api/bills";
  var method = "POST";
  if (editId !== "") {
    url = "/api/bills/" + editId;
    method = "PUT";
  }

  message.innerText = "";
  isSaving = true;

  fetch(url, {
    method: method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(bill), // turn the object into JSON text
  })
    .then(function (response) {
      if (response.ok) {
        window.location.href = "dashboard.html";
      } else if (response.status === 401) {
        window.location.href = "index.html"; // login expired
      } else if (response.status === 400) {
        message.innerText = "Please check the values and try again";
        isSaving = false;
      } else {
        message.innerText = "Could not save the bill";
        isSaving = false;
      }
    })
    .catch(function () {
      message.innerText = "Server not reachable";
      isSaving = false;
    });
}

// ---------- Enter key saves, like the Save button ----------
var inputs = document.querySelectorAll(".form-box input");
for (var i = 0; i < inputs.length; i++) {
  inputs[i].addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
      saveBill();
    }
  });
}
