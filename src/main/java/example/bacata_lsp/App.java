package example.bacata_lsp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import server.BacataServer;

public class App {

	public static void main( String[] args ) {
        startServer(args[0]);
    }
    
    public static void startServer(String port) {
    	try {
	        Socket socket = new Socket("localhost", Integer.parseInt(port));
	
	        InputStream in = socket.getInputStream();
	        OutputStream out = socket.getOutputStream();
	        
	        BacataServer server = new BacataServer();
	    	Launcher<LanguageClient> launcher =  LSPLauncher.createServerLauncher(server, in, out);
	    	
	    	LanguageClient client = launcher.getRemoteProxy();
	    	server.connect(client);
	    	
	    	launcher.startListening();
    	}
    	catch (Exception e) {
			e.printStackTrace();
		}
    }
}
