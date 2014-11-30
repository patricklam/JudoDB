<?php session_start(); ?>

<!doctype html>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta name="gwt:property" content="locale=fr_CA">

	<link type="text/css" rel="stylesheet" href="screen.css" media="screen">
	<link type="text/css" rel="stylesheet" href="print.css" media="print">

    <title>Facturation Judo Qu&eacute;bec: acc&egrave;s interdit</title>
  </head>

  <body>
    <h1>Acc&egrave;s interdit</h1>

    <p>Le compte <?php echo $_SESSION['email'] ?> n'est pas autoris&eacute; &agrave; d'acc&eacute;der au base des donn&eacute;es.
      Veuillez vous <a href="logout.php">re-identifier avec un autre compte</a> (cliquer sur <?php echo $_SESSION['email'] ?> &#x25be; dans le coin en haut &agrave; droite) ou bien contacter l'administrateur du site.</p>
  </body>
</html>
