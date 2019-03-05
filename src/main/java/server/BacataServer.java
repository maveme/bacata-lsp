package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
import org.rascalmpl.interpreter.utils.RascalManifest;
import org.rascalmpl.repl.ILanguageProtocol;
import org.rascalmpl.repl.RascalInterpreterREPL;
import org.rascalmpl.shell.ShellEvaluatorFactory;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValueFactory;

public class BacataServer implements LanguageServer, LanguageClientAware {
	
	private WorkspaceService workspace;
	private TextDocumentService textService;
	private LanguageClient client;
	
	private ILanguageProtocol language;
	private StringWriter stdout;
	private StringWriter stderr;
	
	private static final String JAR_FILE_PREFIX = "jar:file:";
	
	public BacataServer() {
		this.workspace =  new TemplateWorkspaceService();
		try {
			this.language = makeInterpreter();
			stdout = new StringWriter();
			stderr = new StringWriter();
			this.language.initialize(stdout, stderr);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		this.textService = new TemplateTextDocumentService(this);
	}
	
	private static ISourceLocation createJarLocation(IValueFactory vf, URL u) throws URISyntaxException {
		String full = u.toString();
		if (full.startsWith(JAR_FILE_PREFIX)) {
			full = full.substring(JAR_FILE_PREFIX.length());
			return vf.sourceLocation("jar", null, full);
		}
		else {
			return vf.sourceLocation(URIUtil.fromURL(u));
		}
	}

	private ILanguageProtocol makeInterpreter() throws IOException, URISyntaxException{
		return new RascalInterpreterREPL(null, null, false, false, true, null) {
			@Override
			protected Evaluator constructEvaluator(Writer stdout, Writer stderr) {
				Evaluator e = ShellEvaluatorFactory.getDefaultEvaluator(new PrintWriter(stdout), new PrintWriter(stderr));
				try {
					e.addRascalSearchPathContributor(StandardLibraryContributor.getInstance());
					IValueFactory vf = ValueFactoryFactory.getValueFactory();
					Enumeration<URL> res = ClassLoader.getSystemClassLoader().getResources(RascalManifest.META_INF_RASCAL_MF);
					RascalManifest mf = new RascalManifest();
					while (res.hasMoreElements()) {
						URL next = res.nextElement();
						List<String> roots = mf.getManifestSourceRoots(next.openStream());
						if (roots != null) {
							ISourceLocation currentRoot = createJarLocation(vf, next);
							currentRoot = URIUtil.getParentLocation(URIUtil.getParentLocation(currentRoot));
							for (String r: roots) {
								e.addRascalSearchPath(URIUtil.getChildLocation(currentRoot, r));
							}
							e.addRascalSearchPath(URIUtil.getChildLocation(currentRoot, RascalManifest.DEFAULT_SRC));
						}
					}
				} catch (URISyntaxException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return e;
			}
		};
	}

	@Override
	public void connect(LanguageClient client) {
		this.client = client;
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		InitializeResult res = new InitializeResult(new ServerCapabilities());
		res.getCapabilities().setCompletionProvider(new CompletionOptions());
		res.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
		return CompletableFuture.supplyAsync(() -> res);
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return CompletableFuture.supplyAsync(() -> Boolean.TRUE);
	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return this.textService;
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		return this.workspace;
	}

	public ILanguageProtocol getLanguage() {
		return this.language;
	}

	
}
