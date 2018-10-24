/**
 *  Rheem Econet Water Heater SmartThings Device Handler
 *
 *  Copyright 2018 Jaime Pirnie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Last Updated : 10/24/2018
 *
 *  Based on https://github.com/copy-ninja/SmartThings_RheemEcoNet
 *  Based on https://github.com/jjhuff/SmartThings_RheemEcoNet
 *  Based on https://github.com/kramttocs/ST-RheemEconet
 */

metadata {
	definition (name: "Rheem Econet Water Heater", namespace: "jpirnie", author: "Jaime Pirnie") {
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
        capability "Switch"
		capability "Thermostat Heating Setpoint"
        capability "Water Sensor"
        capability "Thermostat Operating State"
        
		
		command "heatLevelUp"
		command "heatLevelDown"
        command "eco"
        command "performance"
		command "enableVacation"
		command "disableVacation"
		command "getAlerts"
		command "updateDeviceData", ["string"]
        
        attribute "maxSetPoint", "number"
        attribute "minSetPoint", "number" 
	}


    tiles {

		standardTile("switch", "device.switch", decoration: "flat" ) {
       		state "on", label: 'On', action: "switch.off",
          		icon: "st.switches.switch.on", backgroundColor: "#79b821"
       		state"off", label: 'Off', action: "switch.on",
          		icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        
		standardTile("refresh", "device.switch", decoration: "flat") {
			state("default", action:"refresh.refresh", icon:"st.secondary.refresh")
		}
        
         multiAttributeTile(name:"summary", type: "thermostat", width: 6, height: 4, canChangeIcon: true) {
        	tileAttribute("device.heatingSetpoint", key: "PRIMARY_CONTROL") {
				attributeState("heatingSetpoint", label:'${currentValue}°', unit:"dF", defaultState: true)
			}
			tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "heatLevelUp")
				attributeState("VALUE_DOWN", action: "heatLevelDown")
			}
			tileAttribute("device.thermostatOperatingStateDisplay", key: "OPERATING_STATE") {
				attributeState("idle", label: 'idle', backgroundColor:"#51bec2")
				attributeState("heating", label: 'heating', backgroundColor:"#ff242b")	
                attributeState("off", label: 'off', backgroundColor:"#181344")			
                attributeState("default", label: 'idle', backgroundColor:"#51bec2", defaultState: true) 
			}
            tileAttribute ("device.water", key: "SECONDARY_CONTROL") {				
				attributeState "wet", 
					label:'Wet', 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#53a7c0"
                    attributeState "dry", 
					label:'Dry', 
					icon: "st.alarm.water.dry",
					backgroundColor:"#ffffff"			
			}
			
        }
        
        standardTile("mode", "mode", decoration: "flat") {
       		state "eco", label: 'Eco', action: "performance", icon: "st.Outdoor.outdoor19", backgroundColor: "#79b821"
       		state "performance", label: 'Perf', action: "eco", icon: "st.Transportation.transportation8", backgroundColor: "#ff8a8e"
		}
		
		standardTile("vacation", "vacation", decoration: "flat") {
       		state "on", label: 'Vacation', action: "disableVacation", icon: "st.Home.home18", backgroundColor: "#79b821"
       		state "off", label: 'Home', action: "enableVacation", icon: "st.Home.home2", backgroundColor: "#ff8a8e"
		}
        valueTile("alert", "alert", width: 6, height: 3) {
       		state "alert", label:'${currentValue}', action: "getAlerts"
		}
        valueTile("minSetPoint", "device.minSetPoint", inactiveLabel: false) {
			state("minSetPoint", label:'Min\n${currentValue}°',
				backgroundColors:[
					[value: 90,  color: "#f49b88"],
					[value: 100, color: "#f28770"],
					[value: 110, color: "#f07358"],
					[value: 120, color: "#ee5f40"],
					[value: 130, color: "#ec4b28"],
					[value: 140, color: "#ea3811"]					
				]
			)
		}
        valueTile("maxSetPoint","maxSetPoint", inactiveLabel: false) {
			state("maxSetPoint", label:'Max\n${currentValue}°',
				backgroundColors:[
					[value: 90,  color: "#f49b88"],
					[value: 100, color: "#f28770"],
					[value: 110, color: "#f07358"],
					[value: 120, color: "#ee5f40"],
					[value: 130, color: "#ec4b28"],
					[value: 140, color: "#ea3811"]					
				]
			)
		}
        
		main "summary"
		details(["summary", "switch", "mode", "vacation", "minSetPoint", "maxSetPoint", "alert", "refresh"])
	}
}


def parse(String description) { }

def installed() {
	parent.refresh();
}

def refresh() {
	parent.refresh();
	parent.getAlerts();
}

def getAlerts(){	
    parent.getAlerts();
}

def updateAlertData(data){
    if(data == null || data.empty)
    {
    	sendEvent(name: "alert", value: "No Current Alerts");
        sendEvent(name: "water", value: "dry")
    }
    else
    {
    	def alerts = ""
    	for(alertItem in data){
         alerts += alertItem.code + " -- " + alertItem.description + "\n\n"
		 if(alertItem.code == "A102"){
			sendEvent(name: "water", value: "wet")
		 }
        }
    	sendEvent(name: "alert", value: alerts);
    }
}

def eco() {
	parent.setDeviceMode(this.device, "Energy Saver")
    sendEvent(name: "mode", value: "eco")
	parent.refresh();
}

def performance() {
	parent.setDeviceMode(this.device, "Performance")
    sendEvent(name: "mode", value: "performance")
	parent.refresh();
}

def enableVacation() {
	log.debug "Enabling Vacation"
	parent.setDeviceVacation(this.device, true)
    sendEvent(name: "vacation", value: "on")
	parent.refresh();
}

def disableVacation() {
	log.debug "Disabling Vacation"
	parent.setDeviceVacation(this.device, false)
    sendEvent(name: "vacation", value: "off")
	parent.refresh();
}

def on() {
   	parent.setDeviceEnabled(this.device, true)
    sendEvent(name: "switch", value: "off")
    parent.refresh();
}

def off() {
   	parent.setDeviceEnabled(this.device, false)
    sendEvent(name: "switch", value: "off")
    parent.refresh();
}

def setHeatingSetpoint(Number setPoint) {
	sendEvent(name: "heatingSetpoint", value: setPoint, unit: "F")
	parent.setDeviceSetPoint(this.device, setPoint)
}

def heatLevelUp() { 
	def setPoint = device.currentValue("heatingSetpoint")
    setPoint = setPoint + 1   
    if(setPoint <= device.currentValue("maxSetPoint") )
    {
        setHeatingSetpoint(setPoint)
    }
    else
    {
        sendEvent(name: "heatingSetpoint", value: setPoint-1, unit: "F", isStateChange: true, descriptionText:"Max Set Point of " + device.currentValue("maxSetPoint") + " has been reached.")
    }
}	

def heatLevelDown() { 
	def setPoint = device.currentValue("heatingSetpoint") 
    setPoint = setPoint - 1
    if(setPoint >= device.currentValue("minSetPoint") )
    {
        setHeatingSetpoint(setPoint)
    }
    else
    {
        sendEvent(name: "heatingSetpoint", value: setPoint+1, unit: "F", isStateChange: true, descriptionText:"Min Set Point of " + device.currentValue("minSetPoint") + " has been reached.")
    }
}

def updateDeviceData(data) {
	sendEvent(name: "heatingSetpoint", value: data.setPoint)    
    sendEvent(name: "thermostatOperatingStateDisplay",  value: (data.isEnabled ? (data.inUse ? "heating" : "idle") : "off"))
    sendEvent(name: "switch", value: data.isEnabled ? "on" : "off")
    sendEvent(name: "minSetPoint", value: data.minSetPoint)    
    sendEvent(name: "maxSetPoint", value: data.maxSetPoint)
	sendEvent(name: "vacation", value: data.isOnVacation ? "on" : "off")
	sendEvent(name: "mode", value: data.mode == "Energy Saver" ? "eco" : "performance")    
}