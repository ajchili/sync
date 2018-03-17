const ref = firebase.database().ref();


ref.child('rooms').once('value').then(function (snapshot) {
  snapshot.forEach(function (childSnapshot) {
    console.log(childSnapshot.key)
  })
})

function setup() {
  $('.feedback').on('click', function() {
    $('.feedbackModal').modal('show');
  });
}

function submitFeedback() {
  var feedback = document.getElementById('feedbackModalInput');

  if (feedback.length > 0) {

  } else {
    
  }
}

setup();
