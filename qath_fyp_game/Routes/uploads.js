const express = require("express");
const router = express.Router();
const multer = require('multer');
const settingsController = require('../Controllers/settings');
const jwt = require('jsonwebtoken');
const store = require("../Controllers/storeRoom");

const storage = multer.diskStorage({
  destination: function(req, file, cb) {
    cb(null, __dirname + '/uploadedImages');
  },
  filename: function(req, file, cb) {
    cb(null, (new Date).getTime() + file.originalname);
  }
});

const fileFilter = (req, file, cb) => {
  // reject a file
  if (file.mimetype === 'image/jpg' || file.mimetype === 'image/jpeg' || file.mimetype === 'image/png') {
    cb(null, true);
  } else {
    cb(null, false);
  }
};

const upload = multer({
  storage: storage,
  fileFilter: fileFilter
});

router.post("/profile", upload.single('image'), (req, res, next) => {

  var API_KEY = req.body.API_KEY;
  if(API_KEY == undefined || !API_KEY.trim()){
    res.json({"error" : true,"message" : "Invalid Request"});
  }else if(API_KEY != store.API_KEY){
    res.json({"error" : true,"message" : "Invalid Request"});
  }else{
    // good to go with key
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
          var user = decoded.user;
          // good to go with token
          if(req.file === undefined ){
            res.send({
              "error" : true,
              "message" : "Required params are missing"
            });
          }else{
            var imageURL = req.file.filename;
            var ob = {
              "provider" : "Qath",
              "image" : imageURL
            };

            settingsController.updateUserProfile(ob,user).then((data)=>{
              console.log(data);
              res.send(data);
            }).catch((err)=>{
              console.log(err);
              res.send(err);
            });
          }
        }
      });
    }
  }
});

module.exports = router;
