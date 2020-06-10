var nodemailer = require('nodemailer');
const uuid = require('uuid');

var transporter = nodemailer.createTransport({
  service : 'gmail',
  auth : {
    user : '',
    pass : ''
  }
});

function sendVerificationEmail(emailAddress,callback){
  const uuidv1 = require('uuid/v1');
  var v = uuidv1();
  var arr = v.split("-");
  var mailOptions = {
    from : '',
    to : emailAddress,
    subject : 'Qath Game - Email Verification',
    text : 'Please verify your account using this code ' + arr[0]
  };
  transporter.sendMail(mailOptions, function(error, info){
    if (error) {
      console.log(error);
      return callback(true);
    } else {
      console.log(info);
      return callback(false,arr[0]);
    }
  });
}

module.exports = {sendVerificationEmail};
