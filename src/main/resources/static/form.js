// ---------- Login check ----------
if (localStorage.getItem("loggedIn") !== "yes") {
  window.location.href = "index.html";
}

// Holds the bill id when editing. Stays empty when adding
var editId = "";

// Keeps the old paid status when editing
var existingPaidMonth = null;

// ---------- Check the page address for an id ----------
// The address looks like bill-form.html?id=3 when editing
var query = window.location.search; // gives "?id=3"
if (query.indexOf("id=") !== -1) {
  editId = query.split("id=")[1]; // keeps only the "3"
}

// ---------- If editing, load the bill into the form ----------
if (editId !== "") {
  document.getElementById("form-heading").innerText = "Edit Bill";

  fetch("/api/bills/" + editId)
    .then(function (response) {
      return response.json();
    })
    .then(function (bill) {
      document.getElementById("title").value = bill.title;
      document.getElementById("bill-type").value = bill.billType;
      document.getElementById("amount").value = bill.amount;
      document.getElementById("due-day").value = bill.dueDay;
      document.getElementById("notes").value = bill.notes;
      // Save this so editing does not erase the paid status
      existingPaidMonth = bill.paidMonth;
    })
    .catch(function () {
      document.getElementById("form-message").innerText =
        "Could not load this bill";
    });
}

// ---------- Save button ----------
function saveBill() {
  var title = document.getElementById("title").value;
  var billType = document.getElementById("bill-type").value;
  var amount = parseFloat(document.getElementById("amount").value); // text to number
  var dueDay = parseInt(document.getElementById("due-day").value); // text to whole number
  var notes = document.getElementById("notes").value;
  var message = document.getElementById("form-message");

  // Check the inputs before sending
  if (title === "") {
    message.innerText = "Please enter a title";
    return;
  }
  if (isNaN(amount) || amount <= 0) {
    message.innerText = "Amount must be more than 0";
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

  fetch(url, {
    method: method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(bill), // turn the object into JSON text
  })
    .then(function (response) {
      if (response.ok) {
        window.location.href = "dashboard.html";
      } else {
        message.innerText = "Could not save the bill";
      }
    })
    .catch(function () {
      message.innerText = "Server not reachable";
    });
}
