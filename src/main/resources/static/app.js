var app = angular.module("ATGToolsApp",[]);
app.controller("ATGToolsController",function($scope,$http){
	
	$scope.envStates = {};
	$scope.resultTiers = [];
	$scope.envs = [];
	$scope.configuration = { 
	"display":{"overall": false,"smsFT":false,"mockValues":false,"emailDummyMode":false},
	"recommendedStates": { "MockValues":"true","EmailDummyModeValues":"false","SmsFeatureToggleValues":"true"},
	"navigation": [
	{"view":"Summary","display":true,"function":function(){$scope.getOverallData();}},
	{"view":"SMS Feature Toggle","display":true,"function":function(){$scope.getTierEnvsAvailability('SmsFeatureToggleValues','SMSFTValue','SMS Feature Toggle States','/atgSMSFTValues',
	'Please note SMS Feature Toggle Enabled means there is a chance of SMS going out to real mobile numbers');}},
	{"view":"Mock Status","display":true,"function":function(){$scope.getTierEnvsAvailability('MockValues','mocks','Mock Status','/atgMockValues',
	'Please note Mocks are Enabled means there is a chance of data being mismatch with other systems');}},
	{"view":"Email Dummy Mode","display":true,"function":function(){$scope.getTierEnvsAvailability('EmailDummyModeValues','DummyModeValue','Email Dummy Mode values','/dummyMode',
	'Please note Email Dummy Mode Disabled means there is a chance of Email going out to real email addresses');}}
	],
	"reqdMocks": [{"mockKey":"mockPaymentGatewayEnabled"},{"mockKey":"mockVerifoneServiceEnabled"}],
	"hostAndPort": "http://lnxs0639.ghanp.kfplc.com:9043",
	"overallDataUri":"/allData",
	"loggingEnabled":true
	};
	
	$scope.display = $scope.configuration.display;
	$scope.recommendedStates = $scope.configuration.recommendedStates;
	$scope.navigation = $scope.configuration.navigation;
	$scope.reqdMocks = $scope.configuration.reqdMocks;
	$scope.hostAndPort = $scope.configuration.hostAndPort;
	//$scope.hostAndPort = "http://localhost:9020";
	$scope.overallDataUri = $scope.configuration.overallDataUri;
	$scope.note='';
	$scope.currentTab = '';
	$scope.loggingEnabled = $scope.configuration.loggingEnabled;
	
	$scope.resetALL = function(){
		
		$scope.display = {"Overall": false,"smsFT":false,"mockValues":false,"emailDummyMode":false}
		$scope.boxHeading = "";
		$scope.summary = [];
		$scope.note='';
		$scope.loading= false;
		$scope.errorInGettingData = '';
		$scope.note1 = "";
        $scope.note2 = "";
        $scope.note3 = "";
		
		 
	};
	
	$scope.getOverallData = function(){
		$scope.resetALL();
		$scope.loading= true;
		$scope.apiCallUrl = $scope.hostAndPort + $scope.overallDataUri;
		$http.get($scope.apiCallUrl)
			.then(function successCallback(response) {
				$scope.envStates = response.data;
				//$scope.envStates = $scope.tmpData();
				$scope.resultTiers = $scope.getUniqVals($scope.envStates.SmsFeatureToggleValues, "tier");
				
				$scope.envs = $scope.getUniqVals($scope.envStates.SmsFeatureToggleValues, "environment");
				
				
				
				//$scope.opcos = $scope.getUniqVals($scope.envStates, "opco");
				//$scope.smsFeatureToggleVals = angular.copy($scope.envStates.SmsFeatureToggleValues);
				$scope.summary = [];
				
				$scope.tmpArray = $scope.prepareSummary($scope.envStates.SmsFeatureToggleValues,"SMSFTValue","SmsFeatureToggleValues");
			
				if($scope.loggingEnabled) console.log($scope.tmpArray);
				$scope.summary.push($scope.tmpArray);

				$scope.tmpArray = $scope.prepareSummary($scope.envStates.EmailDummyModeValues,"DummyModeValue","EmailDummyModeValues");
			
				if($scope.loggingEnabled) console.log($scope.tmpArray);
				$scope.summary.push($scope.tmpArray);

				$scope.tmpArray = $scope.prepareSummary($scope.envStates.MockValues,"mocks","MockValues");
			
				if($scope.loggingEnabled) console.log($scope.tmpArray);
				$scope.summary.push($scope.tmpArray);
				if($scope.loggingEnabled) console.log($scope.summary);
				
				$scope.boxHeading = "Overall Summary";
				$scope.loading= false;
				$scope.display.overall = true;
				$scope.note = "Please note ";
				$scope.note1 = "SMS Feature Toggle Enabled means there is a chance of SMS going out to real mobile numbers.";
				$scope.note2 = "Mocks are Enabled means there is a chance of data being mismatch with other systems.";
                $scope.note3 = "Email Dummy Mode Disabled means there is a chance of Email going out to real email addresses.";

			}, function failureCallback(response){
				$scope.loading= false;
				$scope.errorInGettingData = 'Error occurred while fetching the data. Please try after sometime or contact support team';
			});
		
	};
	
	$scope.prepareSummary = function(resultJSON,key, key2){
		
		$scope.enabledData =[];
		if($scope.loggingEnabled) console.log(resultJSON);
		if(key !== "mocks")
		{
			for (var i=0,j=0; i<resultJSON.length; i++){
				if (resultJSON[i][key] === $scope.recommendedStates[key2]){
					$scope.enabledData[j] = angular.copy(resultJSON[i]);
					j++;
				}
			}
		}
		else if(key === "mocks"){
			for (var i=0,j=0; i<resultJSON.length; i++){
				/*if($scope.loggingEnabled) console.log(resultJSON);
				if($scope.loggingEnabled) console.log(key);
				if($scope.loggingEnabled) console.log(resultJSON[i][key]);*/
				for(var k=0; k<resultJSON[i][key].length;k++){
					if (resultJSON[i][key][k].mockValue === $scope.recommendedStates[key2]){
						$scope.enabledData[j] = angular.copy(resultJSON[i]);
						j++;
						break;
					}
				}
			}
		}
		if($scope.loggingEnabled) console.log($scope.enabledData);
			
		$scope.tmp2 = {};
		$scope.tmpArray = [];
		$scope.tmp2["key"] = angular.copy(key2);
		$scope.tmp2["mode"] = angular.copy(key2);
		
		for (var j=0; j<$scope.envs.length; j++){
			$scope.tmp = {};
			
			
			$scope.tmpKey = 'false';
			$scope.tmpEnv = angular.copy($scope.envs[j].environment);
			for (var k=0; k<$scope.enabledData.length; k++){
				//if($scope.loggingEnabled) console.log("In the smsEnabledData loop");
				//if($scope.tmpEnv === $scope.smsEnabledData[k].environment && $scope.resultTiers[i].tier === $scope.smsEnabledData[k].tier){
				if($scope.tmpEnv === $scope.enabledData[k].environment ){

					$scope.tmpKey = $scope.recommendedStates[key2];
					break;
				}
			}
			
			$scope.tmp.env = angular.copy($scope.tmpEnv);
			//$scope.tmp.tier = $scope.tmpTiers;
			
			$scope.tmp["value"] = angular.copy($scope.tmpKey);
			$scope.tmpArray.push($scope.tmp);
		
		}
		$scope.tmp2.summaryVals = angular.copy($scope.tmpArray);
		
		for (var i =0 ; i< $scope.resultTiers.length; i++){
			var envCount = 0
			for (var j =0 ; j< $scope.envs.length; j++){
				for (var k =0 ; k< resultJSON.length; k++){
					if($scope.envs[j].environment === resultJSON[k].environment && $scope.resultTiers[i].tier === resultJSON[k].tier){
						envCount++;
						break;
					}
						
				}
				
				$scope.resultTiers[i].count= angular.copy(envCount);
				
			}
			
		}
		return $scope.tmp2;
		
	}
	
	$scope.getTierEnvsAvailability = function(mode,key,heading,uri,note){
		
		$scope.resetALL();
		$scope.loading= true;
		
		$scope.personaData=[];
		
		$scope.apiCallUrl = $scope.hostAndPort + uri;
		if($scope.loggingEnabled) console.log($scope.apiCallUrl);
 		 $http.get($scope.apiCallUrl)
			.then(function successCallback(response) {
				$scope.envStates = response.data;
				//$scope.tmp = $scope.tmpData();
				//$scope.envStates = angular.copy($scope.tmp[mode]);
				if($scope.loggingEnabled) console.log($scope.envStates); 
				$scope.envs = $scope.getUniqVals($scope.envStates, "environment");
				
				
				$scope.persData=[];
				if($scope.loggingEnabled) console.log($scope.envs);
				if($scope.loggingEnabled) console.log($scope.personas);
			if(mode !=="MockValues"){
				$scope.personas = $scope.getUniqVals($scope.envStates, "persona");
				for (var i=0; i<$scope.personas.length; i++){
					$scope.persData=[];
					for (var j=0; j<$scope.envStates.length; j++){
						if($scope.personas[i].persona === $scope.envStates[j].persona){
							$scope.tmpJson={};
							
							$scope.tmpJson.value =  angular.copy($scope.envStates[j][key]);
							$scope.tmpJson.environment =  angular.copy($scope.envStates[j].environment);
							$scope.tmpJson.elink =  angular.copy($scope.envStates[j].Link);
							/*if($scope.loggingEnabled) console.log($scope.tmpJson);
							if($scope.loggingEnabled) console.log("i is: "+i+" j is: "+j);*/
							$scope.persData.push($scope.tmpJson);
						}
					} // of for (var j=0; j<$scope.envStates.length; j++)
					$scope.tmpJson2={};
					$scope.tmpJson2.mode = angular.copy(mode);
					$scope.tmpJson2.key = angular.copy($scope.personas[i].persona);
					$scope.tmpJson2.summaryVals = angular.copy($scope.persData);
					$scope.summary.push($scope.tmpJson2);
				} // of for (var i=0; i<$scope.personas.length; i++)
			}// of if(mode !=="MockValues")
			else if(mode ==="MockValues"){
				//var mockExt = '/nucleus//kf/commerce/mocks/MockConfiguration/?propertyName=';
				var mockExt = '?propertyName=';
				for (var i=0;i<$scope.reqdMocks.length; i++){
					
					var mockData = [];
					
					
					for (var j=0;j< $scope.envStates.length; j++){
						
						$scope.tmpJson={};
						
						if(Array.isArray($scope.envStates[j][key] )){
							var idx = $scope.envStates[j][key].findIndex(k => k.mockKey === $scope.reqdMocks[i].mockKey);
							$scope.tmpJson.value =  angular.copy($scope.envStates[j][key][idx]["mockValue"]);
							$scope.tmpJson.environment =  angular.copy($scope.envStates[j].environment);
							$scope.tmpJson.elink =  angular.copy($scope.envStates[j][key][idx]["mockURL"]);
							mockData.push($scope.tmpJson);
						
						}
						else if($scope.envStates[j][key] === 'ERROR'){
							$scope.tmpJson.value =  'ERROR';
							$scope.tmpJson.environment =  angular.copy($scope.envStates[j].environment);
							$scope.tmpJson.elink =  angular.copy($scope.envStates[j].Link + mockExt + $scope.reqdMocks[i].mockKey);
							mockData.push($scope.tmpJson);
						}
							
					}
					$scope.tmpJson2={};
					
					$scope.tmpJson2.mode = angular.copy(mode);
					$scope.tmpJson2.key = angular.copy($scope.reqdMocks[i].mockKey);
					$scope.tmpJson2.summaryVals = angular.copy(mockData);
					$scope.summary.push($scope.tmpJson2);
					
					
				}
				
				
			}
				
				
			if($scope.loggingEnabled) console.log($scope.summary);

			$scope.boxHeading = angular.copy(heading);
			$scope.display.overall = true;
			$scope.loading= false;
			$scope.note=note;

			},function errorCallback(response) {
				// called asynchronously if an error occurs
				// or server returns response with an error status.
				$scope.loading= false;
				$scope.errorInGettingData = 'Error occurred while fetching the data. Please try after sometime or contact support team';
				if($scope.loggingEnabled) console.log("Some thing wrong");
			 }); 
			
		
		

	};
	
	$scope.getUniqVals = function(collection, keyname) {
		// we define our output and keys array;
		var output = [],
		  keys = [];

		// we utilize angular's foreach function
		// this takes in our original collection and an iterator function
		angular.forEach(collection, function(item) {
		  // we check to see whether our object exists
		  var key = item[keyname];
		  // if it's not already part of our keys array
		  if (keys.indexOf(key) === -1) {
			// add it to our keys array
			keys.push(key);
			// push this item to our final output array
			output.push(item);
		  }
		});
		// return our array which should be devoid of
		// any duplicates
		return output;
	 };
	 
	$scope.tmpData = function(){
		 var tempData = {
				};
	 return tempData;
	};

});

app.filter('stateMeaning', function() {
  return function(x) {
	txt=""
	if(x=='false' ){
		txt = "Disabled";
	}
	if(x == 'true') {
		txt = "Enabled";
	}
	if(x == 'ERROR') {
		txt = "Not Accessible";
	}
	return txt;
	};
});