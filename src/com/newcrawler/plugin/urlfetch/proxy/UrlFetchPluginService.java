package com.newcrawler.plugin.urlfetch.proxy;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.soso.plugin.UrlFetchPlugin;
import com.soso.plugin.bo.UrlFetchPluginBo;

public class UrlFetchPluginService implements UrlFetchPlugin{
	
	private final static Log logger = LogFactory.getLog(UrlFetchPluginService.class);
	public static final String PROXY_IP = "proxy.ip";
	public static final String PROXY_PORT = "proxy.port";
	public static final String PROXY_USER = "proxy.username";
	public static final String PROXY_PASS = "proxy.password";
	public static final String PROXY_TYPE = "proxy.type";
	
	public static void main(String[] args){
		Map<String, String> properties=new HashMap<String, String>(); 
		properties.put(PROXY_IP, "127.0.0.1");
		properties.put(PROXY_PORT, String.valueOf(6666));
		properties.put(PROXY_TYPE, "socks5");
		
		Map<String, String> headers=new HashMap<String, String>(); 
		String crawlUrl="http://item.jd.com/832705.html"; 
		String method=null; 
		String cookie=null; 
		String userAgent="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.36 Safari/535.7"; 
		String encoding="GB2312";
		UrlFetchPluginService urlFetchPluginService=new UrlFetchPluginService();
		//urlFetchPluginService.execute(properties, crawlUrl, method, cookie, userAgent, encoding);
		
		crawlUrl="http://www.newcrawler.com/header"; 
		UrlFetchPluginBo urlFetchPluginBo1=new UrlFetchPluginBo(properties, headers, crawlUrl, method, cookie, userAgent, encoding);
		
		Map<String, Object> map =urlFetchPluginService.execute(urlFetchPluginBo1);
		System.out.println(map.get(RETURN_DATA_KEY_CONTENT));
		
		crawlUrl="http://www.google.com"; 
		UrlFetchPluginBo urlFetchPluginBo=new UrlFetchPluginBo(properties, headers, crawlUrl, method, cookie, userAgent, encoding);
		
		map =urlFetchPluginService.execute(urlFetchPluginBo);
		System.out.println(map.get(RETURN_DATA_KEY_CONTENT));
	}
	
	@Override
	public Map<String, Object> execute(UrlFetchPluginBo urlFetchPluginBo) {
		Map<String, String> properties=urlFetchPluginBo.getProperties();
		Map<String, String> headers=urlFetchPluginBo.getHeaders();
		String crawlUrl=urlFetchPluginBo.getCrawlUrl();
		String method=urlFetchPluginBo.getMethod();
		String cookie=urlFetchPluginBo.getCookie();
		String userAgent=urlFetchPluginBo.getUserAgent();
		String encoding=urlFetchPluginBo.getEncoding();
		
		String proxyIP=null;
		int proxyPort=-1;
		String proxyUsername=null;
		String proxyPassword=null;
		String proxyType=null;
		
		if (properties != null) {
			if (properties.containsKey(PROXY_IP) && !"".equals(properties.get(PROXY_IP).trim())) {
				proxyIP = properties.get(PROXY_IP).trim();
			}
			if (properties.containsKey(PROXY_PORT) && !"".equals(properties.get(PROXY_PORT).trim())) {
				proxyPort = Integer.parseInt(properties.get(PROXY_PORT).trim());
			}

			if (properties.containsKey(PROXY_USER) && !"".equals(properties.get(PROXY_USER).trim())) {
				proxyUsername = properties.get(PROXY_USER).trim();
			}
			
			if (properties.containsKey(PROXY_PASS) && !"".equals(properties.get(PROXY_PASS).trim())) {
				proxyPassword = properties.get(PROXY_PASS).trim();
			}
			if (properties.containsKey(PROXY_TYPE) && !"".equals(properties.get(PROXY_TYPE).trim())) {
				proxyType = properties.get(PROXY_TYPE).trim();
			}
		}
		
		if(headers==null){
			headers = new HashMap<String, String>();
		}
		if(StringUtils.isNoneBlank(cookie)){
			headers.put("Cookie", cookie);
		}
		if(StringUtils.isNoneBlank(userAgent)){
			headers.put("User-Agent", userAgent);
		}
		Map<String, Object> map=null;
		try {
			map=read(proxyIP, proxyPort, proxyUsername, proxyPassword, proxyType, headers, crawlUrl, method, encoding);
		} catch (SocketException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return map;
	}
	
	private Map<String, Object> read(String proxyIP, int proxyPort, final String proxyUsername, final String proxyPassword, final String proxyType, Map<String, String> headers, String crawlUrl, String method, String encoding) throws IOException{
		Proxy.Type type=Proxy.Type.HTTP;
		if("socks5".equals(proxyType)){
			type=Proxy.Type.SOCKS;
		}
		Proxy proxy = new Proxy(type, new InetSocketAddress(proxyIP, proxyPort)); // 实例化本地代理对象，端口为8888
		if(proxyUsername!=null && proxyPassword!=null){
			Authenticator authenticator = new Authenticator() {
		        public PasswordAuthentication getPasswordAuthentication() {
		            return (new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray()));
		        }
		    };
		    Authenticator.setDefault(authenticator);
		}
		String cookie=null;
	    if(headers.containsKey("Cookie")){
	    	cookie=headers.get("Cookie");
	    	headers.remove("Cookie");
	    }
		HttpResponse httpResponse = HttpRequester.sendGet(crawlUrl, encoding, headers, cookie, proxy);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(RETURN_DATA_KEY_COOKIES, httpResponse.getHeaderMap());
		map.put(RETURN_DATA_KEY_CONTENT, httpResponse.getContent());
		map.put(RETURN_DATA_KEY_REALURL, httpResponse.getRealURL());
		map.put(RETURN_DATA_KEY_HEADERS, httpResponse.getHeaderMap());
		return map;
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub
		
	}
	
}
