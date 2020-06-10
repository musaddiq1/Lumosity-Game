const express = require('express');
const router = express.Router();
const friendsController = require('../Controllers/friends');

router.post("/cancelRequest",(req,res)=>{
  var ID = req.body.ID;
  if( ID == undefined || !ID){
    return res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{
    friendsController.deleteFriendRequest(ID).then((x)=>{
      return res.send({
        "error" : false,
        "message" : "Request cancelled"
      });
    }).catch((err)=>{
      console.log(err);
      return res.send({
        "error" : true,
        "message" : "Try again in a moment"
      });
    });
  }
});

router.post("/removeFriend",(req,res)=>{
  var ID = req.body.ID;
  if( ID == undefined || !ID){
    return res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{
    friendsController.removeFromFriends(ID).then((x)=>{
      return res.send({
        "error" : false,
        "message" : "removeFriend success"
      });
    }).catch((err)=>{
      console.log(err);
      return res.send({
        "error" : true,
        "message" : "Try again in a moment"
      });
    });
  }
});

router.post("/acceptRequest",(req,res)=>{
  var ID = req.body.ID;
  if( ID == undefined || !ID){
    return res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{
    friendsController.acceptFriendRequest(ID).then((x)=>{
      return res.send({
        "error" : false,
        "message" : "Request accepted"
      });
    }).catch((err)=>{
      return res.send({
        "error" : true,
        "message" : "Try again in a moment"
      });
    });
  }
});

module.exports = router;
