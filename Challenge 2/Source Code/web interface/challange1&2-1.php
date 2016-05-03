 <?php

echo"
<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>
<html xmlns='http://www.w3.org/1999/xhtml'>
<head>
<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />
<title>EC544 Thermostat</title>
 <!-- Bootstrap core CSS -->
    <link href='css/bootstrap.css' rel='stylesheet'>
        <script src='js/jquery.min.js'></script>
	<script src='js/bootstrap.js'></script>
  <script type='text/javascript' src='https://www.google.com/jsapi'></script>

<style>
  p    {color:#cdbfe3}
  a {color:#6f5499;font-weight: bold;}
  a:hover { color:#cdbfe3;font-weight: bold;}
  h1 {color:white}
  h4 {color:white}

  a.dialer{  
    background-color:#cdbfe3;
    color: white;
    display: block;
    height: 40px;
    line-height: 40px;
    text-decoration: none;
    width: 100px;
    text-align: center;}
 
  a.dialer.active{  background-color:#6f5499 ;
    color: white;
    display: block;
    height: 40px;
    line-height: 40px;
    text-decoration: none;
    width: 100px;
    text-align: center;}
</style>
</head>


<nav class='collapse navbar-collapse bs-navbar-collapse' role='navigation'>
<a href='../' class='navbar-brand'>EC544 Team 1</a>
      <ul class='nav navbar-nav'>
        <li>
          <a href='../getting-started'>Challange 1 & 2</a>
        </li>
      </ul>
      <ul class='nav navbar-nav navbar-right'>
        <li><a href='http://www.sparakis.com' onclick='ga('send', 'event', 'Navbar', 'Community links', 'Expo');'>Sparakis</a></li>
      </ul>
    </nav>

<div class='bs-docs-header' id='content' style='background-color: #6f5499; background-image: -webkit-gradient(linear,left top,left bottom,from(#563d7c),to(#6f5499));'>
      <div class='container'>
        <h1>Challange 1 & 2</h1>
        <h4>Requirments:</h4> <p>Communicate wirelessly the temperature sensed by a sunspot to a base station, calibrate the temperature, and display it graphicaly.</p>
        <h4>Our Solution:</h4> <p>Sunspots Report Temperature every minute in response to being turned on. Base station using an Http post sends data to a MySql database where it is stored. Using The Google chart Api and Javascript we display the data graphicaly including a live view.</p
        <div id='carbonads-container'><div class='carbonad'><div id='azcarbon'></div><script>var z = document.createElement('script'); z.async = true; z.src = 'http://engine.carbonads.com/z/32341/azcarbon_2_1_0_HORIZ'; var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(z, s);</script></div></div>

      </div>
    </div>
    </div>

<div class='container' style='background-color:#FEFEFE' >
<h2 >Devices</h2>
<div class='span12'>
<form>
<table class='table table-striped' style='width:500px'>
<thead>
    <tr>
    <th>Location Id</th>
    <th>Device Id</th>
    <th>Calibration</th>
    </tr>
    </thead>
    <tbody>
  <tr>
";

   require_once 'ec544_api/include/Main_Functions.php';

    // include db handler
     $main = new Main_Functions();

   $mysqli = $main->createCon();
    $count = 0;
    if ($result = $mysqli->query("SELECT * FROM device_calibration")) {
        while($row = $result->fetch_array())
 		 {
      echo "<td>".$count."</td>";
      $count++;
			  echo "<td>".$row['device_uid'] . "</td><td> " . $row['calibration_index']."</td>";
 			  echo "</tr>
        <tr>";
  		 }
 
    }

     $numberOfDevices = $count;
echo"
</form>
</table>
<p></p>
<h2>Live Data</h2>
<h5 id='LiveStatus' style='margin-bottom:50px;'>Temperature is in Fahrenheit | Demo Currently Running </h5>
<div id='chart_div' style='width: 400px; height: 120px;'></div>

<h2 style='margin-top:100px;'>Historical Data</h2>
 <div style='display:inline-block;margin-top:80px;'> <table style='width:100%''>
  
";

$count2 =0;
$firsttime =0;
if ($result = $mysqli->query("SELECT * FROM device_calibration")) {
        while($row = $result->fetch_array())
     {
       if($count2 %3 ==0 and $firsttime !=0)
      {
        echo "<tr>";
      }

      if($firsttime ==0)
      {
        echo "<tr>";
         echo "<td class='dialer active'><a id='divAVG' href=''>Average</a></td>"; 
        $firsttime=1;
      }
     
        echo "<td class='dialer'><a id='div".$count."' href=''>".$count2."</a></td>";
        $count2++;


      if($count2 %3 == 2)
      {
          echo "</tr>";
      }
       
       }
     }

echo"
</table>
 </div>
 <div id='line_chart_div'; style='width: 900px; height: 500px;margin-top:80px;'></div>
</div>
</div>
<body>
</body
</html>
   <script type='text/javascript'>
      google.load('visualization', '1', {packages:['gauge']});
      google.setOnLoadCallback(drawChart);
      function drawChart() {


        var data = google.visualization.arrayToDataTable([
          ['Label', 'Value'],
          ['Room Avg', 70],
          ";

          $count3 =1;
           
   if ($result = $mysqli->query("SELECT * FROM device_calibration")) {
   
    while($row = $result->fetch_array())
     {
      
      if(  $count3 == $result->num_rows)
      {
       echo"['".($count3-1)."', 70]";
        $count3++;
      }
      else
      {
         echo"['".($count3-1)."', 70],";
        $count3++;
      }
     }
   }


echo"
        ]);

        var options = {
          width: 1000, height: 320,
          redFrom: 90, redTo: 100,
          yellowFrom:75, yellowTo: 90,
          minorTicks: 5
        };

        var chart = new google.visualization.Gauge(document.getElementById('chart_div'));
        var demo = 0;
        chart.draw(data, options);

        var temp  =0;
        var inComming[];
        var avger =0;
        var dataer =0;

      ";

      //work for checking if data has been updated in past minute

    if ($result = $mysqli->query("SELECT * FROM temperatures WHERE date_created BETWEEN DATE_SUB(NOW(), INTERVAL 1 MINUTE) AND NOW();")) {
   
    while($row = $result->fetch_array())
     {
       
      if( $result->num_rows >0)
      {
         echo "demo = 1;";
      }
      else
      {
          echo "demo = 0;";
      }
     }
   }
     

      echo"
          
        if(demo ==0)
        {
            setInterval(function() {
              avger =0;
          temp = 0;
          // Demo code
          //Display the fact that a demo is running
        
          $( '#LiveStatus').replaceWith('<h5 id='LiveStatus' style='margin-bottom:50px;'>Temperature is in Fahrenheit | Demo Currently Running </h5>' );

        
          ";

       for($i=0;$i<$numberOfDevices;$i++)
       {

       echo" temp = 40 + Math.round(60 * Math.random());
            avger = avger+temp;
          data.setValue(".($i+1).", 1,temp);
          chart.draw(data, options);
       ";
       }
       echo" dataer = avger/".$numberOfDevices.";
          data.setValue(0, 1, dataer);
          chart.draw(data, options);
        
         }, 5000);
      }else
      {


        //real database code display the fact
          $( '#LiveStatus').replaceWith('<h5 id='LiveStatus' style='margin-bottom:50px;'>Temperature is in Fahrenheit | Currently Live </h5>' );
         // getSchoolData
          /*
        ";




       for($i=0;$i<$numberOfDevices;$i++)
       {

       echo" temp = inComming[".($i+1)."];
            avger = avger+temp;
          data.setValue(".($i+1).", 1,temp);
          chart.draw(data, options);
       ";
       }
       echo" dataer = avger/".$numberOfDevices.";
          data.setValue(0, 1, dataer);
          chart.draw(data, options);";


        echo"
        */
      }
   
   


      }
    </script>
    <script type='text/javascript'>
      google.load('visualization', '1', {packages:['corechart']});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = google.visualization.arrayToDataTable([
          ['Hour', 'Sales', 'Expenses', 'vagina'],
          ['2004',  1000,      400, 400],
          ['2005',  1170,      460, 460],
          ['2006',  660,       1120, 1120],
          ['2007',  1030,      540,  540]
        ]);

        var options = {
          title: 'Temperature in Fahrenheit'
        };

        var chart = new google.visualization.LineChart(document.getElementById('line_chart_div'));

        chart.draw(data, options);
      }
    </script>
";

?>