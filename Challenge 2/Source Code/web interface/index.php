 <?php

echo"
<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>
<html xmlns='http://www.w3.org/1999/xhtml'>
<head>
<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />
<title>EC544 Thermostat</title>
 <!-- Bootstrap core CSS -->
  <link href='css/bootstrap.css' rel='stylesheet'>
<link rel='stylesheet' type='text/css' href='css/jquery.datetimepicker.css'/>
 
   <script src='//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js'></script>
  <script type='text/javascript'src='js/jquery.form.min.js'></script>
  <script type='text/javascript' src='js/bootstrap.js'></script>
  <script type='text/javascript' src='js/mapAPI.js'></script>
  <script type='text/javascript' src='js/jquery.datetimepicker.js'></script>

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
          <a href='challange1&2.php'>Challange 1 & 2</a>
        </li>
      </ul>
      <ul class='nav navbar-nav navbar-right'>
        <li><a href='http://expo.getbootstrap.com' onclick='ga('send', 'event', 'Navbar', 'Community links', 'Expo');'>Sparakis</a></li>
      </ul>
    </nav>

<div class='bs-docs-header' id='content' style='background-color: #6f5499; background-image: -webkit-gradient(linear,left top,left bottom,from(#563d7c),to(#6f5499));'>
      <div class='container'>
        <h1>Challange 1 & 2</h1>
        <h4>Requirments:</h4> <p>Communicate wirelessly the temperature sensed by a sunspot to a base station, calibrate the temperature, and display it graphicaly.</p>
        <h4>Our Solution:</h4> <p>Sunspots Report Temperature every minute in response to being turned on. Base station using an Http post sends data to a MySql database where it is stored. Using The Google chart Api and Javascript we display the data graphicaly including a live view.</p>

      </div>
    </div>
    </div>

<div class='container' style='background-color:#FEFEFE' >
<h2 >Devices</h2>
<div class='span12'>
 <form id='liveDataForm' action='getLiveData.php' method='post' >  
       </form>
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

   $device_id= $arrayName = array( );
   $mysqli = $main->createCon();
    $count = 0;
    if ($result = $mysqli->query("SELECT * FROM device_calibration")) {
        while($row = $result->fetch_array())
 		 {
      echo "<td>".$count."</td>";
      
         array_push($device_id, $row['device_uid']);
			  echo "<td>".$row['device_uid'] . "</td><td> " . $row['calibration_index']."</td>";
 			  echo "</tr>
        <tr>";
        $count++;
  		 }
 
    }

     $numberOfDevices = $count;

echo"
</form>
</table>
<p></p>
<h2>Live Data</h2>
<h5 id='LiveStatus' style='margin-bottom:50px;'>Temperature Is In Fahrenheit | Demo Currently Running </h5>
<div id='chart_div' style='width: 400px; height: 120px;'></div>

<h2 style='margin-top:100px;'>Historical Data</h2>

 <h5 style=''>Select a date-time range to generate a graph for data</h5>
 <h5 style='margin-bottom:50px;'>For Demo Generate With Default Values</h5>

<form id='hdForm' action='graphGenerator.php' method='post' target='_blank'>
<div style='display:inline-block'>
 <h6>From</h6>
  <input type='text' value='' name='datetimepicker' id='datetimepicker'/><br><br>
  </div>
  <div style='display:inline-block;margin-left:50px;'>
 <h6>To</h6>
  <input type='text' value='' id='datetimepicker2' name='datetimepicker2'/><br><br>
  </div>
  <p></p>
   <button type='submit'style='margin-bottom:100px;' class='btn btn-primary btn-lg' >Generate Graph</button> 
   <!-- <input type='submit' value='Submit'> -->
   </form>
";

echo"
</table>
 </div>
</div>
</div>
<body>
</body
</html>




   <script>
   //Submit  historical data form
   function submitHDFORM()
   {
    console.log('working');
     $('#hdForm').submit(function() {
       console.log('working2');
     //$('#hdForm').attr('target', '_blank');
return true;
});
   }

   //FUNCTIONS
function getLiveData()
      {

        $('#liveDataForm').ajaxSubmit({success:showResults, fail:showFail});
      }


      function showResults(data)
      {

                //alert(data);
        var json= JSON.parse(data);
         


        if(json['error'] == 1)
        {
           // System is no longer live
          alert('System is not live. Switching back to Demo Mode.');
          location.reload();

        }
        else
        {

           google.load('visualization', '1', {packages:['gauge']});
           google.setOnLoadCallback(drawChart);

           var chart = new google.visualization.Gauge(document.getElementById('chart_div'));
           var options = {
              width: 1000, height: 320,
             redFrom: 90, redTo: 100,
             yellowFrom:85, yellowTo: 90,
             minorTicks: 5
           };

          var data = google.visualization.arrayToDataTable([
          ['Label', 'Value'],
          ['Room Avg', 0],
          ";

          $count3 =1;
           
   if ($result = $mysqli->query("SELECT * FROM device_calibration")) {
   
    while($row = $result->fetch_array())
     {
      
      if(  $count3 == $result->num_rows)
      {
       echo"['".($count3-1)."', 0]";
        $count3++;
      }
      else
      {
         echo"['".($count3-1)."', 0],";
        $count3++;
      }
     }
   }


echo"
        ]);

        var temp =0;
        var avger=0;
        console.log(avger);
";
        
        for($i=0;$i<$numberOfDevices;$i++)
       {

echo"
         temp = parseInt(json['".$device_id[$i]."']);
            avger = avger+temp;
            console.log(avger);
          data.setValue(".($i+1).", 1,temp);
         // chart.draw(data, options);";
       
       }
       echo"
       console.log('final');
       console.log(avger);
         dataer = avger/".$numberOfDevices.";
          data.setValue(0, 1, dataer);
          chart.draw(data, options);


              }
      }


      function showFail()
      {
        alert('Switching to Demo Mode');
         location.reload();
      }

    



    /*** FUNCTIONS END ***/


/*
*/
/******Time picker code *****/
/*
 */

  $('#datetimepicker').datetimepicker({
dayOfWeekStart : 1,
lang:'en',
startDate:  '2014/09/08'
});

$('#datetimepicker').datetimepicker({value:'2014-09-25 01:14:56',step:10});


  $('#datetimepicker2').datetimepicker({
dayOfWeekStart : 1,
lang:'en',
startDate:  '2014/09/08'
});

$('#datetimepicker2').datetimepicker({value:'2014-09-25 01:30:56',step:10});






//
// END OF TIME PICKER CODE
//
//

   //
   //LIVE GAUGE CODE BELOW
   //
   //

      google.load('visualization', '1', {packages:['gauge']});
      google.setOnLoadCallback(drawChart);
      function drawChart() {


        var data = google.visualization.arrayToDataTable([
          ['Label', 'Value'],
          ['Room Avg', 0],
          ";

          $count3 =1;
           
   if ($result = $mysqli->query("SELECT * FROM device_calibration")) {
   
    while($row = $result->fetch_array())
     {
      
      if(  $count3 == $result->num_rows)
      {
       echo"['".($count3-1)."', 0]";
        $count3++;
      }
      else
      {
         echo"['".($count3-1)."', 0],";
        $count3++;
      }
     }
   }


echo"
        ]);

        var options = {
          width: 1000, height: 320,
          redFrom: 90, redTo: 100,
          yellowFrom:85, yellowTo: 90,
          minorTicks: 5
        };

        var chart = new google.visualization.Gauge(document.getElementById('chart_div'));
        var demo = 0;
        chart.draw(data, options);";

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
        var temp  =0;
        var avger =0;
        var dataer =0;
        if(demo ==0)
        {
          // Demo code
          //Display the fact that a demo is running
          $('#LiveStatus').text('Temperature is in Fahrenheit | Demo Currently Running' );

       ";

 echo" setInterval(function() {
          
          avger =0;
          temp = 0;
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
        var inComming;
          $('#LiveStatus').text('Temperature is in Fahrenheit | Live Data' );

        setInterval(function() {
         getLiveData();

        }, 5000);

      }


      }


    
    </script>

";

?>