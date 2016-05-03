 <?php

echo"
<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>
<html xmlns='http://www.w3.org/1999/xhtml'>
<head>
<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />
<title>EC544 Thermostat</title>
 <!-- Bootstrap core CSS -->
    <link href='css/bootstrap.css' rel='stylesheet'>
        <script src='js/jquery-1.10.2.min.js'></script>
	<script src='js/bootstrap.js'></script>


</head>
<div class='span12'>
<form>
<table class='table table-striped' style='width:500px'>
<thead>
    <tr>
    <th>#</th>
    <th>Device Id</th>
    <th>Calibration</th>
    </tr>
    </thead>
    <tbody>
  <tr>
";

   require_once 'thermostat_api/include/Main_Functions.php';

    // include db handler
     $main = new Main_Functions();

   $mysqli = $main->createCon();
    $count =0;
    if ($result = $mysqli->query("SELECT * FROM device_calibration")) {
        while($row = $result->fetch_array())
 		 {
      echo "<td>".$count."</td>";
      $count++;
			  echo "<td>".$row['device_uid'] . "</td><td> <input value='" . $row['calibration_index']."'></input></td>";
 			  echo "</tr>
        <tr>";
  		 }
 
    }

echo"
</form>
</table>
</div>
<body>
</body
</html>
";

?>