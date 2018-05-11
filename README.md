# ReoClient
对使用retrofit + okhttp网络请求客户端的封装


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
        .setTimeout(3 * 1000)
        .setTrustHost(list) //not necessary
        .setCacheable(true) //not necessary
        .openLog(true) //not necessary
        .create()
      
      MyApiManager apiManager = client.createApiManager(MyApiManager::class.java,url1)
      MyApiManager2 apiManager = client.createApiManager(MyApiManager2::class.java,url2)
    }
```
## How to do
### step 1.Add it in your root build.gradle at the end of repositories:
```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
### step 2. Add the dependency
```groovy
	dependencies {
          ...
	        compile 'com.github.abcdqianlei1990:ReoClient:1.0.3'
	}
```

