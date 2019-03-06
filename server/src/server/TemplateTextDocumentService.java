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
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.rascalmpl.repl.CompletionResult;

import server.BacataModel.DocumentLine;
	
public class TemplateTextDocumentService implements TextDocumentService {
	
	private BacataServer server;
	private final Map<String, BacataModel> docs = Collections.synchronizedMap(new HashMap<>());
	
	public TemplateTextDocumentService(BacataServer bacataServer) {
		this.server = bacataServer;
	}
	
	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
		BacataModel document = docs.get(position.getTextDocument().getUri());
		if (!document.getResolvedLines().isEmpty()) {
			int lineNumber = document.getResolvedLines().size() - 1;
			DocumentLine currentLine = document.getResolvedLines().get(lineNumber);
			CompletionResult result = this.server.getLanguage().completeFragment(currentLine.text, currentLine.text.length());
			return CompletableFuture.completedFuture(Either.forLeft(convert(currentLine.text, lineNumber, result)));
		}
		else {
			return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<CompletionItem>()));
		}
	}
	
	public List<CompletionItem> convert(String currentLine, int lineNumber, CompletionResult input) {
		List<CompletionItem> completionItems = new ArrayList<>();
		ArrayList<String> suggestions = input != null ? (ArrayList<String>) input.getSuggestions(): new ArrayList<>();
		for (String string : suggestions) {
			CompletionItem c = new CompletionItem(string);
			c.setTextEdit(new TextEdit(new Range(new Position(lineNumber, input.getOffset()), new Position(lineNumber, input.getOffset()+ string.length())), string));
			completionItems.add(c);
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