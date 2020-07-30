#!/usr/bin/python

import time
import sys
import traceback
import json
import requests
import os.path
from datetime import datetime

ROOT_URL   = "https://www.bmw-connecteddrive.co.uk"
USER_AGENT = "MCVApp/1.5.2 (iPhone; iOS 9.1; Scale/2.00)"

USERNAME='' 
PASSWORD=''
AccessToken=""

def obtainCredentials():
        global AccessToken
        headers = {
            "Content-Type": "application/x-www-form-urlencoded",
            "User-Agent": USER_AGENT
        }

        data = {
            "client_id": "dbf0a542-ebd1-4ff0-a9a7-55172fbfce35",
            "redirect_uri": "https://www.bmw-connecteddrive.com/app/default/static/external-dispatch.html",
            "response_type": "token",
            "scope":"authenticate_user fupo",
            "username": USERNAME,
            "password": PASSWORD
        }

        r = requests.post("https://customer.bmwgroup.com/gcdm/oauth/authenticate", allow_redirects=False, data=data, headers=headers)
        #We expect a 302 reply (redirect)

        if (r.status_code==302):
            #We want the access_token, token_type and expires_in from the Location querystring parameters
            location=r.headers["Location"]

            if location.startswith( "https://www.bmw-connecteddrive.com/app/default/static/external-dispatch.html" ):
                d={}

                parts=location.split("&")
                for word in parts[1:]:
                    values=word.split("=")
                    d[values[0]]=values[1]

                access_token = parts[0].split("#")
                for word in access_token[1:]:
                    values=word.split("=")
                    d[values[0]]=values[1]

                AccessToken = d["access_token"] 
                TokenExpiry = time.time() + float(d["expires_in"])
                #saveCredentials()

            else:
                print("locationHeader=" +location)
                print("Location URL is different from expected")

        else:
            #Throw exception if API call failed
            r.raise_for_status()
            print("Obtained invalid response from authenticate API request")

        return

def call( path):
        """
        Call the API at the given path.
        Argument should be relative to the API base URL, e.g:
            print call('/user/vehicles/')
        """
        #if (time.time() > _TokenExpiry):
        #    obtainCredentials()

        headers = {"Authorization": "Bearer " + AccessToken,
                   "User-Agent":USER_AGENT}

        r = requests.get(ROOT_URL + path,  headers=headers)

        #Raise exception if problem with request
        r.raise_for_status()
        return r.json()

# Initialization
obtainCredentials()

vehicles = call('/api/me/vehicles/v2?all=true')
mycar=vehicles[0]

vin = mycar["vin"]
modelName = mycar["modelName"]

dynamic = call('/api/vehicle/dynamic/v1/'+vin+"?offset=0")
attributes = dynamic["attributesMap"]

percent =  attributes["chargingLevelHv"]
range =  attributes["beRemainingRangeElectric"]

print ("Battery: " + percent + "% ( " + range + " miles )" )

efficiency = call('/api/vehicle/efficiency/v1/'+vin)

lastTrip = efficiency["lastTripList"]
line = lastTrip[0]
if (line["name"] == "LASTTRIP_DELTA_KM" ):
   miles = line["lastTrip"] 

line = lastTrip[2]
if (line["name"] == "AVERAGE_ELECTRIC_CONSUMPTION" ):
   mpk = line["lastTrip"] 

print ("Last trip " + miles + " miles, efficiency: " + mpk + "mi/kWh")
