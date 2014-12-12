package jshttpclient;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import jshttpclient.multipart.MultipartMessage;

public class JSHttpClient {
	private static final String[] VERBS = {"GET", "POST", "PUT", "DELETE", "HEAD" };
	private HttpURLConnection http;
	private HttpsURLConnection https;
	private HashMap<String, String> headers;
	//private String body;
	
	public JSHttpClient() {
		headers = new HashMap<String, String>();
		//body = "";
	}
	
	public void setHeader(String name, String value){
		headers.put(name, value);
	}
	
	public void setHeaders(HashMap<String, String> headers){
		this.headers.putAll(headers);
	}
	
	public HttpURLConnection open(String url) throws Exception{
		if( http != null || https != null )
			throw new Exception("There is a currently active HTTP conenction.");
		return (http = (HttpURLConnection) new URL(url).openConnection());
	}
		
	public HttpsURLConnection openSSL(String url) throws Exception{
		return this.openSSL(url, null, null, null);
	}
	
	public HttpsURLConnection openSSL(String url, String certUrl) throws Exception{
		return this.openSSL(url, certUrl, null, null);
	}
	
	public HttpsURLConnection openSSL(String url, TrustManager[] trusts, HostnameVerifier validHosts) throws Exception{
		if( http != null || https != null )
			throw new Exception("There is a currently active HTTP conenction.");
		URL obj = new URL(url);
		HttpsURLConnection con = null;
		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trusts, new java.security.SecureRandom());

		con = (HttpsURLConnection) obj.openConnection();
		con.setHostnameVerifier(validHosts);
		con.setSSLSocketFactory(sc.getSocketFactory());
		http = null;
		return (https = con);
	}
	
	public HttpsURLConnection openSSL(String url, String keystore, char[] keystorePass) throws Exception{
		//System.out.println("Opening " + url);
		if( http != null || https != null )
			throw new Exception("There is a currently active HTTP conenction.");
		URL obj = new URL(url);
		HttpsURLConnection con = null;
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		
		FileInputStream stream = null;
		if( keystore != null ){
			stream = new FileInputStream(new File(keystore));
		}
		keyStore.load(stream, keystorePass);
		
		// Create a TrustManager that trusts the CAs in our KeyStore
		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
		tmf.init(keyStore);

		// Create an SSLContext that uses our TrustManager
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, tmf.getTrustManagers(), null);
		con = (HttpsURLConnection) obj.openConnection();
		con.setSSLSocketFactory(context.getSocketFactory());
		http = null;
		return (https = con);
	}
	
	public HttpsURLConnection openSSL(String url, String certUrl, String keystore, char[] keystorePass) throws Exception{
		//System.out.println("Opening " + url);
		if( http != null || https != null )
			throw new Exception("There is a currently active HTTP conenction.");
		URL obj = new URL(url);
		HttpsURLConnection con = null;
		if( certUrl != null ){
			// Load CAs from an InputStream
			// (could be from a resource or ByteArrayInputStream or ...)
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream caInput = new BufferedInputStream(new URL(certUrl).openStream());
			Certificate ca;
			try {
			    ca = cf.generateCertificate(caInput);
			} finally {
			    caInput.close();
			}
	
			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			
			FileInputStream stream = null;
			if( keystore != null ){
				stream = new FileInputStream(new File(keystore));
			}
			keyStore.load(stream, keystorePass);
			keyStore.setCertificateEntry(((X509Certificate) ca).getSubjectDN().getName(), ca);
			
			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);
	
			// Create an SSLContext that uses our TrustManager
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);
			con = (HttpsURLConnection) obj.openConnection();
			con.setSSLSocketFactory(context.getSocketFactory());
		}else {
			con = (HttpsURLConnection) obj.openConnection();
		}
		http = null;
		return (https = con);
	}
	
	protected String doRequest(String verb, String body) throws Exception{
		if( !Arrays.asList(VERBS).contains(verb) )
			throw new Exception("Invalid HTTP Verb " + verb);
		if( http != null && https != null )
			throw new Exception("Please use HTTP or HTTPS");
		HttpURLConnection http = (this.http != null ? this.http : https);
		http.setRequestMethod(verb);
		if( headers != null ){
			for (String key : headers.keySet()) {
				http.setRequestProperty(key, headers.get(key));
				//System.out.println(key+": " + headers.get(key));
			}
		}
		
		if( body != null ){
			//System.out.println("Body: " + /*(body.length() > 100 ? body.substring(0, 100) : body)*/ body);
			http.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(http.getOutputStream());
			wr.writeBytes(body);
			wr.flush();
			wr.close();
		}
 
		int responseCode = http.getResponseCode();
		//System.out.println("\nSending '"+verb+"' request to URL : " + http.getURL().toString());
		//System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(http.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		//System.out.println(response.toString());
		this.close();
		return response.toString();
	}
	
	// HTTP GET request
	public String GET() throws Exception {
		return this.doRequest("GET", null);
	}
		
	// HTTP POST request
	public String POST(String body) throws Exception {
		return this.doRequest("POST", body);
	}
	
	public String POST(HashMap<String, String> params) throws Exception {
		return this.doRequest("POST", this.httpBuildQuery(params));
	}
	
	public String POST(MultipartMessage m) throws Exception {
		String body = m.getBody();
		HashMap<String, String> headers = m.getHeaders();
		this.setHeaders(headers);
		return this.doRequest("POST", body);
	}
	
	public String HEAD() throws Exception {
		return this.doRequest("HEAD", null);
	}
	
	public String PUT(String body) throws Exception {
		return this.doRequest("PUT", body);
	}
	
	public String DELETE() throws Exception {
		return this.doRequest("DELETE", null);
	}
	
	public void close(){
		if( this.http != null ) {
			this.http.disconnect();
			this.http = null;
		}else if( this.https != null ) {
			this.https.disconnect();
			this.https = null;
		}
		headers = new HashMap<String, String>();
	}
	
	private String httpBuildQuery(HashMap<String, String> params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

    	Set<String> keys = params.keySet();
    	for (String key : keys) {
    		if( !first )
    			result.append("&");
    		
			result.append(URLEncoder.encode(key, "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(params.get(key), "UTF-8"));
			first = false;
		}
	    return result.toString();
	}
}
