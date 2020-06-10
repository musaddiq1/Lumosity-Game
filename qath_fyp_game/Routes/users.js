const express = require('express');
const router = express.Router();
const userController = require('../Controllers/users');
const gameInfoController = require('../Controllers/gameInfo');
const friendsController = require('../Controllers/friends');
const store = require("../Controllers/storeRoom");

router.post('/register',(req,res,next)=>{

  var username = req.body.username;
  var email = req.body.email;
  var provider_id = req.body.provider_id;
  var provider = req.body.provider;
  var profile = req.body.profile;
  var password = req.body.password;

  if(username == undefined || email == undefined || provider_id == undefined || provider == undefined || profile == undefined || password == undefined || !username.trim() || !email.trim() || !provider_id.trim() || !provider.trim() || !profile.trim() || !password.trim()){
    res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{

    if(provider != store.USER_PROVIDER_FACEBOOK &&  provider != store.USER_PROVIDER_GMAIL && provider != store.USER_PROVIDER_APP_AUTH){
      res.json({"error" : true,"message" : "Invalid Request"});
    }else{
      userController.createUserAccount(username,email,provider_id,provider,profile,password,(err,message,user,token)=>{
        if(err){
          return res.send({
            "error" : true,
            "message" : message
          });
        }else{
          return res.send({
            "error" : false,
            "message" : message,
            "user" : user,
            "token" : token
          });
        }
      });
    }
  }
}); // registration ends here

router.post('/login',(req,res,next)=>{

  var email = req.body.email;
  var password = req.body.password;

  if( email == undefined || !email || password == undefined || !password){
    return res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{

    userController.userSignIn(email,password,(err,message,user,token)=>{
      if(err){
        return res.send({
          "error" : true,
          "message" : message
        });
      }else{
        res.send({
          "error" : false,
          "message" : message,
          "user" : user,
          "token" : token
        });
      }
    });
  }
});// login router ends here

router.post("/stats",(req,res,next)=>{
  var userID = req.body.userID;
  if( userID == undefined || !userID){
    return res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{
    gameInfoController.userGameInfo(userID).then((data)=>{
      return res.send({"error" : false, "data" : data});
    }).catch((err)=>{
      return res.send({
        "error" : true,
        "message" : "something went wrong."
      });
    });
  }
});

router.post("/friends",(req,res,next)=>{
  var userID = req.body.userID;
  if( userID == undefined || !userID){
    return res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{
    friendsController.getAllFriendsByUserID(userID).then((data)=>{
      return res.send({"error" : false, "data" : data});
    }).catch((err)=>{
      return res.send({
        "error" : true,
        "message" : "something went wrong."
      });
    });
  }
});

router.post('/resendEmail',(req,res,next)=>{
  var email = req.body.email;
  if(!email){
    return res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{
    userController.resendVerificationEmail(email,(err,message)=>{
      if(err){
        return res.send({
          "error" : true,
          "message" : message
        });
      }else{
        return res.send({
          "error" : false,
          "message" : message
        });
      }
    });
  }
});// resendEmail router ends here

router.post('/verifyEmail',(req,res,next)=>{
  var email = req.body.email;
  var code = req.body.code;
  var from = req.body.from;

  if(!email || !code || !from){
    return res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{
    userController.verifyEmailAddress(email,code,from,(err,message)=>{
      if(err){
        return res.send({
          "error" : true,
          "message" : message
        });
      }else{
        return res.send({
          "error" : false,
          "message" : message
        });
      }
    });
  }
});//

router.post("/resetPassword",(req,res,next)=>{

  var password = req.body.password;
  var email = req.body.email;
  var code = req.body.code;

  if(!password || !email || !code){
    return res.send({"error" : true,"message":'Required Parameters are missing'});
  }else{

    userController.resetPassword(password,email,code,(err,message)=>{
      if(err){
        return res.send({"error" : true,"message" : message});
      }else{
        res.send({
          "error" : false,
          "message" : message
        });
      }
    });
  }
});

module.exports = router;
