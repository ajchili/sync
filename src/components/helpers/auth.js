import firebase from '../config/firebase';

let firebaseAuth = firebase.auth();

export function auth(email, pw) {
  return firebaseAuth().createUserWithEmailAndPassword(email, pw);
}

export function logout() {
  return firebaseAuth().signOut();
}

export function login(email, pw) {
  return firebaseAuth().signInWithEmailAndPassword(email, pw);
}

export function resetPassword(email) {
  return firebaseAuth().sendPasswordResetEmail(email);
}

export function user() {
  return firebaseAuth().currentUser;
}

export function onAuth(callback) {
  return firebaseAuth().onAuthStateChanged(callback);
}
