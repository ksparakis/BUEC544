<?php
 error_reporting(E_ALL);
 ini_set('display_errors', 1);
 

require_once 'thermostat_api/include/Main_Functions.php';


 
    // include db handler
     $main = new Main_Functions();
    

if (isset($_POST['tag']) && $_POST['tag'] != '') 
{
    // get tag
    $tag = $_POST['tag'];
	
    // response Array
    $response = array('tag'=> $tag, 'success' => 0, 'error' => 0);
   // check for tag type
    if ($tag == "report_temp") {
        // Request type is check Login
        $temperature = $_POST['temperature'];
        $device_id = $_POST['device_id'];
       // $location_id = $_POST[KEY::LOCATION_ID];
        $transmission_id = $_POST['transmission_id'];
      


        $calibration = $main->getCalibration($device_id);
        $temperature += $calibration;
        // check for user
        $post = $main->postTemp($temperature,$device_id, $transmission_id);
       
       if($post == true)
       {
       	 	$response["success"] = 1;
            $response["success_msg"] = "Posted location: "+$device_id+" , Transmission: "+$transmission_id+" ...Successfuly";
            echo json_encode($response);
        } else {
            // user not found
            // echo json with error = 1
            $response["error"] = 1;
            $response["error_msg"] = "Error";
            echo json_encode($response);
        }
		
    }

 
 
  }
   else {
	
    echo "Access Denied";
}










