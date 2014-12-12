package jshttpclient.multipart;


public abstract class MultipartPart {
	protected String raw;
	public abstract String getContent();
}
