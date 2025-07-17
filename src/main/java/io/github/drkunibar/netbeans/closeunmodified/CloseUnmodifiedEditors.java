package io.github.drkunibar.netbeans.closeunmodified;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "Window", id = "io.github.drkunibar.netbeans.closeunmodified.CloseUnmodifiedEditors")
@ActionRegistration(iconBase = "io/github/drkunibar/netbeans/closeunmodified/close16x16.png", displayName = "#CTL_CloseUnmodifiedEditors")
@ActionReference(path = "Menu/Window", position = 20625)
@Messages("CTL_CloseUnmodifiedEditors=Close Unmodified Editors")
public final class CloseUnmodifiedEditors implements ActionListener {

    private static final String ATTRIBUTE_IS_MODIFIED = "ProvidedExtensions.VCSIsModified";

    @Override
    public void actionPerformed(ActionEvent e) {
        closeEditors();
    }

    private void closeEditors() {
        Collection<TopComponent> closeableTopComponents = getCloseableTopComponents();
        SwingUtilities.invokeLater(() -> {
            closeableTopComponents.forEach(TopComponent::close);
        });
    }

    private Collection<TopComponent> getCloseableTopComponents() {
        final WindowManager wm = WindowManager.getDefault();
        return WindowManager.getDefault()
                .getRegistry()
                .getOpened()
                .stream()
                // is it an editor component
                .filter(wm::isEditorTopComponent)
                // is it a cloneable
                .filter(tc -> tc instanceof CloneableTopComponent)
                // ca be closed
                .filter(tc -> tc.canClose())
                // check if file is changed
                .filter((TopComponent tc) -> {
                    Lookup lookup = tc.getLookup();
                    DataObject dataObject = lookup.lookup(DataObject.class);
                    if (dataObject == null) {
                        return false;
                    }
                    FileObject primaryFile = dataObject.getPrimaryFile();
                    if (primaryFile == null) {
                        return false;
                    }
                    Boolean isModified = (Boolean) primaryFile.getAttribute(ATTRIBUTE_IS_MODIFIED);
                    return !Objects.equals(isModified, Boolean.TRUE);
                })
                .collect(Collectors.toList());
    }
}
