<?php
 error_reporting(E_ALL);
 ini_set('display_errors', 1);
 set_time_limit(0);

require_once 'ec544_api/include/Main_Functions.php';


 
    // include db handler
     $main = new Main_Functions();
    
    while(true)
    {
    
     sleep(60); 
        $main->simulationInsert();
       
    }
?>
        