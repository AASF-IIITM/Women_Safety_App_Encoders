const functions = require('firebase-functions');
// import * as admin from 'firebase-admin'
const admin = require('firebase-admin')
admin.initializeApp(functions.config().firebase)
// import { object } from 'firebase-functions/lib/providers/storage';
// Create and Deploy Your First Cloud Functions
// https://firebase.google.com/docs/functions/write-firebase-functions

// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.sent_status = functions.https.onCall((data, context) => {
    const source_no = data.source_no;
    const target_no = data.target_no;
    const selection = data.selection;
    const source_name = data.source_name;
    const target_name = data.target_name;

    if(selection === "accept") {
        admin.database().ref(`/invitation/${target_no}/`).once("value")
        .then(snapshot => {
            const findSource = snapshot.val()// return all the timestamp along with the contianed object
            var keys = Object.keys(findSource)
            const promises =[]
            for (var i=0 ;i < keys.length ; i++) {
                var k =keys[i];
                if (findSource[k].number === source_no) {
                    const p=admin.database().ref(`/invitation/${target_no}/${k}`).remove();
                    promises.push(p)
                    const q= admin.database().ref(`/Users/${target_no}/matchedall/`).push({
                        name : source_name,
                        number : source_no,
                    })
                    promises.push(q)
                    // const r= admin.database().ref(`/Users/${source_no}/sent`).once("value")
                    // promises.push(r)
                    break;
                }
            }
            return Promise.all(promises)
        })
        .catch(error => {
            console.log(error)
            response.status(500).send(error)
        })
        admin.database().ref(`/Users/${source_no}/sent`).once("value")
        .then (snapshot => {
            const findTarget = snapshot.val()
            var keys = Object.keys(findTarget)
            const promises = [];
            for (var i=0 ;i < keys.length ; i++) {
                var k =keys[i];
                if (findTarget[k].number === target_no) {
                    const p=admin.database().ref(`/Users/${source_no}/sent/${k}`).update({
                        "status" : "accepted",
                    });
                    promises.push(p)
                    const q= admin.database().ref(`/Users/${source_no}/matched/`).push({
                        name : target_name,
                        number : target_no,
                    })
                    promises.push(q)
                    const r = admin.database().ref(`/Users/${source_no}/matchedall/`).push({
                        name : target_name,
                        number : target_no,
                    })
                    promises.push(r)
                    break;
                }
            }
            return Promise.all(promises)
        })
        .catch((err) => {
            console.error("error in adding document",err);
        })
    }

    else {
        admin.database().ref(`/invitation/${target_no}/`).once("value")
        .then(snapshot => {
            const findSource = snapshot.val() // return all the timestamp along with the contianed object
            var keys = Object.keys(findSource)
            const promises =[]
            for (var i=0 ;i < keys.length ; i++) {
                var k =keys[i];
                if (findSource[k].number === source_no) {
                    const p=admin.database().ref(`/invitation/${target_no}/${k}/`).remove();
                    promises.push(p)
                    break;
                }
            }
            return Promise.all(promises)
        })
        .catch(error => {
            console.log(error)
            response.status(500).send(error)
        })
        admin.database().ref(`/Users/${source_no}/sent`).once("value")
        .then (source_snap => {
            const findTarget = source_snap.val()
            var keys = Object.keys(findTarget)
            const promises = [];
            for (var i=0 ;i < keys.length ; i++) {
                var k =keys[i];
                if (findTarget[k].number === target_no) {
                    const p=admin.database().ref(`/Users/${source_no}/sent/${k}`).update({
                        "status" : "rejected"
                    });
                    promises.push(p)
                    break;
                }
            }
           return Promise.all(promises)
        })
        .catch(error => {
            console.log(error)
            response.status(500).send(error)
        })
    }
})