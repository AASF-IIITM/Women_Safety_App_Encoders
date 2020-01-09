const functions = require('firebase-functions');
import * as admin from 'firebase-admin'
import { object } from 'firebase-functions/lib/providers/storage';
// Create and Deploy Your First Cloud Functions
// https://firebase.google.com/docs/functions/write-firebase-functions

// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });


// exports.sent_status = functions.database
// .ref('/users/{target_no}/received/{receivedId}')
// .onCreate ((snapshot , context) => {
//     const receivedId = context.params.receivedId
//     const target_no = context.params.target_no
//     console.log(`phone no == ${target_no} and id == ${receivedId}`)
// })


exports.sent_status = functions.https.onCall((data, context) => {
    const source_no = data.source_no;
    const target_no = data.target_no;
    const selection = data.selection;
    const source_name = data.source_name;
    const target_name = data.target_name;

    if(selection == "accept") {
        admin.database().ref(`/users/${target_no}/received/`).once("value")
        .then(snapshot => {
            const findSource = snapshot.val() // return all the timestamp along with the contianed object
            var keys = Object.keys(findSource)
            const promises =[]
            for (var i=0 ;i < keys.length ; i++) {
                var k =keys[i];
                if (findSource[k].number == source_no) {
                    const p=admin.database().ref(`/users/${target_no}/received/${k}`).remove();
                    promises.push(p)
                    const q= admin.database().ref(`/users/${target_no}/matched/${k}`).push({
                        name : source_name,
                        number : source_no,
                    })
                    promises.push(q)
                    const r= admin.database().ref(`/users/${source_no}/sent`).once("value")
                    promises.push(r)
                    break;
                }
            }
            return Promise.all(promises)
        })
        .then (source_snap => {
            const findTarget = source_snap.val()
            var keys = Object.keys(source_snap)
            const promises = [];
            for (var i=0 ;i < keys.length ; i++) {
                var k =keys[i];
                if (findTarget[k].number == target_no) {
                    const p=admin.database().ref(`/users/${source_no}/sent/${k}`).remove();
                    promises.push(p)
                    const q= admin.database().ref(`/users/${source_no}/matched/${k}`).push({
                        name : source_name,
                        number : source_no,
                    })
                    promises.push(q)
                    break;
                }
            }
            Promise.all(promises)
        }) 
    }

    else {
        admin.database().ref(`/users/${target_no}/received/`).once("value")
        .then(snapshot => {
            const findSource = snapshot.val() // return all the timestamp along with the contianed object
            var keys = Object.keys(findSource)
            const promises =[]
            for (var i=0 ;i < keys.length ; i++) {
                var k =keys[i];
                if (findSource[k].number == source_no) {
                    const p=admin.database().ref(`/users/${target_no}/received/${k}`).remove();
                    promises.push(p)
                    const r= admin.database().ref(`/users/${source_no}/sent`).once("value")
                    promises.push(r)
                    break;
                }
            }
            return Promise.all(promises)
        })
        .then (source_snap => {
            const findTarget = source_snap.val()
            var keys = Object.keys(source_snap)
            const promises = [];
            for (var i=0 ;i < keys.length ; i++) {
                var k =keys[i];
                if (findTarget[k].number == target_no) {
                    const p=admin.database().ref(`/users/${source_no}/sent/${k}`).remove();
                    promises.push(p)
                    break;
                }
            }
            Promise.all(promises)
        })
    }
})