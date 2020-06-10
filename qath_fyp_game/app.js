const express = require("express");
const app = express();
const http = require('http').Server(app);
var io = require('socket.io')(http);

const _Parser = require("body-parser");
app.use(_Parser.urlencoded({extended : false}));
app.use(_Parser.json());
app.use("/uploadedFiles",express.static('./Routes/uploadedImages'));
app.set('view engine','ejs');
const jwt = require('jsonwebtoken');
const ArrayList = require('arraylist');
const async = require("async");

//Controllers
const store = require("./Controllers/storeRoom");
const usersController = require("./Controllers/users");
const gameInfoController = require("./Controllers/gameInfo");
const friendsController = require("./Controllers/friends");

//Socket Handlers
const gameHandler = require("./socketHandlers/game");

//Global Variables
var onlineUsers = new ArrayList;
var requestsForGame = new ArrayList;
var definedRequestsForGame = new ArrayList;
var currentGames = new ArrayList;

app.get("/test",(req,res)=>{
  userFriendStats(3).then((d)=>{
    res.send(d);
  }).catch((e)=>{
    res.send(e);
  });
});

app.get("/game",(req,res,next)=>{
  res.render("game");
});

app.get("/game1",(req,res,next)=>{
  res.render("game1");
});

app.get('/stats',(req,res,next)=>{
  console.log("-----------------------------------------------------------------------------------------------");
  console.log("Online Users : ",onlineUsers);
  console.log("requestsForGame : ",requestsForGame);
  console.log("currentGames : ",currentGames);
  console.log("definedRequestsForGame : ",definedRequestsForGame);
  console.log("-----------------------------------------------------------------------------------------------");
  res.send(JSON.stringify(onlineUsers)+"\n"+JSON.stringify(requestsForGame)+"\n"+JSON.stringify(currentGames)+"\n"+JSON.stringify(definedRequestsForGame));
});

app.get("/",(req,res,next)=>{
  res.send({"error" : false, "message" : "ello someone 0_0"});
});

var api_auth = (req,res,next) => {
  var API_KEY = req.body.API_KEY;
  if(API_KEY == undefined || !API_KEY.trim()){
    res.json({"error" : true,"message" : "Invalid Request"});
  }else if(API_KEY != store.API_KEY){
    res.json({"error" : true,"message" : "Invalid Request"});
  }else{
    next();
  }
};

var TOKEN_AUTH = (req,res,next) => {
  var token = req.body.token;
  if(token == undefined || !token.trim()){
    res.json({"error" : true,"message" : "Invalid Request"});
  }else{
    jwt.verify(token, store.SECRET_KEY , function(err, decoded) {
      if(err){
        return res.send({
          "error" : true,
          "message" : "Authentication Failed"
        });
      }else{
        req.body.user = decoded.user;
        next();
      }
    });
  }
};

var userFriendStats = (userID) => {
  return new Promise((a,b)=>{
    friendsController.getAllFriendsIDsByUserID(userID).then((d)=>{
      return a(gameHandler.onlineFriendStats(d,requestsForGame,currentGames,onlineUsers,definedRequestsForGame,userID));
    }).catch((e)=>{
      return b(e);
    });
  });
};

app.use("/users", api_auth , require("./Routes/users"));
app.use("/settings", api_auth , TOKEN_AUTH ,  require("./Routes/settings"));
app.use("/friends", api_auth , TOKEN_AUTH ,  require("./Routes/friends"));
app.use('/uploads', require('./Routes/uploads'));

io.on("connection",(socket)=>{

  socket.on("join",(user,callback)=>{
    console.log(user);
    if(user == undefined || callback == undefined){
      return "Authentication failed";
    }else{
      if(user.token == undefined || !user.token.trim() || user.API_KEY == undefined || !user.API_KEY.trim()){
        return callback({
          "error" : true,
          "message" : "Authentication Failed"
        });
      }else{
        if(user.API_KEY.trim() != store.API_KEY){
          return callback({
            "error" : true,
            "message" : "Authentication Failed"
          });
        }else{
          jwt.verify(user.token, store.SECRET_KEY , function(err, decoded) {
            if(err){
              return callback({
                "error" : true,
                "message" : "Authentication Failed"
              });
            }else{
              socket.userID = decoded.user.userID;
              socket.user = decoded.user;
              socket.connectedDevice = socket.id;
              onlineUsers.add({
                "user_name" : decoded.user.username,
                "userID" : decoded.user.userID,
                "provider" : decoded.user.provider,
                "socketID" : socket.id
              });
              var state = "online";
              usersController.setUserActiveSession(socket.userID,state);
              usersController.managedUserPings(socket.userID,(isReached,pings)=>{
                  if(isReached){
                      io.to(socket.connectedDevice).emit('creditGenerated',{"error" : false,"message" : "Hurrah! You have been credit with 500 coins.\nAfter every 1 hour of connectivity with our servers, you are credit with 500 coins."});
                  }
                  io.to(socket.connectedDevice).emit('pingsCounter',{"error" : false, "pings" : pings});
              });
              console.log("User connected : userID = ",socket.userID);
              return callback({
                "error" : false,
                "message" : "Connected successfully"
              });
            }
          });
        }
      }
    }
  });// join ends here

  socket.on('onlineFriends',(callback)=>{
    if(socket.userID == undefined){
      return callback({data : []});
    }else{
      userFriendStats(socket.userID).then((d)=>{
        return callback({
          data : d
        });
      }).catch((e)=>{
        return callback({data : []});
      });
    }
  });

  socket.on("sendChatMessage",(data,callback)=>{
    if(socket.userID == undefined){
      return callback({
        "error" : true,
        "message" : "Please try again in a moment."
      });
    }else{
      var time = (new Date).getTime();
      data['time'] = time;
      io.to(data.otherPlayerSocketID).emit('chatMessageReceived',{"error" : false,"message" : "new message","data" : data});
      return callback({
        "error" : false,
        "message" : "sent.",
        "time" : time
      });
    }
  });

  socket.on("requestGame",(callback)=>{
    if(socket.userID == undefined){
      return callback({
        "error" : true,
        "message" : "Please try again in a moment."
      });
    }else{

      // verify user has enough coins to play
      gameInfoController.userGameInfo(socket.userID).then((data)=>{

        if(data.userID != -99){
          var coins = data.coins;
          if(coins >= 500){
            // good to go
            gameHandler.handleGameRequest(socket,callback,requestsForGame,currentGames,io,data.rank);
          }else{
            return callback({
              "error" : true,
              "message" : "Oho.\nYou do not have enough coins to play the game.\nMinimum coins for game are 500."
            });
          }
        }else{
          return callback({
            "error" : true,
            "message" : "Something is wrong with your account info.\nPlease contact the support team."
          });
        }
      }).catch((err)=>{
        return callback({
          "error" : true,
          "message" : "Please try again in a moment."
        });
      });
    }
  });

  socket.on("requestDefinedGame",(userData,callback)=>{

    if(socket.userID == undefined || !userData){
      return callback({
        "error" : true,
        "message" : "Please try again in a moment."
      });
    }else{

      // verify user has enough coins to play
      gameInfoController.userGameInfo(socket.userID).then((data)=>{

        if(data.userID != -99){
          var coins = data.coins;
          if(coins >= 500){
            // check friend coins

            gameInfoController.userGameInfo(userData.friendID).then((f_data)=>{

              if(f_data.userID != -99){
                var coins = f_data.coins;
                if(coins >= 500){
                  // good to go
                  gameHandler.handleDefinedGameRequest(socket,callback,definedRequestsForGame,currentGames,io,userData,onlineUsers);
                }else{
                  return callback({
                    "error" : true,
                    "message" : "Oho.\n"+userData.friendName+" do not have enough coins to play the game.\nMinimum coins for game are 500."
                  });
                }
              }else{
                return callback({
                  "error" : true,
                  "message" : "Something is wrong with "+userData.friendName+"'s account info.\nPlease contact the support team."
                });
              }
            }).catch((err)=>{
              return callback({
                "error" : true,
                "message" : "Please try again in a moment."
              });
            });
          }else{
            return callback({
              "error" : true,
              "message" : "Oho.\nYou do not have enough coins to play the game.\nMinimum coins for game are 500."
            });
          }
        }else{
          return callback({
            "error" : true,
            "message" : "Something is wrong with your account info.\nPlease contact the support team."
          });
        }
      }).catch((err)=>{
        return callback({
          "error" : true,
          "message" : "Please try again in a moment."
        });
      });
    }
  });

  socket.on("sendMoveToPlayer",(ob,callback)=>{
    console.log("sendMoveToPlayer : ",ob);
    if(socket.userID == undefined){
      return callback({
        "error" : true,
        "message" : "Authentication Failed"
      });
    }else{
      io.to(ob.emitTo).emit('receivedPlayerMove',{"error" : false,"message" : "receivedPlayerMove","index" : ob.index,"type" : ob.type});
      io.to(ob.myID).emit('receivedPlayerMove',{"error" : false,"message" : "receivedPlayerMove","index" : ob.index,"type" : ob.type});
      return callback({
        "error" : false,
        "message" : "DONE"
      });
    }
  });

  socket.on("quitGame",(callback)=>{
    if(socket.userID == undefined){
      return callback({
        "error" : true,
        "message" : "Authentication Failed"
      });
    }else{
      async.forEach(currentGames,(ob)=>{
        if(ob != undefined){

          var whiteGameInfoID = ob.whiteInfo.gameInfoID;
          var blackGameInfoID  = ob.blackInfo.gameInfoID;

          if( ob.white === socket.userID || ob.white_socketID === socket.connectedDevice ){
            // white has left the game so black wins
            gameInfoController.incrementPlayerCoins(blackGameInfoID,500);
            gameInfoController.decrementPlayerCoins(whiteGameInfoID,500);
            io.to(ob.black_socketID).emit('gameFinished',{
              "error" : false,
              "message" : "White has left the game.\nYou WON this game.",
              "wonPlayerID" : ob.black
            });
          }else if( ob.black === socket.userID || ob.black_socketID === socket.connectedDevice ){
            // black has left the game so white wins
            gameInfoController.incrementPlayerCoins(whiteGameInfoID,500);
            gameInfoController.decrementPlayerCoins(blackGameInfoID,500);
            io.to(ob.white_socketID).emit('gameFinished',{
              "error" : false,
              "message" : "Black has left the game.\nYou WON this game.",
              "wonPlayerID" : ob.white
            });
          }

          currentGames.remove(ob);
        }
      });
      return callback({
        "error" : false,
        "message" : "Game closed"
      });
    }
  });

  socket.on("cancelGameRequest",(callback)=>{
    if(socket.userID == undefined){
      return callback({
        "error" : true,
        "message" : "Authentication Failed"
      });
    }else{
      async.forEach(requestsForGame,(ob)=>{
        if(ob != undefined && ( ob.userID === socket.userID || ob.socketID === socket.connectedDevice ) ){
          requestsForGame.remove(ob);
        }
      });
      return callback({
        "error" : false,
        "message" : "Cancelled"
      });
    }
  });

  socket.on("cancelDefinedGameRequest",(callback)=>{
    if(socket.userID == undefined){
      return callback({
        "error" : true,
        "message" : "Authentication Failed"
      });
    }else{
      async.forEach(definedRequestsForGame,(ob)=>{
        if(ob != undefined && ( ob.userID === socket.userID || ob.socketID === socket.connectedDevice ) ){
          definedRequestsForGame.remove(ob);
        }
      });
      return callback({
        "error" : false,
        "message" : "Cancelled"
      });
    }
  });

  socket.on("rejectDefinedRequest",(data,callback)=>{
    if(socket.userID == undefined || !data){
      return callback({
        "error" : true,
        "message" : "Authentication Failed"
      });
    }else{
      async.forEach(definedRequestsForGame,(ob)=>{
        if(ob != undefined && ( ob.userID === data.friendID && ob.friendID === socket.userID ) ){
          definedRequestsForGame.remove(ob);
          io.to(ob.socketID).emit('gameRequestRejected',{"error" : false,"message" : data.name+" has rejected the Game Request."});
          return callback({
            "error" : false,
            "message" : "Rejected successfully"
          });
        }
      });
    }
  });

  socket.on("gameWon",(data)=>{
    if(socket.userID == undefined || data == undefined || !data){
      return callback({
        "error" : true,
        "message" : "Authentication Failed"
      });
    }else{
      async.forEach(currentGames,(ob)=>{
        if(ob != undefined && ob.gameID == data.gameID){

          var whiteGameInfoID = ob.whiteInfo.gameInfoID;
          var blackGameInfoID  = ob.blackInfo.gameInfoID;


          var wonPlayerID = data.wonPlayerID;
          if(ob.white == wonPlayerID){
            gameInfoController.incrementPlayerCoins(whiteGameInfoID,500);
            gameInfoController.decrementPlayerCoins(blackGameInfoID,500);

            if(data.blackMoveCounter != 0 && data.whiteMoveCounter != 0){
              gameInfoController.changeBestGameStats(whiteGameInfoID,data.whiteMoveCounter);
            }

          }else{
            gameInfoController.incrementPlayerCoins(blackGameInfoID,500);
            gameInfoController.decrementPlayerCoins(whiteGameInfoID,500);

            if(data.blackMoveCounter != 0 && data.whiteMoveCounter != 0){
              gameInfoController.changeBestGameStats(blackGameInfoID,data.blackMoveCounter);
            }

          }



          io.to(data.white_socketID).emit('gameWonByPlayer',{"error" : false,"data" : data});
          io.to(data.black_socketID).emit('gameWonByPlayer',{"error" : false,"data" : data});
          currentGames.remove(ob);
          console.log(data);
        }
      });
    }
  });

  socket.on("removeBallFromView",(data)=>{
    console.log(data);
    if(socket.userID == undefined || data == undefined || !data){
      //Authentication Failed
    }else{
      io.to(data.white_socketID).emit('playerLostTurn',{"error" : false,"data" : data});
      io.to(data.black_socketID).emit('playerLostTurn',{"error" : false,"data" : data});
    }
  });

  socket.on("sendFriendRequest",(data,callback)=>{
    if(socket.userID == undefined || data == undefined || !data){
      return callback({
        "error" : true,
        "message" : "Try again in a moment"
      });
    }else{
      friendsController.createFriendRequest(data).then((res)=>{
        if(!res.error){
          io.to(data.receiverSocketId).emit('friendRequestReceived',{"error" : false,"data" : data,"friendRequestID" : res.friendRequestID});
        }
        return callback(res);
      }).catch((err)=>{
        return callback({
          "error" : true,
          "message" : "Try again in a moment"
        });
      });
    }
  });

  socket.on("acceptFriendRequest",(data,callback)=>{
    if(socket.userID == undefined || data == undefined || !data){
      return callback({
        "error" : true,
        "message" : "Try again in a moment"
      });
    }else{
      friendsController.acceptFriendRequest(data.ID).then((res)=>{
        return callback({
          "error" : false,
          "message" : "Request accepted"
        });
      }).catch((err)=>{
        return callback({
          "error" : true,
          "message" : "Try again in a moment"
        });
      });
    }
  });

  socket.on("startTheDefinedGame",(data,callback)=>{
    if(socket.userID == undefined || data == undefined || !data){
      return callback({
        "error" : true,
        "message" : "Try again in a moment"
      });
    }else{
      var objectFound = false;
      var gameOb = null;
      async.forEach(definedRequestsForGame,(ob)=>{
        if(ob != undefined && ( ob.userID === data.friendID && ob.friendID === socket.userID ) ){
          // ob found
          objectFound = true;
          gameOb = ob;
        }
      });

      if(objectFound){
        gameHandler.startDefinedGame(gameOb,socket,callback,definedRequestsForGame,currentGames,io);
      }else{
        return callback({
          "error" : true,
          "message" : "Game Request is expired.Please made another request."
        });
      }
    }
  });

  socket.on("cancelFriendRequest",(data,callback)=>{
    if(socket.userID == undefined || data == undefined || !data){
      return callback({
        "error" : true,
        "message" : "Try again in a moment"
      });
    }else{
      friendsController.deleteFriendRequest(data.ID).then((res)=>{
        return callback({
          "error" : false,
          "message" : "Request cancelled"
        });
      }).catch((err)=>{
        return callback({
          "error" : true,
          "message" : "Try again in a moment"
        });
      });
    }
  });

  socket.on('pong', function(user,callback){
    // heart beat received from client - check if user in socket session is undefined - re connect user
    if(socket.userID === undefined){
      if(user == undefined || callback == undefined){
        return "Authentication failed";
      }else{
        if(user.token == undefined || !user.token.trim() || user.API_KEY == undefined || !user.API_KEY.trim()){
          return callback({
            "error" : true,
            "message" : "Authentication Failed"
          });
        }else{
          if(user.API_KEY.trim() != store.API_KEY){
            return callback({
              "error" : true,
              "message" : "Authentication Failed"
            });
          }else{
            jwt.verify(user.token, store.SECRET_KEY , function(err, decoded) {
              if(err){
                return callback({
                  "error" : true,
                  "message" : "Authentication Failed"
                });
              }else{
                socket.userID = decoded.user.userID;
                socket.user = decoded.user;
                socket.connectedDevice = socket.id;
                onlineUsers.add({
                  "user_name" : decoded.user.username,
                  "userID" : decoded.user.userID,
                  "provider" : decoded.user.provider,
                  "socketID" : socket.id
                });
                var state = "online";
                usersController.setUserActiveSession(socket.userID,state);

                usersController.managedUserPings(socket.userID,(isReached,pings)=>{
                    if(isReached){
                        io.to(socket.connectedDevice).emit('creditGenerated',{"error" : false,"message" : "Hurrah! You have been credit with 500 coins.\nAfter every 1 hour of connectivity with our servers, you are credit with 500 coins."});
                    }
                    io.to(socket.connectedDevice).emit('pingsCounter',{"error" : false,"pings" : pings});
                });


                console.log("User connected : userID = ",socket.userID);
                return callback({
                  "error" : false,
                  "message" : "Connected successfully"
                });
              }
            });
          }
        }
      }
    }else{
      usersController.managedUserPings(socket.userID,(isReached,pings)=>{
          if(isReached){
              io.to(socket.connectedDevice).emit('creditGenerated',{"error" : false,"message" : "Hurrah! You have been credit with 500 coins.\nAfter every 1 hour of connectivity with our servers, you are credit with 500 coins."});
          }
          io.to(socket.connectedDevice).emit('pingsCounter',{"error" : false,"pings" : pings});
      });
    }
  });// pong ends here

  socket.on('disconnect',()=>{

    var onlineUsersTemp = new ArrayList;

    async.forEach(onlineUsers,(ob)=>{
      if(  !(ob.userID === socket.userID || ob.socketID === socket.connectedDevice) ){
        onlineUsersTemp.add(ob);
      }
    });

    onlineUsers = null;
    onlineUsers = onlineUsersTemp;

    async.forEach(requestsForGame,(ob)=>{
      if(ob != undefined && ( ob.userID === socket.userID || ob.socketID === socket.connectedDevice ) ){
        requestsForGame.remove(ob);
      }
    });

    async.forEach(definedRequestsForGame,(ob)=>{
      if(ob.friendID === socket.userID ){
        // friend went down
        io.to(ob.socketID).emit('friendWentDown',{
          "error" : false,
          "message" : "friend is down"
        });
        definedRequestsForGame.remove(ob);
      }
      if(ob != undefined && ( ob.userID === socket.userID || ob.socketID === socket.connectedDevice ) ){
        definedRequestsForGame.remove(ob);
      }
    });

    async.forEach(currentGames,(ob)=>{
      if(ob != undefined){

        var whiteGameInfoID = ob.whiteInfo.gameInfoID;
        var blackGameInfoID  = ob.blackInfo.gameInfoID;

        if( ob.white === socket.userID || ob.white_socketID === socket.connectedDevice ){
          // white has left the game so black wins
          gameInfoController.incrementPlayerCoins(blackGameInfoID,500);
          gameInfoController.decrementPlayerCoins(whiteGameInfoID,500);
          io.to(ob.black_socketID).emit('gameFinished',{
            "error" : false,
            "message" : "White has left the game.\nYou WON this game.",
            "wonPlayerID" : ob.black
          });
        }else if( ob.black === socket.userID || ob.black_socketID === socket.connectedDevice ){
          // black has left the game so white wins
          gameInfoController.incrementPlayerCoins(whiteGameInfoID,500);
          gameInfoController.decrementPlayerCoins(blackGameInfoID,500);
          io.to(ob.white_socketID).emit('gameFinished',{
            "error" : false,
            "message" : "Black has left the game.\nYou WON this game.",
            "wonPlayerID" : ob.white
          });
        }
        currentGames.remove(ob);
      }
    });

    usersController.setUserActiveSession(socket.userID,(new Date).getTime());
    console.log('user has left us : userID = ',socket.userID);
  });// disconnect ends here

});// io ends here

function sendHeartbeat(){
  setTimeout(sendHeartbeat, 1000);
  io.emit('ping', { beat : 1 });
}
setTimeout(sendHeartbeat, 1000);

app.use((req,res,next)=>{
  const error = new Error('Requested Resource not found');
  next(error);
});

app.use((error,req,res,next)=>{
  res.json({
    error : true,
    message : error.message
  });
});

http.listen(3005,()=>{
  console.log("Server is running on port 3005");
});
