<?php
       session_start();
       unset($_SESSION['authenticated']);
       unset($_SESSION['email']);
       unset($_SESSION['identity']);
       session_destroy();
       header("Location: index.php");
?>
