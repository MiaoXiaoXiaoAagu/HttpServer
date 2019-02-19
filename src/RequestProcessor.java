
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;
    
public class RequestProcessor implements Runnable {
  
  private final static Logger logger = Logger.getLogger(
      RequestProcessor.class.getCanonicalName());

  private File rootDirectory;
  private String indexFileName = "index.html";
  private Socket connection;
  private PostHandle postHandle;
  private Writer out;
  
  public RequestProcessor(File rootDirectory, 
      String indexFileName, Socket connection) {//init for response method like post\get\put\head
        
    if (rootDirectory.isFile()) {
      throw new IllegalArgumentException(
          "rootDirectory must be a directory, not a file");   
    }
    try {
      rootDirectory = rootDirectory.getCanonicalFile();
    } catch (IOException ex) {
    }
    this.rootDirectory = rootDirectory;

    if (indexFileName != null) this.indexFileName = indexFileName;
    this.connection = connection;
    postHandle = new PostHandle();
  }
  
  @Override
  public void run() {
    // for security checks
    String root = rootDirectory.getPath();
    try {              
      OutputStream raw = new BufferedOutputStream(
                          connection.getOutputStream()
                         );         
      out = new OutputStreamWriter(raw);
      InputStream postIn=connection.getInputStream();
    
      Reader in = new InputStreamReader(
                   new BufferedInputStream(
                    connection.getInputStream()
                   ),"US-ASCII"
                  );
      StringBuilder requestLine = new StringBuilder();
      while (true) {
        int c = in.read();
        if (c == '\r' || c == '\n') break;
        requestLine.append((char) c);
      }
      
      String get = requestLine.toString();
      
      logger.info(connection.getRemoteSocketAddress() + " " + get);
      
      String[] tokens = get.split("\\s+");
      String method = tokens[0];
      
      if (method.equals("GET")) {responseGet(tokens,out,raw,root);}
      else if(method.equals("HEAD")){responseHead(tokens,out,raw,root);}
      else if(method.equals("POST"))
      {
    	  responsePost(get,out,in);
      }
      else {
    	  System.out.println("其余情况");
    	  System.out.println(get);
    	  int temp;
		  StringBuilder postData = new StringBuilder();
		  while((temp=in.read())!=-1)
		  {	
				postData.append((char)temp);
		  }
		System.out.println(postData.toString());
      }
    } catch (IOException ex) {
      logger.log(Level.WARNING, 
          "Error talking to " + connection.getRemoteSocketAddress(), ex);
    } finally {
      try {
        connection.close();        
      }
      catch (IOException ex) {} 
    }
  }
  
  public void responsePost(String requestLine, Writer out, Reader in) throws IOException {
      System.out.println(requestLine);
      out.write("HTTP/1.1 100 Continue\r\n");
      out.flush();

      int contentLength = -1;
      String inputLine;
      int blankLine = 0;
      while (true) {
          inputLine = readLine(in);
          if(inputLine.trim().isEmpty()) {
              if(blankLine == 2) { //遇到真正的空行（head与数据之间那个）时blankLine=2
                  break;
              }else {
                  blankLine++;
              }
          }else {
              blankLine--;
              if (inputLine.startsWith("Content-Length")) {
                  contentLength = Integer.parseInt(inputLine.split(":")[1].trim());
              }
          }
      }
      StringBuilder data = new StringBuilder();
      while (contentLength > data.length()) {
          data.append((char) in.read());
      }
      
      //String rep = postHandle.handle(data.toString());
      String rep = "{\n\"status\":\"ok\"\n}";
      if (this.out != null) {
          sendHeader(out, "HTTP/1.1 200 OK", "application/json;charset=UTF-8", rep.length());
          out.write(rep);
          out.flush();
      }
  }

  private void responseHead(String[] requestLine,Writer out,OutputStream raw,String root) throws IOException
  {
	  String version = "";
	  version = requestLine[2];
	  String fileName = requestLine[1];
	  String contentType = 
	          URLConnection.getFileNameMap().getContentTypeFor(fileName);
	  File theFile = new File(rootDirectory, 
	          fileName.substring(1, fileName.length()));
	      
	      if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) { // Don't let clients outside the document root
		        if (version.startsWith("HTTP/")) { // send a MIME header
		          sendHeader(out, "HTTP/1.1 204 OK", contentType, (int)theFile.length());
		        } 
	
	      }
  }
  
  private void responseGet( String[] requestLine,Writer out,OutputStream raw,String root) throws IOException {
	  String version = "";
	  String fileName = requestLine[1];
      if (fileName.endsWith("/")) fileName += indexFileName;
      String contentType = 
          URLConnection.getFileNameMap().getContentTypeFor(fileName);
      if (requestLine.length > 2) {
        version = requestLine[2];
        
      }

      File theFile = new File(rootDirectory, 
          fileName.substring(1, fileName.length()));
      
      if (theFile.canRead() 
          // Don't let clients outside the document root
          && theFile.getCanonicalPath().startsWith(root)) {
        byte[] theData = Files.readAllBytes(theFile.toPath());
        if (version.startsWith("HTTP/")) { // send a MIME header
          sendHeader(out, "HTTP/1.1 200 OK", contentType, theData.length);
        } 
    
        // send the file; it may be an image or other binary data 
        // so use the underlying output stream 
        // instead of the writer
        raw.write(theData);
        raw.flush();
      } else { // can't find the file
        String body = new StringBuilder("<HTML>\r\n")
            .append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
            .append("</HEAD>\r\n")
            .append("<BODY>")
            .append("<H1>HTTP Error 404: File Not Found</H1>\r\n")
            .append("</BODY></HTML>\r\n").toString();
        if (version.startsWith("HTTP/")) { // send a MIME header
          sendHeader(out, "HTTP/1.1 404 File Not Found", 
              "text/html; charset=utf-8", body.length());
        } 
        out.write(body);
        out.flush();
      }
  }
  private String readLine(Reader in) {
      StringBuilder inputLine = new StringBuilder();
      while (true) {
          int c;
          try {
              c = in.read();
              if (c == '\r' || c == '\n')
                  break;
              inputLine.append((char) c);
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
      return inputLine.toString();
  }
  
  public static void sendHeader(Writer out, String responseCode,
      String contentType, int length)
      throws IOException {
    out.write(responseCode + "\r\n");
    Date now = new Date();
    out.write("Date: " + now + "\r\n");
    out.write("Server: JHTTP 2.0\r\n");
    out.write("Content-length: " + length + "\r\n");
    out.write("Content-type: " + contentType + "\r\n\r\n");
    out.flush();
  }
}