<?php
require 'Client.php';
require 'GrantType/IGrantType.php';
require 'GrantType/AuthorizationCode.php';
require '_config.php';

$GOOGLE_AUTHORIZE_URL="https://accounts.google.com/o/oauth2/auth";
$GOOGLE_ACCESS_TOKEN_URL="https://accounts.google.com/o/oauth2/token";

if (isset($_GET["error"]))
{
    echo("<pre>OAuth Error: " . $_GET["error"]."\n");
    echo('<a href="index.php">Retry</a></pre>');
    die;
}

session_start();
$_SESSION['authenticated'] = 'yes';
// $client = new OAuth2\Client($GOOGLE_CLIENT_ID, $GOOGLE_CLIENT_SECRET, OAuth2\Client::AUTH_TYPE_FORM);
if (!isset($_SESSION['authenticated']) || $_SESSION['authenticated'] != "yes")
{
    if (!isset($_SESSION['state']) && isset($_GET['state'])) {
        // xxx is this actually safe?!
        $_SESSION['state'] = $_GET['state'];
    }
    if (!isset($_GET["code"]))
    {
        $state = md5(rand());
        $_SESSION['state'] = $state;
        $authUrl = $client->getAuthenticationUrl($GOOGLE_AUTHORIZE_URL, $LOGIN_URL, array("scope" => "openid email profile", "client_id" => $GOOGLE_CLIENT_EMAIL, "state" => $state));
        header("Location: ".$authUrl);
        die("Redirect");
    }
    else
    {
        if ($_GET['state'] != $_SESSION['state']) {
            die("state doesn't match");
        }

        $params = array("code" => $_GET["code"], "redirect_uri" => $LOGIN_URL, "client_id" => $GOOGLE_CLIENT_EMAIL);
        $response = $client->getAccessToken($GOOGLE_ACCESS_TOKEN_URL, "authorization_code", $params);
        $accessTokenResult = $response["result"];
        $client->setAccessToken($accessTokenResult["access_token"]);
        $client->setAccessTokenType(OAuth2\Client::ACCESS_TOKEN_OAUTH);

        $at = $accessTokenResult["access_token"];
        $response = $client->fetch("https://www.googleapis.com/plus/v1/people/me/openIdConnect")['result'];

        if (isset($response['email_verified']) && $response['email_verified'] == 'true') {
            $_SESSION['authenticated'] = "yes";
            $_SESSION['email'] = $response['email'];
            $_SESSION['plus_identity'] = $response['sub'];
            header("Location: index.php");
            die("Redirect");
        } else {
            echo 'Usager non r&eacute;connu.';
            die;
        }
    }
}
?>

<!doctype html>

<html>
  <head>
    <!-- before your module(*.nocache.js) loading  -->
    <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <!--[if IE 7]>
      <link rel="stylesheet" href="{your module name}/css/font-awesome-ie7.css">
    <![endif]-->
    <!-- your module(*.nocache.js) loading  -->
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta name="gwt:property" content="locale=fr_CA">

    <title>Facturation Judo Qu&eacute;bec</title>
    <script type="text/javascript" language="javascript" src="judodb/judodb.nocache.js"></script>

    <link type="text/css" rel="stylesheet" href="files/screen.css" media="screen">
    <link type="text/css" rel="stylesheet" href="files/print.css" media="print">
  </head>

  <body>

    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
    
    <noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        for this application to display correctly.
      </div>
    </noscript>

  </body>
</html>
