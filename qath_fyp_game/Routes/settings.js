const express = require('express');
const router = express.Router();
const settingsController = require('../Controllers/settings');
const gameInfoController = require('../Controllers/gameInfo');

router.post("/updateName",(req,res)=>{

  var username = req.body.name;

  if(username == undefined || !username ){
    res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{
    settingsController.updateUserName(username,req.body.user).then((data)=>{
      res.send(data);
    }).catch((err)=>{
      res.send(err);
    });
  }

});

router.post("/updatePassword",(req,res)=>{

  var password = req.body.password;

  if(password == undefined || !password ){
    res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else{
    settingsController.updateUserPassword(password,req.body.user).then((data)=>{
      res.send(data);
    }).catch((err)=>{
      res.send(err);
    });
  }

});

router.post("/makePurchase",(req,res)=>{
  var userID = req.body.user.userID;
  var type = req.body.type;

  if(userID === undefined || !userID || type === undefined || !type ){
    res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else if(type != "GOLD_BALL" && type != "DIAMOND_BALL"){
    res.send({
      "error" : true,
      "message" : "Invalid Request"
    });
  }else{
    // verify user has enough coins
    gameInfoController.userGameInfo(userID).then((data)=>{
      if(data.userID != -99){
        var coins = data.coins;
        if(type === "GOLD_BALL"){
          if(coins >= 30000){
            // good to go
            gameInfoController.purchaseTheGoldBalls(userID).then((d)=>{
              return res.send({
                "error" : false,
                "message" : "You have successfully purchased gold balls."
              });
            }).catch((e)=>{
              return res.send({
                "error" : true,
                "message" : "Please try again in a moment."
              });
            });
          }else{
            return res.send({
              "error" : true,
              "message" : "Oho.\nYou do not have enough coins to make this purchase."
            });
          }
        }else{
          if(coins >= 80000){
            // good to go
            gameInfoController.purchaseTheDiamondBalls(userID).then((d)=>{
              return res.send({
                "error" : false,
                "message" : "You have successfully purchased diamond balls."
              });
            }).catch((e)=>{
              return res.send({
                "error" : true,
                "message" : "Please try again in a moment."
              });
            });
          }else{
            return res.send({
              "error" : true,
              "message" : "Oho.\nYou do not have enough coins to make this purchase."
            });
          }
        }
      }else{
        return res.send({
          "error" : true,
          "message" : "Something is wrong with your account info.\nPlease contact the support team."
        });
      }
    }).catch((err)=>{
      return res.send({
        "error" : true,
        "message" : "Please try again in a moment."
      });
    });
  }
});

router.post("/changeBall",(req,res)=>{
  var userID = req.body.user.userID;
  var type = req.body.type;

  if(userID === undefined || !userID || type === undefined || !type ){
    res.send({
      "error" : true,
      "message" : "Required Parameters are missing"
    });
  }else if(type != "GOLD_BALL" && type != "DIAMOND_BALL" && type != "SILVER_BALL"){
    res.send({
      "error" : true,
      "message" : "Invalid Request"
    });
  }else{
    if(type === "GOLD_BALL"){
      gameInfoController.changeCurrentBallToGold(userID).then((d)=>{
        return res.send(d);
      }).catch((e)=>{
          console.log(e);
        return res.send({
          "error" : true,
          "message" : "Please try again in a moment."
        });
      });
    }else if(type === 'DIAMOND_BALL'){
      gameInfoController.changeCurrentBallToDiamond(userID).then((d)=>{
        return res.send(d);
      }).catch((e)=>{

        return res.send({
          "error" : true,
          "message" : "Please try again in a moment."
        });
      });
    }else{
      gameInfoController.changeCurrentBallToSilver(userID).then((d)=>{
        return res.send(d);
      }).catch((e)=>{
        return res.send({
          "error" : true,
          "message" : "Please try again in a moment."
        });
      });
    }
  }
});

module.exports = router;
