<?php
       # Implement OpenID authentication.
       require 'openid.php';
    
       try {
           # Change 'localhost' to your domain name.
           $openid = new LightOpenID('localhost');
           if($openid->mode == 'cancel') {
               echo "<p>L'utilisateur a annul&eacute; son identification.</p>";
           } elseif ($openid->validate()) {
               $data = $openid->getAttributes();

               session_start();
               $_SESSION['authenticated'] = "yes";
               $_SESSION['email'] = $data['contact/email'];
               $_SESSION['identity'] = $openid->identity;
               header("Location: index.php");
               exit;
           } else {
               echo "<p>L'utilisateur n'est pas identifi&eacute;.</p>";
           }
        } catch(ErrorException $e) {
            echo $e->getMessage();
        }
?>
