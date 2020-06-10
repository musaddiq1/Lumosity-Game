const dbConnection = require("./dbConnection");

function createUserGameInfo(userID){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("insert into game_info set ?",{
      "userID" : userID,
      "coins" : 10000,
      "gameWon" : 0,
      "gameLost" : 0
    }).then((data)=>{
      return a(data.insertId);
    }).catch((err)=>{
      return b(err);
    });
  });
}

function userGameInfo(userID){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("select * from game_info where userID = ? ",[userID]).then((data)=>{
      if ( data === undefined || data.length == 0 ) {
        return a({"userID" : -99});
      }
      return a(data[0]);
    }).catch((err)=>{
      return b(err);
    });
  });
}

function changeBestGameStats(ID,moves){
  dbConnection.queryWithParams("select * from game_info where ID = ? ",[ID]).then((data)=>{
    if ( data === undefined || data.length == 0 ) {
      // user has no info record
    }else{
      var previousMoves = data[0].best_game_won;
      if(moves > previousMoves){
        dbConnection.queryWithParams("update game_info set best_game_won = ? where ID = ?",[moves,ID]).then((data)=>{}).catch((err)=>{});
      }
    }
  }).catch((err)=>{
  });
}

function incrementPlayerCoins(ID,coins){
  dbConnection.queryWithParams("select * from game_info where ID = ? ",[ID]).then((data)=>{
    if ( data === undefined || data.length == 0 ) {
      // user has no info record
    }else{
      var previousCoins = data[0].coins;
      var nowCoins = previousCoins + coins;
      var previousGamesWon = data[0].gameWon;
      var nowWon = previousGamesWon + 1;
      if(nowWon > 20 && nowWon <= 60){
        dbConnection.queryWithParams("update game_info set coins = ?,gameWon = ?,rank = 'Platinum' where ID = ?",[nowCoins,nowWon,ID]).then((data)=>{}).catch((err)=>{});
      }else if(nowWon > 60){
        dbConnection.queryWithParams("update game_info set coins = ?,gameWon = ?,rank = 'Crown' where ID = ?",[nowCoins,nowWon,ID]).then((data)=>{}).catch((err)=>{});
      }else{
        dbConnection.queryWithParams("update game_info set coins = ?,gameWon = ? where ID = ?",[nowCoins,nowWon,ID]).then((data)=>{}).catch((err)=>{});
      }
    }
  }).catch((err)=>{
  });
}

function decrementPlayerCoins(ID,coins){
  dbConnection.queryWithParams("select * from game_info where ID = ? ",[ID]).then((data)=>{
    if ( data === undefined || data.length == 0 ) {
      // user has no info record
    }else{
      var previousCoins = data[0].coins;
      var nowCoins = previousCoins - coins;
      var previousGamesLost = data[0].gameLost;
      var nowLost = previousGamesLost + 1;
      dbConnection.queryWithParams("update game_info set coins = ?,gameLost = ? where ID = ?",[nowCoins,nowLost,ID]).then((data)=>{}).catch((err)=>{});
    }
  }).catch((err)=>{
  });
}

function decrementPlayerCoins_notWinRatio(ID,coins){
  dbConnection.queryWithParams("select * from game_info where ID = ? ",[ID]).then((data)=>{
    if ( data === undefined || data.length == 0 ) {
      // user has no info record
    }else{
      var previousCoins = data[0].coins;
      var nowCoins = previousCoins - coins;
      dbConnection.queryWithParams("update game_info set coins = ? where ID = ?",[nowCoins,ID]).then((data)=>{}).catch((err)=>{});
    }
  }).catch((err)=>{
  });
}

function purchaseTheGoldBalls(userID){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("update game_info set goldPurhcased = 1 where userID = ?",[userID]).then((data)=>{
      decrementPlayerCoins_notWinRatio(userID,30000);
      return a(data);
    }).catch((err)=>{
      return b(err);
    });
  });
}

function purchaseTheDiamondBalls(userID){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("update game_info set diamondPurhcased = 1 where userID = ?",[userID]).then((data)=>{
      decrementPlayerCoins_notWinRatio(userID,80000);
      return a(data);
    }).catch((err)=>{
      return b(err);
    });
  });
}

function changeCurrentBallToGold(userID){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("select goldPurhcased from game_info where userID = ?",[userID]).then((data)=>{
      if(data === undefined || data.length === 0){
        return a({error:true,message:"user information does not exit"});
      }else{
        if(data[0].goldPurhcased){
          dbConnection.queryWithParams("update game_info set currentBalls = 'GOLD_BALL' where userID = ?",[userID]).then((d)=>{return a({error:false,message:"Changed successfully"});}).catch((e)=>{return b(e);});
        }else{
          return a({error:true,message:"Please first purchase the gold balls."});
        }
      }
    }).catch((err)=>{
      return b(err);
    });
  });
}

function changeCurrentBallToDiamond(userID){
  return new Promise((a,b)=>{
    dbConnection.queryWithParams("select diamondPurhcased from game_info where userID = ?",[userID]).then((data)=>{
      if(data === undefined || data.length === 0){
        return a({error:true,message:"user information does not exit"});
      }else{
        if(data[0].diamondPurhcased){
          dbConnection.queryWithParams("update game_info set currentBalls = 'DIAMOND_BALL' where userID = ?",[userID]).then((d)=>{return a({error:false,message:"Changed successfully"});}).catch((e)=>{return b(e);});
        }else{
          return a({error:true,message:"Please first purchase the diamond balls."});
        }
      }
    }).catch((err)=>{
      return b(err);
    });
  });
}

function changeCurrentBallToSilver(userID){
  return new Promise((a,b)=>{
      dbConnection.queryWithParams("update game_info set currentBalls = 'SILVER_BALL' where userID = ?",[userID]).then((d)=>{return a({error:false,message:"Changed successfully"});}).catch((e)=>{return b(e);});
  });
}

module.exports = {changeBestGameStats,changeCurrentBallToSilver,createUserGameInfo,userGameInfo,incrementPlayerCoins,decrementPlayerCoins,purchaseTheGoldBalls,purchaseTheDiamondBalls,changeCurrentBallToGold,changeCurrentBallToDiamond};
