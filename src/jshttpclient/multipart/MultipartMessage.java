package jshttpclient.multipart;

import java.util.ArrayList;
import java.util.HashMap;

public class MultipartMessage {
	public static final String CRLF = "\r\n";
	private String boundary;
	private ArrayList<MultipartPart> parts;
	
	public MultipartMessage(){		
		boundary = "xxx" + System.currentTimeMillis() + "xxx";
		parts = new ArrayList<MultipartPart>();
	}
	
	public String getBoundary(){
		return boundary;
	}
	
	public void addPart(MultipartPart part){
		this.parts.add(part);
	}

	public int getLength(){
		return this.toString().length();
	}

	public HashMap<String, String> getHeaders(){
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Length", String.valueOf(this.getLength()));
		headers.put("Content-Type", "multipart/form-data; boundary=" + this.boundary);
		return headers;
	}

	public String getBody(){
		return this.toString();
	}
	
	@Override
	public String toString() {
		if( this.parts == null || this.parts.size() == 0 )
			return "";
		String body = "";
		for (MultipartPart part : parts) {
			body += "--"+ this.boundary + MultipartMessage.CRLF;
			body += part.getContent();
		}
		
		body += MultipartMessage.CRLF;
		body += "--" + this.boundary + MultipartMessage.CRLF + MultipartMessage.CRLF;
		return body;
	}
}
