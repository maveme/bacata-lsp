package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.rascalmpl.repl.CompletionResult;
	
public class TemplateTextDocumentService implements TextDocumentService {
	
	private BacataServer server;
	private final Map<String, BacataModel> docs = Collections.synchronizedMap(new HashMap<>());
	
	public TemplateTextDocumentService(BacataServer bacataServer) {
		this.server = bacataServer;
	}
	
	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
		BacataModel document = docs.get(position.getTextDocument().getUri());
		String currentLine = document.getResolvedLines().get(document.getResolvedLines().size()-1).text;
		CompletionResult result = this.server.getLanguage().completeFragment(currentLine, currentLine.length());
		ArrayList<String> resultz = (ArrayList<String>) result.getSuggestions();
		
		return CompletableFuture.completedFuture(Either.forLeft(convert(resultz)));
	}
	
	public List<CompletionItem> convert(ArrayList<String> input) {
		List<CompletionItem> completionItems = new ArrayList<>();
		for (String string : input) {
			completionItems.add(new CompletionItem(string));
		}
		return completionItems;
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		BacataModel model = new BacataModel(params.getTextDocument().getText());
		docs.put(params.getTextDocument().getUri(), model);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		BacataModel model = new BacataModel(params.getContentChanges().get(0).getText());
		docs.put(params.getTextDocument().getUri(), model);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		this.docs.remove(params.getTextDocument().getUri());
		
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
	}

}