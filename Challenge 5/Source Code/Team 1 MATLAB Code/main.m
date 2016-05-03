%%Wenyang Zhang

%clear all; close all; clc;

function main

close all;clear all; clc;
%% Timer
t = timer('TimerFcn', 'stat=false; disp(''Timer!'')',...
    'StartDelay',1000);
%%
x = 0; y = 0;
% Scenario setup
BeaconID = ['358F';'7DF3'; '45BB'; '80F5'; '78FB'; '79B0'];
boundaryX = 62;
boundaryY = 30;
%Beacon = [x,y]
Beacon = [0,6; 0,25; 30,0; boundaryY,30; boundaryX,3; boundaryX,23];
scenariofigure = figure('Name','Scenario','NumberTitle','off','units','pixels','Position',[500 500 1000 1000]);
axis([-2 boundaryX+2 -2 boundaryY+2]);
hold on;
for i=1:6
    plot(Beacon(i,1),Beacon(i,2),'bo');
    text(Beacon(i,1)-1,Beacon(i,2)-2,BeaconID(i,:));
    hold on;
end
spotFig = plot(x,y,'r*');

spotLabel = text(x-1,y-3,'Beacon');

%% Read file
fileName = './SpotDistance.txt';
fileID = fopen(fileName);
if (fileID == -1)
    disp('ERROR: Cannot open file');
    return
end

start(t);
stat=true;
while(stat==true)
    [x,y]=mainFunction(fileID); %Read data from txt file
    httpPost(x,y);
    pause(5) %transmit inveral in secs
end
% Do Triliteration



%% Output to the Server



delete(t);
close('all');

%% Support functions
    function httpPost (x,y)
        x= round(x);
        y= round(y);
        % for later to send device ID
        kop = strcat('http://54.205.115.111/ec544/challange5/uploadData.php?','tag=', 'report_distance', '&x=', num2str(x) , '&y=', num2str(y), '&device_id=','765E' );
        %  str = urlread('http://54.205.115.111/ec544/challange5/uploadData.php','Post',{'tag', 'report_distance', 'x', Spot.x , 'y', Spot.y, 'rssi', distance, 'device_id','765E' });
        str = urlread(kop);
        
    end


%Function for data processing and triliteration
    function [x,y]= mainFunction(fileID)
        [status, data] = freadSunSPOTFile(fileID);
        switch (status)
            case 'nonewdata'
                disp('No new data');
                return
                
            case 'newData'
                disp('Received new data');
        end
        BeaconDisCell = cell(6,1);
        BeaconNumm = zeros(6,1);
        BeaconN=length(unique(data{2}));
        if (BeaconN <3)
            disp('ERROR: Not enough beacon data');
            return
        elseif (BeaconN>6)
            disp('ERROR: Receive data from  more than 6 SPOTs');
            return
        end
        B=zeros(6,3);
        B(:,1:2)=Beacon;% get the address of the 6 beacons
        for i = 1:length(data{1,1})
            if strcmp(BeaconID(1,:),data{2}{i})
                BeaconDisCell{1} = vertcat(BeaconDisCell{1},data{1,3}(i));
                BeaconNumm(1)=1;
            end
            if strcmp(BeaconID(2,:),data{2}{i})
                BeaconDisCell{2} = vertcat(BeaconDisCell{2},data{1,3}(i));
                BeaconNumm(2)=1;
            end
            if strcmp(BeaconID(3,:),data{2}{i})
                BeaconDisCell{3} = vertcat(BeaconDisCell{3},data{1,3}(i));
                BeaconNumm(3)=1;
            end
            if strcmp(BeaconID(4,:),data{2}{i})
                BeaconDisCell{4} = vertcat(BeaconDisCell{4},data{1,3}(i));
                BeaconNumm(4)=1;
            end
            if strcmp(BeaconID(5,:),data{2}{i})
                BeaconDisCell{5} = vertcat(BeaconDisCell{5},data{1,3}(i));
                BeaconNumm(5)=1;
            end
            if strcmp(BeaconID(6,:),data{2}{i})
                BeaconDisCell{6} = vertcat(BeaconDisCell{6},data{1,3}(i));
                BeaconNumm(6)=1;
            end
            
        end
        
        
        
        B(:,3) = cellfun(@mean, BeaconDisCell); %Get the mean if we have more than 1 distance values from one spot
        
        for i = 1:6
            if (BeaconNumm(i)==0)
                B(i,:) = []; %Delete non-used SPOT info
            end
        end
        
        [x, y]=trilateration(B,BeaconN);
        
        
        disp([x y]);
        if (x<0)
            x = 0; %This should not be exceuted
            disp('X is smaller than 0');
        elseif (x>boundaryX)
            x = 62;
            disp('X is bigger than 62');
        end
        if (y<0)
            y = 0;
            disp('Y is smaller than 0');
        elseif (y>boundaryY)
            y = 30;
            disp('Y is bigger than 30');
        end
        
        delete(spotFig);
        delete(spotLabel);
        hold on;
        spotFig = plot(x,y,'r*');
        hold on;
        spotLabel = text(x-1,y-3,'Beacon');
        
    end %end of function updataData(varargin)


% Function for extracting files from txt file
    function [status, data] = freadSunSPOTFile(fileID)
        data = textscan(fileID, '%s %s %f'); %Read formatted data e.g.[ReceiverID BeaconID 36.39] from text file of string
        %data{1}{i} = Receiver ID
        %data{2}{i} = Beacon ID
        %data{3}{i} = distance
        if (isempty(data{1})) %i.e. when there is no data
            status = 'nonewdata';
            data = [];
            return
        end
        status = 'newData';
    end


end




