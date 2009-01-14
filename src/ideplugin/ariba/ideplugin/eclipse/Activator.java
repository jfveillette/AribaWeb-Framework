package ariba.ideplugin.eclipse;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.osgi.framework.BundleContext;
import ariba.ideplugin.core.RemoteOpen;
import ariba.ideplugin.core.RemoteOpen.Opener;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin  implements Opener{

	// The plug-in ID
	public static final String PLUGIN_ID = "ariba.ui.ci.cilisten";

	// The shared instance
	private static Activator plugin;
	private RemoteOpen ropen;
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ropen = new RemoteOpen(this);
		ropen.start();
	}

	public boolean open (String name, int line)
    {
        Display.getDefault().asyncExec(new OpenAction(name, line));
        return true;
    }

    public class OpenAction implements Runnable
    {
        String _file;
        int _line;

        public OpenAction (String f, int l)
        {
            _file = f;
            _line = l;
        }

        public void run ()
        {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IProject[] projects = root.getProjects();
            try {
                IFile file = recurseFind(projects);
                if(file !=null){
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
                    IEditorPart editor = IDE.openEditor(window.getActivePage(), file, true);
                    if(editor instanceof AbstractTextEditor){
                        AbstractTextEditor te = (AbstractTextEditor) editor;
                        IDocument document = te.getDocumentProvider().getDocument(te.getEditorInput());
                        try{
                            int start = document.getLineOffset(_line - 1);
                            te.selectAndReveal(start, 0);
                            te.getSite().getPage().activate(te);
                        }catch(BadLocationException ble){
                            
                        }
                    }
                }
            }
            catch (CoreException ce) {

            }
        }

        protected IFile recurseFind (IResource[] resources) throws CoreException
        {
            for (int i = 0; i < resources.length; i++) {
                if (resources[i] instanceof IContainer) {
                    if (resources[i] instanceof IProject) {
                        if (!((IProject)resources[i]).isOpen()) {
                            continue;
                        }
                    }
                    IFile ret = recurseFind(((IContainer)resources[i]).members());
                    if(ret != null)
                        return ret;
                }
                else if (resources[i] instanceof IFile) {
                    if (resources[i].exists()) {
                        if (((IFile)resources[i]).getFullPath().toString().indexOf(_file) > -1)
                        {
                            return (IFile)resources[i];
                        }
                    }
                }
            }
            return null;
        }
    }

	/*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		ropen.stop();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
