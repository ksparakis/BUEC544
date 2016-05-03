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



 if ($result = $mysqli->query("SELECT * FROM `device_calibration` WHERE `device_uid`='{$device_id}'")) {
        while($row = $result->fetch_array())
         {
            $calibrator = $row['calibration_index'];
            echo'works';
         }
 
    }
     $mysqli->close();
     return $calibrator;

 }

 public function getLiveData()
 {
    $theData = array('success' => 0, 'error' => 1);
    $mysqli = $this->createCon();

  if ($result = $mysqli->query("SELECT * FROM temperatures WHERE date_created BETWEEN DATE_SUB(NOW(), INTERVAL 1 MINUTE) AND NOW();")) {
   
    while($row = $result->fetch_array())
     {
       
      if( $result->num_rows >0)
      {
         $theData['error'] = 0;
          $theData['success'] = 1;
          $theData[$row['device_id']]= $row['temperature'];
        
      }
      else
      {
          echo json_encode($theData);
         return;
      }
     }
   }
 
      //$mysqli->close();
    echo json_encode($theData);
}


public function simulationInsert()
{
  $mysqli = $this->createCon();
  if ($result = $mysqli->query("SELECT * FROM `device_calibration`")) {

    while($row = $result->fetch_array())
     {
       
      $rando = rand(0 , 90);
      echo "Inserted ".$rando." into ".$row['device_uid']."</br>";
         if($result2 = $mysqli->query("INSERT INTO `temperatures` (`temp_id`, `device_id`, `location_id`, `temperature`, `date_created`, `transmission_id`) VALUES (NULL, '{$row['device_uid']}', NULL, {$rando}, CURRENT_TIMESTAMP, '');"))
         {

         }
  
     }
   }


}

}

?>
