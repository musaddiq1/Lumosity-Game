const uuidv1 = require('uuid/v1');
const usersController = require("../Controllers/users");
const friendsController = require("../Controllers/friends");
const async = require("async");

function handleGameRequest(socket,callback,requestsForGame,currentGames,io,rank){
  if(requestsForGame.size() == 0){
    requestsForGame.add({
      "userID" : socket.userID,
      "socketID" : socket.connectedDevice,
      "requestedAt" : (new Date).getTime(),
      "userRank" : rank
    });
    console.log("Currently no user is online. Waiting for other users request for game.");
    return callback({"error" : false, "message" : "Currently no user is online. Waiting for other users request for game.","playerFound" : false});
  }else{
    var isAreadyInArr = false;

    async.forEach(requestsForGame,(ob)=>{
      if(ob != undefined && ( ob.userID === socket.userID || ob.socketID === socket.connectedDevice ) ){
        isAreadyInArr = true;
      }
    });
    if(isAreadyInArr){
      return callback({"error" : true, "message" : "You already have a game request in request queue.","playerFound" : false});
    }else{
      //not in request queue
      var otherUser = null;

      // search for same rank
      for(let looper = 0; looper < requestsForGame.length; looper++){
          if(requestsForGame[looper].userRank == rank){
            // user found
            otherUser = requestsForGame[looper];
            break;
          }
      }

      if(otherUser == null){
        otherUser = requestsForGame.get(0);
      }

      var ob = {
        "white" : socket.userID,
        "black" : otherUser.userID,
        "white_socketID" : socket.connectedDevice,
        "black_socketID" : otherUser.socketID,
        "startedAt" : (new Date).getTime(),
        "gameID" : uuidv1(),
        "type" : "world"
      };
      currentGames.add(ob);

      requestsForGame.remove(otherUser);

      friendsController.areUsersFriends(ob.black,ob.white).then((x)=>{
        ob["friendsOb"] = x;
        usersController.getUserInfoByID(ob.white,(err,user)=>{
          if(!err){
            ob["whiteInfo"] = user;
            usersController.getUserInfoByID(ob.black,(err,user)=>{
              if(!err){
                ob["blackInfo"] = user;
              }
              io.to(ob.white_socketID).emit('gameStarted',{"error" : false,"message" : "Game Started","data" : ob});
              io.to(ob.black_socketID).emit('gameStarted',{"error" : false,"message" : "Game Started","data" : ob});

              console.log("Game started.",ob);
              return callback({"error" : false, "message" : "Game started.","playerFound" : true});
            });
          }else{
            return callback({"error" : true, "message" : "Something went wrong","playerFound" : false});
          }
        });
      }).catch((err)=>{
        return callback({"error" : true, "message" : "Something went wrong","playerFound" : false});
      });
    }
  }
}

function handleDefinedGameRequest(socket,callback,definedRequestsForGame,currentGames,io,userData,onlineUsers){
  if(definedRequestsForGame.size() === 0){
    definedRequestsForGame.add({
      "userID" : socket.userID,
      "socketID" : socket.connectedDevice,
      "requestedAt" : (new Date).getTime(),
      "friendID" : userData.friendID
    });
    console.log("Waiting for approval from friend.");
    // emit to friend
    for(var i=0;i<onlineUsers.length;i++){
      if(onlineUsers[i].userID === userData.friendID){
        io.to(onlineUsers[i].socketID).emit('requestReceivedForDefinedGame',{"error" : false,"message" : userData.requestedBy+" has requested the game.","friendID" : socket.userID});
        break;
      }
    }
    return callback({"error" : false, "message" : "Waiting for "+userData.friendName+" to accept the game request.","playerFound" : false});
  }else{
    var isAreadyInArr = false;
    var otherUser = null;
    async.forEach(definedRequestsForGame,(ob)=>{
      if(ob != undefined &&  (ob.friendID === socket.userID && ob.userID === userData.friendID) ){
        isAreadyInArr = true;
        otherUser = ob;
      }
    });
    if(isAreadyInArr && otherUser != null){
      // request is found - start the game

      var ob = {
        "white" : socket.userID,
        "black" : otherUser.userID,
        "white_socketID" : socket.connectedDevice,
        "black_socketID" : otherUser.socketID,
        "startedAt" : (new Date).getTime(),
        "gameID" : uuidv1(),
        "type" : "closed"
      };
      currentGames.add(ob);

      definedRequestsForGame.remove(otherUser);

      friendsController.areUsersFriends(ob.black,ob.white).then((x)=>{
        ob["friendsOb"] = x;
        usersController.getUserInfoByID(ob.white,(err,user)=>{
          if(!err){
            ob["whiteInfo"] = user;
            usersController.getUserInfoByID(ob.black,(err,user)=>{
              if(!err){
                ob["blackInfo"] = user;
              }
              io.to(ob.white_socketID).emit('gameStarted',{"error" : false,"message" : "Game Started","data" : ob});
              io.to(ob.black_socketID).emit('gameStarted',{"error" : false,"message" : "Game Started","data" : ob});

              console.log("Game started.",ob);
              return callback({"error" : false, "message" : "Game started.","playerFound" : true});
            });
          }else{
            return callback({"error" : true, "message" : "Something went wrong","playerFound" : false});
          }
        });
      }).catch((err)=>{
        return callback({"error" : true, "message" : "Something went wrong","playerFound" : false});
      });

    }else{
      //not in request queue
      var isAreadyInArr_d = false;
      async.forEach(definedRequestsForGame,(ob)=>{
        if(ob != undefined && ( ob.userID === socket.userID || ob.socketID === socket.connectedDevice ) && (ob.friendID === userData.friendID)){
          isAreadyInArr_d = true;
        }
      });
      if(isAreadyInArr_d){
        return callback({"error" : true, "message" : "You already have a game request in request queue.","playerFound" : false});
      }else{

        definedRequestsForGame.add({
          "userID" : socket.userID,
          "socketID" : socket.connectedDevice,
          "requestedAt" : (new Date).getTime(),
          "friendID" : userData.friendID
        });
        console.log("Waiting for approval from friend.");
        // emit to friend
        for(var i=0;i<onlineUsers.length;i++){
          if(onlineUsers[i].userID === userData.friendID){
            io.to(onlineUsers[i].socketID).emit('requestReceivedForDefinedGame',{"error" : false,"message" : userData.requestedBy+" has requested the game.","friendID" : socket.userID});
            break;
          }
        }
        return callback({"error" : false, "message" : "Waiting for "+userData.friendName+" to accept the game request.","playerFound" : false});
      }
    }
  }
}

function startDefinedGame(otherUser,socket,callback,definedRequestsForGame,currentGames,io){
  var ob = {
    "white" : socket.userID,
    "black" : otherUser.userID,
    "white_socketID" : socket.connectedDevice,
    "black_socketID" : otherUser.socketID,
    "startedAt" : (new Date).getTime(),
    "gameID" : uuidv1(),
    "type" : "closed"
  };
  currentGames.add(ob);

  definedRequestsForGame.remove(otherUser);

  friendsController.areUsersFriends(ob.black,ob.white).then((x)=>{
    ob["friendsOb"] = x;
    usersController.getUserInfoByID(ob.white,(err,user)=>{
      if(!err){
        ob["whiteInfo"] = user;
        usersController.getUserInfoByID(ob.black,(err,user)=>{
          if(!err){
            ob["blackInfo"] = user;
          }
          io.to(ob.white_socketID).emit('gameStarted',{"error" : false,"message" : "Game Started","data" : ob});
          io.to(ob.black_socketID).emit('gameStarted',{"error" : false,"message" : "Game Started","data" : ob});

          console.log("Game started.",ob);
          return callback({"error" : false, "message" : "Game started.","playerFound" : true});
        });
      }else{
        return callback({"error" : true, "message" : "Something went wrong","playerFound" : false});
      }
    });
  }).catch((err)=>{
    return callback({"error" : true, "message" : "Something went wrong","playerFound" : false});
  });
}

function onlineFriendStats(friendList,requestsForGame,currentGames,onlineUsers,definedRequestsForGame,userID){

  for(var i=0;i<friendList.length;i++){

    var f = friendList[i];

    // check user in game request queue
    var r_ob = isFriendInRequestQueue(requestsForGame,f);

    if(r_ob.found){
      f["state"] = {"type" : "requestQueue","time" : r_ob.requestedAt};
      continue;
    }

    // check user in definedGameQueue
    var r_defined_ob = isFriendInDefinedQueue(definedRequestsForGame,f,userID);

    if(r_defined_ob.found){
      if(r_defined_ob.gameWithME){
          f["state"] = {"type" : "requestQueueWithMe","time" : r_defined_ob.requestedAt};
          continue;
      }else{
          f["state"] = {"type" : "requestQueue","time" : r_defined_ob.requestedAt};
          continue;
      }
    }


    // check user in current Games queue
    if(isFriendPlayingGame(currentGames,f)){
      f["state"] = {"type" : "playingGame"};
      continue;
    }

    // check user is online and idle
    if(isUserOnlineIdle(onlineUsers,f)){
      f["state"] = {"type" : "onlineAndIdle"};
      continue;
    }

    //user is offline
    f["state"] = {"type" : "offline"};

  }// outer loop ends here

  return friendList;
}

function isFriendInDefinedQueue(definedRequestsForGame,friend,userID){
  for(var i=0;i<definedRequestsForGame.length;i++){
    if(definedRequestsForGame[i].userID === friend.friendID){
      if(definedRequestsForGame[i].friendID === userID){
        return {"found" : true,"requestedAt" : definedRequestsForGame[i].requestedAt,"gameWithME" : true};
      }else{
        return {"found" : true,"requestedAt" : definedRequestsForGame[i].requestedAt,"gameWithME" : false};
      }
    }
  }
  return {"found" : false};
}

function isFriendInRequestQueue(requestsForGame,friend){
  for(var i=0;i<requestsForGame.length;i++){
    if(requestsForGame[i].userID === friend.friendID){
      return {"found" : true,"requestedAt" : requestsForGame[i].requestedAt};
    }
  }
  return {"found" : false};
}

function isFriendPlayingGame(currentGames,friend){
  for(var i=0;i<currentGames.length;i++){
    if(currentGames[i].white === friend.friendID || currentGames[i].black === friend.friendID){
      return true;
    }
  }
  return false;
}

function isUserOnlineIdle(onlineUsers,friend){
  for(var i=0;i<onlineUsers.length;i++){
    if(onlineUsers[i].userID === friend.friendID){
      return true;
    }
  }
  return false;
}


module.exports = {handleGameRequest,onlineFriendStats,handleDefinedGameRequest,startDefinedGame};
