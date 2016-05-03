  function getLiveData()
      {

        $('#liveDataForm').ajaxSubmit({success:showResults, fail:showFail});
      }


      function showResults(data)
      {
        var json = JSON.parse(data);
         

        if(json['error'] == 1)
        {
          
          alert("System is not live. Switching back to Demo Mode.");
          location.reload();

        }else{
        

        for($i=0;$i<$numberOfDevices;$i++)
       {

         temp = data['".$device_id[$i]."'];
            avger = avger+temp;
          data.setValue(".($i+1).", 1,temp);
          chart.draw(data, options);
       
       }
         dataer = avger/".$numberOfDevices.";
          data.setValue(0, 1, dataer);
          chart.draw(data, options);


        alert(data);
      }
      }


      function showFail()
      {
        alert('Switching to Demo Mode');
         location.reload();
      }

    