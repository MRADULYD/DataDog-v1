public class Logic {

    static ApiClient apiClient = Configuration.getDefaultApiClient();
    static String nextLogId = null;
    static List<Log> currentLogList = new ArrayList<>();

    public static void main(String[] args) throws ApiException, ParseException, IOException {

        ArrayList<ArrayList<String>> parent = new ArrayList<>();
        LogsApi logsApi = new LogsApi();
        LogsListRequest body = new LogsListRequest();
        try {

            HashMap<String, String> serverVariables = new HashMap<String, String>();
            serverVariables.put("site","datadoghq.eu"); // or datadoghq.com, you can find this on the url of your datadog account

            HashMap<String,String> secrets = new HashMap<String, String>();
            secrets.put("apiKeyAuth","your_api_key");
            secrets.put("appKeyAuth","your_app_key");

            //serverVariables.put()
            apiClient.setServerVariables(serverVariables);
            apiClient.configureApiKeys(secrets);
            apiClient.setTempFolderPath("\\");


            logsApi.setApiClient(apiClient);

            Scanner in = new Scanner(System.in);
            System.out.println("Please enter search start time in Format: dd-MM-yyyy HH:mm:ss");
            String startTime = in.nextLine();

            System.out.println("Please enter search end time in Format: dd-MM-yyyy HH:mm:ss");
            String endTime = in.nextLine();

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            String fileSuffix = startDate.toString().trim().replace(":","").replace("2021","");
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String fStartDate = format.format(startDate);
            String fEndTime = format.format(endDate);

            body.setIndex("*");
            body.setSort(LogsSort.TIME_ASCENDING);
            body.setLimit(1000);
            body.setQuery("service:YOU-SERVICE-NAME-HERE* AND *YOUR_SEARCH_KEYWORDS_1* AND *YOUR_SEARCH_KEYWORDS_2*");
            LogsListRequestTime logsListRequestTime = new LogsListRequestTime();
            ZonedDateTime fStartDate_Z = ZonedDateTime.parse(fStartDate.split("\\+",2)[0]+ "Z");
            ZonedDateTime fEndTime_Z = ZonedDateTime.parse(fEndTime.split("\\+",2)[0] + "Z");
            logsListRequestTime.setTimezone("UTC+00:00");
            System.out.println("You may want to print the times to confirm the formats");
            logsListRequestTime.setFrom(OffsetDateTime.from((fStartDate_Z).plusHours(4).plusMinutes(30)));
            logsListRequestTime.setTo(OffsetDateTime.from((fEndTime_Z)).plusHours(4).plusMinutes(30));
            body.setTime(logsListRequestTime);
            nextLogId = "";
            currentLogList = logsApi.listLogs().body(body).executeWithHttpInfo().getData().getLogs();
            nextLogId = logsApi.listLogs().body(body).execute().getNextLogId(); // This is the id from where your next log search will start.
            while(nextLogId != null){
                try{
                    currentLogList.forEach( e->
                    {
                        String[] logStatement = e.getContent().getMessage().split("Filename : ")[1].split("\\.");
                        //your further logic goes here. You can split the log statement to find your keywords
                        //and do your analytics. You would probably want to store your data in any of the suitable
                        //data structure and print it in to an excel.
                    });
                    body.startAt(nextLogId); //next log search starts from n+1 log id. where n is your last fetched log.
                    currentLogList = logsApi.listLogs().body(body).executeWithHttpInfo().getData().getLogs();
                    nextLogId = logsApi.listLogs().body(body).execute().getNextLogId();
                }
                //To continue log search in case of an exception
                catch (ApiException ex) {
                    ex.printStackTrace();
                    System.out.println(ex.getMessage());
                    body.startAt(nextLogId);
                    currentLogList = logsApi.listLogs().body(body).executeWithHttpInfo().getData().getLogs();
                    nextLogId = logsApi.listLogs().body(body).execute().getNextLogId();
                }
                catch (Exception ex){
                    System.out.println(ex.getMessage());
                    body.startAt(nextLogId);
                    currentLogList = logsApi.listLogs().body(body).executeWithHttpInfo().getData().getLogs();
                    nextLogId = logsApi.listLogs().body(body).execute().getNextLogId();
                }
            }
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
            //Your logic that you may want to apply in case of a failure.
            currentLogList = logsApi.listLogs().body(body).executeWithHttpInfo().getData().getLogs();
            nextLogId = logsApi.listLogs().body(body).execute().getNextLogId();
        }
    }
}
