# ReoClient
对使用retrofit + okhttp网络请求客户端的封装，增加复用性


## Usage
```java
    public void initClient(){
      //base url
      String url1 = "https://www.sojson.com/";
      String url2 = "http://v.juhe.cn/";
      List<String> urlList = new ArrayList<>();
      urlList.add(url1);
      urlList.add(url2);
      
      //headers you want to add
      Map<String,String> headers = HashMap<String,String>()
      headers.put("testHeader","test")
      
      //hosts you trust,not necessary
      List<String> list = ArrayList<String>()
      list.add("www.213.com")
      list.add("www.aasfdf.com")
      
      ReoClient.Builder builder = ReoClient.Builder(context)
      String baseUrl = "http://api.test.com/"
      ReoClient client = builder.setBaseUrl(urlList)
        .setHeaders(headers)
        .setTime
