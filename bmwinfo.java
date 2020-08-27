// boilerplate android/java code for bmw connected

String accesstoken;

    String login() {
        String baseurl = "https://www.bmw-connecteddrive.co.uk";
        String authurl = "https://customer.bmwgroup.com/gcdm/oauth/authenticate";
        String addr = authurl;

        URL url = null;
        try {
            url = new URL(addr);

            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

            httpCon.addRequestProperty("Content-Type","application/x-www-form-urlencoded");
            httpCon.setRequestProperty("User-Agent","MCVApp/1.5.2 (iPhone; iOS 9.1; Scale/2.00)");
            httpCon.setDoOutput(true);
            httpCon.connect();

            String POST_PARAMS = "client_id=dbf0a542-ebd1-4ff0-a9a7-55172fbfce35" +
                    "&redirect_uri=" + URLEncoder.encode("https://www.bmw-connecteddrive.com/app/default/static/external-dispatch.html","UTF-8" ) +
                    "&username=" + URLEncoder.encode("my@email.com","UTF-8" ) +
                    "&response_type=token&state=FOOBAR" +
                    "&scope=" + URLEncoder.encode("authenticate_user fupo","UTF-8" ) +
                    "&password=" + URLEncoder.encode("mypassword","UTF-8" );

            OutputStream os = httpCon.getOutputStream();
            os.write(POST_PARAMS.getBytes());
            os.flush();
            os.close();

            try (InputStream stream = httpCon.getInputStream()) {
            } catch (Exception e){
                //horrible hack to get access token from url
                String returnstring = e.toString();
                //Log.d("bmwinfo",returnstring);
                int start = returnstring.indexOf("access_token=");
                if (start > 0) {
                    returnstring = returnstring.substring(start+13);
                    int end = returnstring.indexOf("&");
                    accesstoken=returnstring.substring(0,end);
                }
            }

        } catch (Exception e) {
            Log.d("bmwinfo",e.toString());
            //e.printStackTrace();
        }
        return(accesstoken);
    }

    String callApi(String path) {
        String baseurl = "https://www.bmw-connecteddrive.co.uk";
        String addr = baseurl + path;
        String json = "";
        URL url = null;
        try {
            url = new URL(addr);

            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

            httpCon.setRequestProperty("Authorization", "Bearer " + accesstoken);
            httpCon.setRequestProperty("User-Agent", "MCVApp/1.5.2 (iPhone; iOS 9.1; Scale/2.00)");
            httpCon.connect();

            BufferedReader in = new BufferedReader( new InputStreamReader(httpCon.getInputStream()));

            String inputLine;
            StringBuffer sb = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.d("bmwinfo",e.toString());
        }
        return(json);
    }
    String getField(String json, String tag){
        int start = json.indexOf(tag) + tag.length() + 3;
        String part = json.substring(start);
        int end = part.indexOf("\"");
        String value = json.substring(start,start+end);
        return(value);
    }


            new Thread() {
                public void run() {
                    accesstoken = login();
                    String result = callApi("/api/me/vehicles/v2?all=true");
                    String vin = getField(result,"vin");

                    result = callApi("/api/vehicle/dynamic/v1/"+vin+"?offset=0");
                    Log.d("bmwinfo - result",result);
                    String batterysize = getField(result,"battery_size_max");
                    String milesrange = getField(result,"beRemainingRangeElectricMile");
                    String batterypercent = getField(result,"chargingLevelHv");

                    Log.d("bmwinfo","Battery " + batterypercent + "% ( " + milesrange + " miles range )");
                    Log.d("bmwinfo","Battery " + batterysize  + " Wh");

                    String charging = getField(result,"chargingLogicCurrentlyActive");
                    Log.d("bmwinfo",charging);


/*
                            'door_driver_front': 'CLOSED',
                            'door_passenger_front': 'CLOSED',
                            'door_driver_rear': 'CLOSED',
                            'door_passenger_rear': 'CLOSED',

                            'window_driver_front': 'CLOSED',
                            'window_passenger_front': 'CLOSED',
                            'window_passenger_rear': 'CLOSED',
                            'window_driver_rear': 'CLOSED',

                            'hood_state': 'CLOSED',
                            'trunk_state': 'CLOSED',

                            'lights_parking': 'OFF',
                            'door_lock_state': 'SECURED',

                            'mileage': '5250',

                            'Segment_LastTrip_time_segment_end_formatted_date': '24.02.2020',
                            'Segment_LastTrip_time_segment_end_formatted_time': '09:01 AM',
                            'Segment_LastTrip_time_segment_end_formatted': '24.02.2020 09:01 AM',
                            'Segment_LastTrip_time_segment_end': '24.02.2020 09:01:00 UTC',

                            'updateTime_converted_time': '09:01 AM',
                            'updateTime_converted_date': '24.02.2020',
                            'updateTime_converted_timestamp': '1582534913000',
                            'updateTime': '24.02.2020 09:01:53 UTC',

                            'lsc_trigger': 'VEHCSHUTDOWN_SECURED',

                            'lastUpdateReason': 'VEHCSHUTDOWN_SECURED'}

 */
                    // result = callApi("/api/vehicle/efficiency/v1/"+vin);
                    Log.d("bmwinfo - result",result);
                }
            }.start();
