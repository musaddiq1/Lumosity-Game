const dbConnection = require("./dbConnection");
const gameInfoController = require("./gameInfo");
const async = require("async");
const jwt = require('jsonwebtoken');
const store = require("./storeRoom");
const emailController = require("./email");

function createUserAccount(username,email,provider_id,provider,profile,password,callback){

  if(store.USER_PROVIDER_APP_AUTH.trim() == provider){
    // check only email -- user is trying to create account using the app mode

    dbConnection.queryWithParams("Select * from users where email = ? ",[email]).then((data)=>{
      if ( data === undefined || data.length == 0 ) {
        // user does not exits - create user
        var status = "unApproved";

        var profileOb = {
          "provider" : "Qath",
          "image" : profile
        };


        emailController.sendVerificationEmail(email,(e,code)=>{
          if(e){
            return callback(true,"Something went wrong.Please try again later.");
          }else{

            var values = {
              "name" : username,
              "email" : email,
              "profile" : JSON.stringify(profileOb),
              "provider" : provider,
              "provider_id" : provider_id,
              "status" : status,
              "password" : password,
              "code" : code
            };

            dbConnection.queryWithParams("INSERT INTO users SET ?",values).then((results)=>{
              var user = {
                "username" : username,
                "email" : email,
                "provider_id" : provider_id,
                "provider" : provider,
                "profile" : JSON.stringify(profileOb),
                "userID" : results.insertId,
                "status" : status
              };

              jwt.sign({ user : user }, store.SECRET_KEY , { expiresIn: 2419200000 }, function(err, jwt_token) {
                if(err){
                  return callback(true,"Something went wrong.Please try again later.");
                }else{
                  // create user game info -
                  gameInfoController.createUserGameInfo(user.userID).then((gameInfoID)=>{
                    user["gameInfoID"] = gameInfoID;
                    user["coins"] = 10000;
                    user["gamesLost"] = 0;
                    user["gamesWon"] = 0;

                    user["currentBalls"] = "SILVER_BALL";
                    user["goldPurhcased"] = 0;
                    user["diamondPurhcased"] = 0;
                    user["best_game_won"] = "none";
                    user["rank"] = "Ace";

                    return callback(false,'Account created successfully',user,jwt_token);
                  }).catch((err)=>{
                    return callback(true,"Something went wrong.Please try again later.");
                  });
                }
              });

            }).catch((err)=>{
              return callback(true,"Something went wrong.Please try again later.");
            });
          }
        });
      }else{
        // user already exits - check the provider
        return callback(true,"User already exits with this Email.");
      }
    }).catch((err)=>{
      console.log(err);
      return callback(true,"Something went wrong.Please try again later.");
    });


  }else{
    dbConnection.queryWithParams("Select * from users where ( ( provider_id = ? AND provider = ? )  )",[provider_id,provider]).then((data)=>{
      if ( data === undefined || data.length == 0 ) {
        // user does not exits - create user
        var status = "unApproved";
        if(store.USER_PROVIDER_APP_AUTH != provider){
          status = "approved";
        }

        var profileOb = {
          "provider" : "other",
          "image" : profile
        };


        var values = {
          "name" : username,
          "email" : email,
          "provider" : provider,
          "profile" : JSON.stringify(profileOb),
          "provider_id" : provider_id,
          "status" : status,
          "password" : password
        };
        dbConnection.queryWithParams("INSERT INTO users SET ?",values).then((results)=>{
          var user = {
            "username" : username,
            "email" : email,
            "provider_id" : provider_id,
            "provider" : provider,
            "profile" : JSON.stringify(profileOb),
            "userID" : results.insertId,
            "status" : status
          };

          jwt.sign({ user : user }, store.SECRET_KEY , { expiresIn: 2419200000 }, function(err, jwt_token) {
            if(err){
              return callback(true,"Something went wrong.Please try again later.");
            }else{
              gameInfoController.createUserGameInfo(user.userID).then((gameInfoID)=>{
                user["gameInfoID"] = gameInfoID;
                user["coins"] = 10000;
                user["gamesLost"] = 0;
                user["gamesWon"] = 0;

                user["currentBalls"] = "SILVER_BALL";
                user["goldPurhcased"] = 0;
                user["diamondPurhcased"] = 0;
                user["best_game_won"] = "none";
                user["rank"] = "Ace";

                return callback(false,'Account created successfully',user,jwt_token);
              }).catch((err)=>{
                return callback(true,"Something went wrong.Please try again later.");
              });
            }
          });

        }).catch((err)=>{
          if(err.sqlState == 23000){
            return callback(true,"User already exits with this email address.");
          }else{
            return callback(true,"Something went wrong.Please try again later.");
          }
        });
      }else{
        // user already exits - check the provider
        var profileOb = {
          "provider" : "other",
          "image" : profile
        };

        if(store.USER_PROVIDER_APP_AUTH.trim() == provider){
          return callback(true,"User already exits with this Email.");
        }else{
          // its facebook/gmail request -- return user object
          var user = {
            "username" : username,
            "email" : email,
            "provider_id" : provider_id,
            "provider" : provider,
            "profile" : JSON.stringify(profileOb),
            "userID" : data[0].ID,
            "status" : data[0].status
          };
          jwt.sign({ user : user }, store.SECRET_KEY , { expiresIn: 2419200000 }, function(err, jwt_token) {
            if(err){
              return callback(true,"Something went wrong.Please try again later.");
            }else{
              // return user game info also
              gameInfoController.userGameInfo(user.userID).then((popi)=>{
                if(popi.userID == -99){
                  return callback(true,"Something went wrong.Please try again later.");
                }else{
                  user["gameInfoID"] = popi.ID;
                  user["coins"] = popi.coins;
                  user["gamesLost"] = popi.gameLost;
                  user["gamesWon"] = popi.gameWon;
                  user["currentBalls"] = popi.currentBalls;
                  user["goldPurhcased"] = popi.goldPurhcased != 0;
                  user["diamondPurhcased"] = popi.diamondPurhcased != 0;
                  user["best_game_won"] = popi.best_game_won;
                  user["rank"] = popi.rank;
                  return callback(false,'User Signed in successfully',user,jwt_token);
                }
              }).catch((err)=>{
                return callback(true,"Something went wrong.Please try again later.");
              });
            }
          });
        }
      }
    }).catch((err)=>{
      console.log(err);
      return callback(true,"Something went wrong.Please try again later.");
    });
  }
}

function userSignIn(email,password,callback){
  dbConnection.queryWithParams("Select * from users where email = ? AND password = ?",
  [email,password]).then((data)=>{
    if (data === undefined || data.length == 0) {
      dbConnection.queryWithParams("Select * from users where email = ?",[email]).then((results)=>{
        if (results === undefined || results.length == 0) {
          return callback(true,'User does not exit with this email.');
        }else {
          if(results[0].provider.trim() == store.USER_PROVIDER_APP_AUTH){
            return callback(true,'password is incorrect');
          }else{
            var msg;
            if(results[0].provider.trim() == store.USER_PROVIDER_GMAIL){
              msg = "Please log in using the Gmail login provider.";
            }else{
              msg = "Please log in using the Facebook login provider.";
            }
            return callback(true,msg);
          }
        }
      }).catch((err)=>{
        return callback(true,'Something went wrong.\nPlease try again later.');
      });
    }else{
      if(data[0].provider.trim() == store.USER_PROVIDER_GMAIL){
        return callback(true,"Please log in using the Gmail login provider.");
      }else if(data[0].provider.trim() == store.USER_PROVIDER_FACEBOOK){
        return callback(true,"Please log in using the Facebook login provider.");
      }else{
        var user = {
          "username" : data[0].name,
          "email" : email,
          "provider_id" : data[0].provider_id,
          "provider" : data[0].provider,
          "profile" : data[0].profile,
          "userID" : data[0].ID,
          "status" : data[0].status
        };
        jwt.sign({ user : user }, store.SECRET_KEY , { expiresIn: 2419200000 }, function(err, token) {
          if(err){
            return callback(true,'Something went wrong.\nPlease try again later.');
          }else{
            gameInfoController.userGameInfo(user.userID).then((popi)=>{
              if(popi.userID == -99){
                return callback(true,"Something went wrong.Please try again later.");
              }else{
                user["gameInfoID"] = popi.ID;
                user["coins"] = popi.coins;
                user["gamesLost"] = popi.gameLost;
                user["gamesWon"] = popi.gameWon;
                user["currentBalls"] = popi.currentBalls;
                user["goldPurhcased"] = popi.goldPurhcased != 0;
                user["diamondPurhcased"] = popi.diamondPurhcased != 0;
                user["best_game_won"] = popi.best_game_won;
                user["rank"] = popi.rank;
                return callback(false,'User Log in successfully',user,token);
              }
            }).catch((err)=>{
              return callback(true,"Something went wrong.Please try again later.");
            });
          }
        });
      }
    }
  }).catch((err)=>{
    console.log(err);
    return callback(true,'Something went wrong.\nPlease try again later.');
  });
}

function setUserActiveSession(ID,state){
  dbConnection.queryWithParams("update users SET last_seen = ? WHERE ID = ?",[state,ID]).then((data)=>{}).catch((err)=>{});
}

function managedUserPings(ID,callback){
  dbConnection.queryWithParams("select pings from users where ID = ?",[ID]).then((data)=>{
    if(data === undefined || data.length === 0){
      return callback(false,0);
    }else{
      var userPings = data[0].pings;
      if(userPings >= 3600){
        dbConnection.queryWithParams("update game_info set coins = coins + 500 where userID = ?",[ID]).then((data)=>{}).catch((err)=>{});
        dbConnection.queryWithParams("update users set pings = 0 where ID = ?",[ID]).then((data)=>{
          return callback(true,0);
        }).catch((err)=>{
          console.log(err);
          return callback(false,userPings);
        });
      }else{
        dbConnection.queryWithParams("update users set pings = pings + 1 where ID = ?",[ID]).then((data)=>{
          return callback(false,userPings + 1);
        }).catch((err)=>{
          console.log(err);
          return callback(false,userPings);
        });
      }
    }
  }).catch((err)=>{
    console.log(err);
    return callback(false,0);
  });
}

function getUserLastSeen(userID){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("select * from users where ID = ?",[userID]).then((data)=>{
      if(data === undefined || data.length === 0 ){
        return a("none");
      }else{
        return a(data[0].last_seen);
      }
    }).catch((err)=>{
      return b(err);
    });
  });
}

function getUserInfoByID(ID,callback){
  dbConnection.queryWithParams("select u.name,u.profile,g.ID as gameInfoID,g.coins,g.gameWon,g.gameLost,g.currentBalls,g.best_game_won,g.rank from users u  join game_info g on u.ID = g.userID  where u.ID = ?",[ID]).then((data)=>{
    return callback(false,{
      "name" : data[0].name,
      "profile" : JSON.parse(data[0].profile),
      "gameInfoID" : data[0].gameInfoID,
      "coins" : data[0].coins,
      "gameWon" : data[0].gameWon,
      "gameLost" : data[0].gameLost,
      "currentBalls" : data[0].currentBalls,
      "best_game_won" : data[0].best_game_won,
      "rank" : data[0].rank
    });
  }).catch((err)=>{
    console.log(err);
    return callback(true);
  });
}

function resendVerificationEmail(email,callback){
  dbConnection.queryWithParams("select * from users where email = ?",[email]).then((x)=>{
    if(x === undefined || x.length == 0){
      return callback(true,"User does not exit with this email address.Please create account first.");
    }else{
      emailController.sendVerificationEmail(email,(e,code)=>{
        if(e){
          return callback(true,"Something went wrong.Please try again later.");
        }else{
          dbConnection.queryWithParams("update users set code = ? where email = ? ",[code,email]).then((data)=>{
            return callback(false,"Email sent successfully");
          }).catch((e)=>{
            return callback(true,"Something went wrong.Please try again later.");
          });
        }
      });
    }
  }).catch((err)=>{
    return callback(true,"Something went wrong.Please try again later.");
  });
}

function verifyEmailAddress(email,code,from,callback){
  dbConnection.queryWithParams("select code from users where email = ?",[email]).then((_data)=>{
    if(_data === undefined || _data.length === 0){
      // user does not exits
      return callback(true,"User does not exit with this email address.");
    }else{
      if(_data[0].code == -99){
        resendVerificationEmail(email,(err,message)=>{
          if(err){
            return callback(true,"Something went wrong.Please try again later.");
          }else{
            return callback(true,"Please use the new code sent to your email.");
          }
        });
      }else{
        dbConnection.queryWithParams("update users set status = 'approved' where code = ? AND email = ? ",[code,email]).then((data)=>{
          if(data.changedRows === 0 && data.affectedRows === 0){
            return callback(true,"Invalid code");
          }else{
            if(from != "forgot_password"){
              dbConnection.queryWithParams("update users set code = '-99' where email = ?",[email]).then((data)=>{}).catch((e)=>{});
            }
            return callback(false,"Account verified");
          }
        }).catch((e)=>{
          return callback(true,"Something went wrong.Please try again later.");
        });
      }
    }
  }).catch((e)=>{
    return callback(true,"Something went wrong.Please try again later.");
  });
}

function resetPassword(password,email,code,callback){
  dbConnection.queryWithParams("select code from users where email = ?",[email]).then((cData)=>{
    if(cData[0].code === "-99"){
      return callback(true,"Verification code for this email address does not exit.");
    }else{
      dbConnection.queryWithParams("update users set status = 'verified',password = ? where code = ? AND email = ?",[password,code,email]).then((data)=>{
        if(data.changedRows === 0 && data.affectedRows === 0){
          return callback(true,"Invalid verification code");
        }else{
          dbConnection.queryWithParams("update users set code = '-99' where email = ?",[email]).then((data)=>{}).catch((e)=>{});
          return callback(false,"Password Changed successfully");
        }
      }).catch((e)=>{
        return callback(true,"Something went wrong.Please try again later.");
      });
    }
  }).catch((e)=>{
    return callback(true,"Something went wrong.Please try again later.");
  });
}


module.exports = {createUserAccount,userSignIn,setUserActiveSession,getUserInfoByID,getUserLastSeen,resendVerificationEmail,verifyEmailAddress,resetPassword,managedUserPings};
