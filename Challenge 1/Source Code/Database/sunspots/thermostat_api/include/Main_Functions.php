<?php
 
class Main_Functions {
 
    private $mysqli;
 
    //put your code here
    // constructor
    function __construct() {
        
        // connecting to database
        $mysqli = new mysqli("localhost", "root", "", "sunspotThermostat");

      
    }
 
    // destructor
    function __destruct() {
         
    }
 
 	public function unautherized()
	{
			$response["error"] = 455;
			$response["error_msg"] = "Unauthorized user";
			 echo json_encode($response);
	}
 
 public function createCon()
 {
     $mysqli = new mysqli("localhost", "root", "", "sunspotThermostat");
     return $mysqli;
 }

 public function postTemp($temperature,$device_id, $transmission_id)
 {
     $mysqli = $this->createCon();
    if ($mysqli->connect_errno) {
    printf("Connect failed: %s\n", $mysqli->connect_error);
    exit();
    }

 	  $result = $mysqli->query("INSERT INTO `sunspotThermostat`.`temperatures` (`temp_id`, `device_id`, `location_id`, `temperature`, `date_created`, `transmission_id`) VALUES (NULL, '{$device_id}', NULL, '{$temperature}', CURRENT_TIMESTAMP, '{$transmission_id}');");
        $mysqli->close();
        // check for successful store=
        if ($result) {
       		return true;
        } else {
            return false;
        }

       
 }

 public function getCalibration($device_id)
 {
    $calibrator=0.0;
       $mysqli = $this->createCon();

    if ($mysqli->connect_errno) {
    printf("Connect failed: %s\n", $mysqli->connect_error);
    exit();
    }



 if ($result = $mysqli->query("SELECT calibration_index FROM device_calibration WHERE device_id = '{$device_id}'")) {
        while($row = $result->fetch_array())
         {
   
            $calibrator = $row['calibration_index'];
            echo "YOLO";
         }
 
  echo "YOL4O";
    }
     $mysqli->close();
     return $calibrator;

 }


}

?>
