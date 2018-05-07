# UserfulClient
对使用retrofit + okhttp网络请求客户端的简单封装


## Usage
```java
    public void initClient(){
      //headers you want to add
      Map<String,String> headers = HashMap<String,String>()
      headers.put("testHeader","test")
      
      //hosts you trust,not necessary
      List<String> list = ArrayList<String>()
      list.add("www.213.com")
      list.add("www.aasfdf.com")
      list.add("h5.xiaozhijie.com")
      
      UfClient.Builder builder = UfClient.Builder(context)
      String baseUrl = "http://api.test.com/"
      builder.setBaseUrl(baseUrl)
        .setHeaders(headers)
        .setTimeout(3 * 1000)
        .setTrustHost(list) //not necessary
        .setCache(true) //not necessary
      UfClient client = builder.create()
      MyApiManager apiManager = client.createApiManager(MyApiManager::class.java)
    }
```
