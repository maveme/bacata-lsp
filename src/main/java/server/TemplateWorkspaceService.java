package server;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

public class TemplateWorkspaceService implements WorkspaceService {

	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
		return null;
	}

	public void didChangeConfiguration(DidChangeConfigurationParams params) {
	}

	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
	}

}