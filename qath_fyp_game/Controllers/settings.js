const dbConnection = require("./dbConnection");
const jwt = require('jsonwebtoken');
const store = require("./storeRoom");

function updateUserName(name,decodedUser){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams('UPDATE users set name = ? where ID = ?',[name,decodedUser.userID]).then((results)=>{
      if(results.changedRows == 0 && results.affectedRows == 0){
        return a({
          "error" : true,
          "message" : "Invalid user account Information"
        });
      }else{
        var user = {
          "username" : name,
          "email" : decodedUser.email,
          "provider_id" : decodedUser.provider_id,
          "provider" : decodedUser.provider,
          "profile" : decodedUser.profile,
          "userID" : decodedUser.userID,
          "status" : decodedUser.status
        };
        jwt.sign({ user : user }, store.SECRET_KEY , { expiresIn: 2419200000 }, function(err, token) {
          if(err){
            return a({
              "error" : true,
              "message" : 'Oops.\nYour name is updated but we could not proceed ahead.\nPlease log in again.'
            });
          }else{
            return a({
              "error" : false,
              "message" : "name updated successfully",
              "token" : token
            });
          }
        });
      }
    }).catch((error)=>{
      var res = {
        "error" : true,
        "message" : "Something went wrong.\nPlease try again in a moment."
      };
      return b(res);
    });
  });
}

function updateUserPassword(password,decodedUser){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams('UPDATE users set password = ? where ( ID = ? AND provider = "USER_PROVIDER_APP_AUTH" )',[password,decodedUser.userID]).then((results)=>{
      if(results.changedRows == 0 && results.affectedRows == 0){
        return a({
          "error" : true,
          "message" : "Invalid user account Information"
        });
      }else{
        return a({
          "error" : false,
          "message" : "password updated successfully"
        });
      }
    }).catch((error)=>{
      var res = {
        "error" : true,
        "message" : "Something went wrong.\nPlease try again in a moment."
      };
      return b(res);
    });
  });
}

function updateUserProfile(profileAddress,decodedUser){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams('UPDATE users set profile = ? where ID = ?',[JSON.stringify(profileAddress),decodedUser.userID]).then((results)=>{
      if(results.changedRows == 0 && results.affectedRows == 0){
        return a({
          "error" : true,
          "message" : "Invalid user account Information"
        });
      }else{
        var user = {
          "username" : decodedUser.name,
          "email" : decodedUser.email,
          "provider_id" : decodedUser.provider_id,
          "provider" : decodedUser.provider,
          "profile" : profileAddress,
          "userID" : decodedUser.userID,
          "status" : decodedUser.status
        };
        jwt.sign({ user : user }, store.SECRET_KEY , { expiresIn: 2419200000 }, function(err, token) {
          if(err){
            return a({
              "error" : true,
              "message" : 'Oops.\nYour profile is updated but we could not proceed ahead.\nPlease log in again.'
            });
          }else{
            return a({
              "error" : false,
              "message" : "profile updated successfully",
              "token" : token,
              "profile" : profileAddress.image,
              "provider" : profileAddress.provider
            });
          }
        });
      }
    }).catch((error)=>{
      console.log(error);
      var res = {
        "error" : true,
        "message" : "Something went wrong.\nPlease try again in a moment."
      };
      return b(res);
    });
  });
}

module.exports = {
  updateUserName,updateUserPassword,updateUserProfile
};
