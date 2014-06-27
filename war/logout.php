<?php
       require 'Client.php';
       require 'GrantType/IGrantType.php';
       require 'GrantType/AuthorizationCode.php';
       require '_config.php';

       $GOOGLE_AUTHORIZE_URL="https://accounts.google.com/o/oauth2/auth";
       $GOOGLE_ACCESS_TOKEN_URL="https://accounts.google.com/o/oauth2/token";

       session_start();
       $client = new OAuth2\Client($GOOGLE_CLIENT_ID, $GOOGLE_CLIENT_SECRET, OAuth2\Client::AUTH_TYPE_FORM);
       if (!isset($_GET["code"]))
       {
           $state = md5(rand());
           $_SESSION['state'] = $state;
           $authUrl = $client->getAuthenticationUrl($GOOGLE_AUTHORIZE_URL, $LOGOUT_URL, array("scope" => "openid email profile", "client_id" => $GOOGLE_CLIENT_EMAIL, "state" => $state));
           header("Location: " . $authUrl);
           die("Redirect");
       }
       else
       {
           $params = array("code" => $_GET["code"], "redirect_uri" => $LOGOUT_URL, "client_id" => $GOOGLE_CLIENT_EMAIL);
           $response = $client->getAccessToken($GOOGLE_ACCESS_TOKEN_URL, "authorization_code", $params);

           $accessTokenResult = $response["result"];

           $client->setAccessTokenType(OAuth2\Client::AUTH_TYPE_URI);
           $at = $accessTokenResult["access_token"];
           $response = $client->fetch("https://accounts.google.com/o/oauth2/revoke", array("token" => $at));

           unset($_SESSION['authenticated']);
           unset($_SESSION['email']);
           unset($_SESSION['plus_identity']);
           session_destroy();
           header("Location: index.php");
       }
?>
