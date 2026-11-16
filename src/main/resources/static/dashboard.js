// ---------- Login check ----------
// If the user is not logged in, go back to the login page
if (localStorage.getItem("loggedIn") !== "yes") {
  window.location.href = "index.html";
}

// This list holds all bills loaded from the server
var allBills = [];

// ---------- Date helpers ----------

// Returns today's date. Change the return line only for testing
function getToday() {
  return new Date();
}

// Makes text safe to place inside HTML
function escapeText(text) {
  var result = String(text);
  result = result.split("&").join("&amp;"); // & becomes &amp;
  result = result.split("<").join("&lt;"); // < becomes &lt;
  result = result.split(">").join("&gt;"); // > becomes &gt;
  return result;
}

// Returns this month as text, like 2026-11
function getThisMonthText() {
  var today = getToday();
  var year = today.getFullYear();
  var month = today.getMonth() + 1; // getMonth() starts at 0, so add 1
  var monthText = String(month);
  if (month < 10) {
    monthText = "0" + monthText; // 5 becomes 05
  }
  return year + "-" + monthText;
}

// Returns true if the bill is paid for this month
function isPaid(bill) {
  return bill.paidMonth === getThisMonthText();
}

// Returns days left until the due day. Negative means it has passed
function getDaysLeft(bill) {
  var today = getToday();
  var year = today.getFullYear();
  var month = today.getMonth() + 1;

  // Day 0 of the next month gives the last day of this month
  var lastDay = new Date(year, month, 0).getDate();

  // The 31st rule: in a shorter month, use the last day of the month
  var dueDay = bill.dueDay;
  if (dueDay > lastDay) {
    dueDay = lastDay;
  }

  return dueDay - today.getDate();
}

// Number used for sorting: smaller means more urgent
function getSortValue(bill) {
  var value = getDaysLeft(bill);
  if (isPaid(bill)) {
    value = value + 1000; // paid bills go to the bottom
  }
  return value;
}

// ---------- Load bills from the server ----------

function loadBills() {
  // fetch asks the Spring server for data
  fetch("/api/bills")
    .then(function (response) {
      return response.json(); // turn the reply into a list
    })
    .then(function (data) {
      allBills = data;
      showBills();
    })
    .catch(function () {
      alert("Could not load bills. Is the server running?");
    });
}

// ---------- Show bills in the table and cards ----------

function showBills() {
  var filter = document.getElementById("type-filter").value;
  var tableBody = document.getElementById("bills-body");
  var rows = "";
  var total = 0;
  var paid = 0;

  // Sort the most urgent bills to the top
  allBills.sort(function (a, b) {
    return getSortValue(a) - getSortValue(b);
  });

  for (var i = 0; i < allBills.length; i++) {
    var bill = allBills[i];

    // Cards count all bills, even when a filter is on
    total = total + bill.amount;
    if (isPaid(bill)) {
      paid = paid + bill.amount;
    }

    // Show this bill only if it matches the filter
    if (filter === "All" || bill.billType === filter) {
      var daysLeft = getDaysLeft(bill);
      var badgeClass = "";
      var badgeText = "";

      if (isPaid(bill)) {
        badgeClass = "badge-paid";
        badgeText = "Paid";
      } else if (daysLeft < 0) {
        badgeClass = "badge-overdue";
        badgeText = "Overdue by " + (0 - daysLeft) + " days";
      } else if (daysLeft <= 3) {
        badgeClass = "badge-3days";
        if (daysLeft === 0) {
          badgeText = "Due today";
        } else {
          badgeText = "Due in " + daysLeft + " days";
        }
      } else if (daysLeft <= 7) {
        badgeClass = "badge-7days";
        badgeText = "Due in " + daysLeft + " days";
      } else if (daysLeft <= 10) {
        badgeClass = "badge-10days";
        badgeText = "Due in " + daysLeft + " days";
      } else {
        badgeClass = "badge-upcoming";
        badgeText = "Due in " + daysLeft + " days";
      }

      // Text on the pay button
      var payButtonText = "Mark Paid";
      if (isPaid(bill)) {
        payButtonText = "Mark Unpaid";
      }

      // Build one table row as text
      rows = rows + "<tr>";
      rows = rows + "<td>" + escapeText(bill.title) + "</td>";
      rows = rows + "<td>" + escapeText(bill.billType) + "</td>";
      rows = rows + "<td>Rs " + bill.amount.toFixed(2) + "</td>";
      rows = rows + "<td>" + bill.dueDay + "</td>";
      rows =
        rows +
        "<td><span class='badge " +
        badgeClass +
        "'>" +
        badgeText +
        "</span></td>";
      rows = rows + "<td>";
      rows =
        rows +
        "<button class='btn-paid' onclick='togglePaid(" +
        bill.id +
        ")'>" +
        payButtonText +
        "</button>";
      rows =
        rows +
        "<button class='btn-edit' onclick='editBill(" +
        bill.id +
        ")'>Edit</button>";
      rows =
        rows +
        "<button class='btn-delete' onclick='deleteBill(" +
        bill.id +
        ")'>Delete</button>";
      rows = rows + "</td>";
      rows = rows + "</tr>";
    }
  }

  // Message when nothing matches
  if (rows === "") {
    rows = "<tr><td colspan='6'>No bills found</td></tr>";
  }

  // Put the rows into the table
  tableBody.innerHTML = rows;

  // Fill the summary cards
  var pending = total - paid;
  document.getElementById("total-amount").innerText = "Rs " + total.toFixed(2);
  document.getElementById("paid-amount").innerText = "Rs " + paid.toFixed(2);
  document.getElementById("pending-amount").innerText =
    "Rs " + pending.toFixed(2);
}

// ---------- Button actions ----------

// Switch a bill between Paid and Unpaid
function togglePaid(id) {
  fetch("/api/bills/" + id + "/toggle-paid", { method: "POST" }).then(
    function () {
      loadBills(); // reload the table
    },
  );
}

// Open the form page in edit mode
function editBill(id) {
  window.location.href = "bill-form.html?id=" + id;
}

// Delete a bill after asking
function deleteBill(id) {
  var sure = confirm("Delete this bill?");
  if (sure === true) {
    fetch("/api/bills/" + id, { method: "DELETE" }).then(function () {
      loadBills();
    });
  }
}

// Log out and return to the login page
function logout() {
  localStorage.removeItem("loggedIn");
  window.location.href = "index.html";
}

// ---------- Start ----------
loadBills();
