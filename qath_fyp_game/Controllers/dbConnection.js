const mysql = require('mysql');

const pool = mysql.createPool({
  connectionLimit : 10,
  host : 'localhost',
  user : 'root',
  password : '',
  database : 'android_game_q'
});

function queryWithParams(sqlQuery , values){
  return new Promise((resolve,reject)=>{
    pool.getConnection((error,connection)=>{
      if(error){
        return reject(error);
      }else {
        if(connection){
          connection.query(sqlQuery, values , (error,results,fields)=>{
            connection.release();
            if(error){
                return reject(error);
            }
              return resolve(results);
          });
        }
      }// if - else ends here
    });

  });
}// methods ends here

function query(sqlQuery){
  return new Promise((resolve,reject)=>{
    pool.getConnection((error,connection)=>{
      if(error){
        return reject(error);
      }else {
        if(connection){
          connection.query(sqlQuery , (error,results)=>{
            connection.release();
            if(error){
              return reject(error);
            }
            return resolve(results);
          });
        }
      }// if - else ends here
    });
  });
}// methods ends here



module.exports = {
  queryWithParams,
  query
};
