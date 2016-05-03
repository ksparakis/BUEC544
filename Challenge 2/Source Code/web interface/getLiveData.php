<?php
 error_reporting(E_ALL);
 ini_set('display_errors', 1);
 

require_once 'ec544_api/include/Main_Functions.php';


 
    // include db handler
     $main = new Main_Functions();
    
        $main->getLiveData();
?>
        