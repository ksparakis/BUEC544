<?php
 error_reporting(E_ALL);
 ini_set('display_errors', 1);
 

require_once 'ec544_api/include/Main_Functions.php';


 
    // include db handler
     $main = new Main_Functions();
    

if(isset($_POST['datetimepicker'])&&isset($_POST['datetimepicker2'])) {
   

     $mysqli = $main->createCon();
    $time1 = $_POST['datetimepicker'];
    //$time1 = substr($time1, 0, -3);
	  $time2 = $_POST['datetimepicker2'];
    //$time2 = substr($time2, 0, -3);
    /*
    echo $time1.'</br>';
    echo $time2;
    */
   
    $count = 0;
    $deviceArray = array( );
   if ($result = $mysqli->query("SELECT * FROM device_calibration")) {
        while($row = $result->fetch_array())
     {

     
         array_push($deviceArray, $row['device_uid']);
         $count++;
      }
 
    }
    
       $numberOfDevices = $count;

   
   echo"
   <!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>
<html xmlns='http://www.w3.org/1999/xhtml'>
<head>
<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />
<title>EC 544 Thermostat Graph</title>
<link type='text/css' rel='stylesheet' href='css/rickshaw.min.css'>
<script src='https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js'></script>
<script src='https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.15/jquery-ui.min.js'></script>
<link type='text/css' rel='stylesheet' href='http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css'>
<script src='https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.15/jquery-ui.min.js'></script>
<script src='js/d3.min.js'></script>
<script src='js/d3.layout.min.js'></script>
<script src='js/rickshaw.js'></script>
<link href='css/bootstrap.css' rel='stylesheet'>
<script src='js/Rickshaw.Graph.RangeSlider.js'></script>
<script src='js/Rickshaw.Graph.RangeSlider.Preview.js'></script>



<style>
#chart_container {
        display: inline-block;
        font-family: Arial, Helvetica, sans-serif;
}
#chart {
        float: left;
}
#legend {
        float: left;
        margin-left: 15px;
}
#offset_form {
        float: left;
        margin: 2em 0 0 15px;
        font-size: 13px;
}
#y_axis {
        float: left;
        width: 40px;
}

a {color:#ffffff;font-weight: bold;}
 a:hover { color:#cdbfe3;font-weight: bold;}

</style>

<nav class='collapse navbar-collapse bs-navbar-collapse' role='navigation' style='background-color:#6f5499; margin-bottom:50px;'>
<a href='../' class='navbar-brand'>EC544 Team 1</a>
      <ul class='nav navbar-nav'>
        <li>
          <a href='challange1&2.php'>Challange 1 & 2</a>
        </li>
      </ul>
      <ul class='nav navbar-nav navbar-right'>
        <li><a href='http://www.sparakis.com' onclick='ga('send', 'event', 'Navbar', 'Community links', 'Expo');'>Sparakis</a></li>
      </ul>
    </nav>


<h3 style='text-align:center;margin-bottom:45px;'>Temperature Graph From: '".$time1."'' To: '".$time2."'' In Fahrenheit</h3>


<div id='chart_container' style='margin-left:80px'>
       <div id='y_axis'></div>
        <div id='chart'></div>
        <div id='legend' class='rickshaw_legend' style='height: 123px;'> </div>
        
        <form id='offset_form' class='toggler'>    
        </form>
        <div id='preview' style='width: 1080px; margin-top:470px;' class='ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all'></div>

</div>

<script>
var palette = new Rickshaw.Color.Palette();

var graph = new Rickshaw.Graph( {
        element: document.querySelector('#chart'),
        width: 900,
        height: 460,
        min:0,
        max:110,
        renderer: 'line',
        series: [";


    for($i=0; $i<$numberOfDevices; $i++)
    {
      echo" { ";

      echo"name: '".$deviceArray[$i]."',";
      echo"data: [";
      $counter = 0;
      if ($result = $mysqli->query("SELECT * FROM `temperatures` WHERE `device_id` = '{$deviceArray[$i]}' AND `date_created` BETWEEN '{$time1}' AND '{$time2}' ")) {
        while($row = $result->fetch_array())
     {
        echo"{ x: ";
        echo strtotime($row['date_created']).", y:".$row['temperature']."}";
        if( $counter+1 == $result->num_rows)
        {

        }else{
          echo",";
        }
        $counter++;
       
      }

 
    }

      echo" ],";
      echo"color: palette.color()";
    if(($i+1) == $numberOfDevices)
    {
     echo" } ";
    }
    else
    {
      echo" }, ";
    }
   }





echo"
        ]
} );

 


var hoverDetail = new Rickshaw.Graph.HoverDetail( {
  graph: graph
} );

graph.render();

var xAxis = new Rickshaw.Graph.Axis.Time({
    graph: graph
});

xAxis.render();

var yAxis = new Rickshaw.Graph.Axis.Y({
    graph: graph,
    tickFormat: Rickshaw.Fixtures.Number.formatKMBT
});

yAxis.render();

var legend = new Rickshaw.Graph.Legend( {
        element: document.querySelector('#legend'),
        graph: graph
} );

var shelving = new Rickshaw.Graph.Behavior.Series.Toggle({
    graph: graph,
    legend: legend
});


var smoother = new Rickshaw.Graph.Smoother( {
  graph: graph,
  element: document.querySelector('#smoother')
} );

var highlighter = new Rickshaw.Graph.Behavior.Series.Highlight({
    graph: graph,
    legend: legend
});

var order = new Rickshaw.Graph.Behavior.Series.Order({
    graph: graph,
    legend: legend
});

var preview = new Rickshaw.Graph.RangeSlider( {
  graph: graph,
  element: document.getElementById('preview'),
} );

</script>
  ";
}
else    // CODE FOR WHEN POST DOESNT HAPPEN , CLOSE PAGE
{
echo"
   <!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>
<html xmlns='http://www.w3.org/1999/xhtml'>
<head>
<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />
<title>EC 544 Thermostat Graph</title>
<link type='text/css' rel='stylesheet' href='css/rickshaw.min.css'>
<script src='js/d3.min.js'></script>
<script src='js/d3.layout.min.js'></script>
<script src='js/rickshaw.js'></script>
<link href='css/bootstrap.css' rel='stylesheet'>



<style>
#chart_container {
        display: inline-block;
        font-family: Arial, Helvetica, sans-serif;
}
#chart {
        float: left;
}
#legend {
        float: left;
        margin-left: 15px;
}
#offset_form {
        float: left;
        margin: 2em 0 0 15px;
        font-size: 13px;
}
#y_axis {
        float: left;
        width: 40px;
}

a {color:#ffffff;font-weight: bold;}
 a:hover { color:#cdbfe3;font-weight: bold;}

</style>

<nav class='collapse navbar-collapse bs-navbar-collapse' role='navigation' style='background-color:#6f5499; margin-bottom:150px;'>
<a href='../' class='navbar-brand'>EC544 Team 1</a>
      <ul class='nav navbar-nav'>
        <li>
          <a href='challange1&2.php'>Challange 1 & 2</a>
        </li>
      </ul>
      <ul class='nav navbar-nav navbar-right'>
        <li><a href='http://www.sparakis.com' onclick='ga('send', 'event', 'Navbar', 'Community links', 'Expo');'>Sparakis</a></li>
      </ul>
    </nav>

    <h1 style='text-align:center'>Returning to Challange 1 & 2...</h1>
<script>
  
  setTimeout(function(){
    //do what you need here
    close();
}, 500);
</script>
";
}
?>
  










