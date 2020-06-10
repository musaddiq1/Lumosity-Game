const dbConnection = require("./dbConnection");
const async = require("async");

function createFriendRequest(payload){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("select * from friends where ( sender = ? && receiver = ?) || ( sender = ? && receiver = ? )",[payload.sender,payload.receiver,payload.receiver,payload.sender]).then((x)=>{
      if(x === undefined || x.length == 0){
        dbConnection.queryWithParams("insert into friends set ?",{
          "status" : "pending",
          "sender" : payload.sender,
          "receiver" : payload.receiver,
          "text" : payload.text
        }).then((data)=>{
          return a({
            "error" : false,
            "message" : "Request sent",
            "friendRequestID" : data.insertId
          });
        }).catch((err)=>{
          return b(err);
        });
      }else{
        return a({
          "error" : true,
          "message" : "Request already present"
        });
      }
    }).catch((err)=>{
      return b(err);
    });
  });
}

function deleteFriendRequest(ID){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("delete from friends where ID = ?",[ID]).then((data)=>{
      return a(data);
    }).catch((err)=>{
      return b(err);
    });
  });
}

function removeFromFriends(ID){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("delete from friends where ID = ?",[ID]).then((data)=>{
      return a(data);
    }).catch((err)=>{
      return b(err);
    });
  });
}

function acceptFriendRequest(ID){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("update friends set status = 'friends' where ID = ?",[ID]).then((data)=>{
      return a(data);
    }).catch((err)=>{
      return b(err);
    });
  });
}

function areUsersFriends(userA,userB){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("select ID as friendID,status as friendStatus,sender as requestSender from friends where ( sender = ? AND receiver = ? ) || ( sender = ? AND receiver = ? )",[userA,userB,userB,userA]).then((data)=>{
      if(data === undefined || data.length === 0){
        return a({"friendID" : -99,"friendStatus" : "not_friends","requestSender" : -99});
      }else{
        return a(data[0]);
      }
    }).catch((err)=>{
      return b(err);
    });
  });
}

function getAllFriendsByUserID(userID){
  const IDs = [];
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("select f.ID,f.status,f.sender,f.receiver,f.text,u.name as receiverName,u.profile as receiverProfile from friends f join users u on f.receiver = u.ID where f.sender = ? || f.receiver = ?",[userID,userID])
    .then((data)=>{

      async.forEach(data,(d)=>{
        IDs.push(d.sender);
      });

      if(IDs.length === 0){
        return a([]);
      }else{
        dbConnection.queryWithParams("select ID as sender,name,profile from users where ID IN (?)",[IDs])
        .then((q)=>{

          async.forEach(data,(x)=>{
            async.forEach(q,(y)=>{
              if(x.sender == y.sender){
                x["senderName"] = y.name;
                x["senderProfile"] = y.profile;
              }
            });
          });

          return a(data);
        }).catch((err)=>{

          return b(err);
        });
      }
    }).catch((err)=>{
      console.log(err);
      return b(err);
    });
  });
}

function getAllFriendsIDsByUserID(userID){
  const friends = [];
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("select * from friends where sender = ? or receiver = ? AND status = 'friends' ",[userID,userID]).then((data)=>{
      async.forEach(data,(x)=>{
        if(x.sender == userID){
          friends.push(x.receiver);
        }else{
          friends.push(x.sender);
        }
      });
      dbConnection.queryWithParams("select ID as friendID,last_seen from users where ID IN (?)",[friends]).then((o)=>{
        return a(o);
      }).catch((err)=>{
        return b(err);
      });
    }).catch((err)=>{
      return b(err);
    });
  });
}

module.exports = {
  createFriendRequest,deleteFriendRequest,areUsersFriends,acceptFriendRequest,getAllFriendsByUserID,removeFromFriends,getAllFriendsIDsByUserID
};
