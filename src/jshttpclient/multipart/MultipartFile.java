package jshttpclient.multipart;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.util.Arrays;

public class MultipartFile extends MultipartPart{
	public MultipartFile(String name, File file) throws UnsupportedEncodingException, IOException{
		StringBuilder writer = new StringBuilder();
		String fileName = file.getName();
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"").append(MultipartMessage.CRLF);        
        String ct = URLConnection.guessContentTypeFromName(fileName);
        if( ct == null )
        	ct = "text/plain";        
        writer.append("Content-Type: " + ct).append(MultipartMessage.CRLF);
        writer.append("Content-Transfer-Encoding: binary").append(MultipartMessage.CRLF).append(MultipartMessage.CRLF);

        FileInputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
            writer.append(new String(buffer, "ISO-8859-1").substring(0, bytesRead));
            Arrays.fill(buffer, (byte)0);
        }
        inputStream.close();
        
        writer.append(MultipartMessage.CRLF);
        this.raw = writer.toString();
	}
	
	public String getContent(){
		return this.raw;
	}
}
