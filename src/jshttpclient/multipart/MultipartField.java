package jshttpclient.multipart;

public class MultipartField extends MultipartPart {
	public MultipartField(String name, Object value){
		StringBuilder writer = new StringBuilder();
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(MultipartMessage.CRLF).append(MultipartMessage.CRLF);
        writer.append(value).append(MultipartMessage.CRLF);
        this.raw = writer.toString();
	}

	public String getContent(){
		return this.raw;
	}
}
