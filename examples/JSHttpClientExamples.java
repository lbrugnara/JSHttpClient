
import java.io.File;
import jshttpclient.JSHttpClient;
import jshttpclient.multipart.MultipartField;
import jshttpclient.multipart.MultipartFile;
import jshttpclient.multipart.MultipartMessage;

class JSHttpClientExamples {
    public static void main(String[] args) {
        JSHttpClient http = new JSHttpClient();
        String resp = null;
        try {
            /*=======================*/
            /*= HTTPS GET request 	=*/
            /*= Content type JSON   =*/
            /*=======================*/            
            http.openSSL("https://<somedomain>");
            http.setHeader("Accept", "application/json");
            resp = http.GET();

            /*=======================*/
            /*= HTTPS POST request  =*/
            /*= Content type JSON   =*/
            /*=======================*/
            http.openSSL("https://<somedomain>", "<certificate URL");
            http.setHeader("Accept", "application/json");
            http.setHeader("Content-type", "application/json");
            resp = http.POST(
                "{"				+
                "	\"a\":\"b\""+
                "}"
            );

            /*====================================*/
            /*= GET request                      =*/
            /*====================================*/
            http.open("http://<somedomain>");
            resp = http.GET();

            /*====================================*/
            /*= POST request                     =*/
            /*= Content type multipart/form-data =*/
            /*====================================*/
            http.open("http://<somedomain>");
            MultipartMessage message = new MultipartMessage();
            message.addPart(new MultipartField("param1", "1"));
            message.addPart(new MultipartField("param2", 2));			
            message.addPart(new MultipartFile("file", new File("<path to file>")));
            resp = http.POST(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}