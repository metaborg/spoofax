package org.metaborg.spoofax.eclipse.meta.issue;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.meta.SpoofaxMetaPlugin;
import org.metaborg.spoofax.eclipse.util.BundleUtils;
import org.osgi.framework.Bundle;

import com.google.common.collect.Iterables;


public class ReportIssueDialog extends Dialog {
    public ReportIssueDialog(Shell parent) {
        super(parent);
    }

    @Override protected Control createDialogArea(Composite parent) {
        final Composite container = (Composite) super.createDialogArea(parent);

        final Link description = new Link(container, SWT.NULL);
        description.setText("Please report issues at <a>http://yellowgrass.org/project/SpoofaxWithCore</a>.\n"
            + "Use the following information when reporting bugs:");
        description.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent event) {
                try {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(event.text));
                    
                } catch(PartInitException | MalformedURLException e) {

                }
            }
        });

        String eclipseVersionText = getProfileIUText();
        if(eclipseVersionText == null) {
            eclipseVersionText = getBundleText(Platform.getProduct().getDefiningBundle());
        }

        final Map<String, Bundle> bundles = BundleUtils.bundlesBySymbolicName(SpoofaxMetaPlugin.context());
        final Bundle spoofaxBundle = bundles.get(SpoofaxPlugin.id);

        final String systemText = SystemUtils.OS_NAME + " " + SystemUtils.OS_ARCH + " " + SystemUtils.OS_VERSION;
        
        final Text text = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.setText("Eclipse: " + getProfileIUText() + "\nSpoofax: " + getBundleText(spoofaxBundle) + "\nSystem: " + systemText);
        text.setEditable(false);
        
        return container;
    }

    @Override protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        newShell.setText("Report issue");
    }


    /**
     * Modified from http://stackoverflow.com/a/7094318/499240.
     */
    private String getProfileIUText() {
        try {
            final ProvisioningUI provisioningUI = ProvisioningUI.getDefaultUI();
            if(provisioningUI == null) {
                return null;
            }

            final String profileId = provisioningUI.getProfileId();
            final ProvisioningSession provisioningSession = provisioningUI.getSession();
            if(provisioningSession == null) {
                return null;
            }

            final IQueryable<IInstallableUnit> queryable =
                ((IProfileRegistry) provisioningSession.getProvisioningAgent()
                    .getService(IProfileRegistry.SERVICE_NAME)).getProfile(profileId);
            if(queryable == null) {
                return null;
            }

            final IQueryResult<IInstallableUnit> result = queryable.query(QueryUtil.createIUProductQuery(), null);
            if(result == null) {
                return null;
            }
            final IInstallableUnit iu = Iterables.get(result, 0);
            return iu.toString();
        } catch(Exception e) {

        }
        return null;
    }

    private String getBundleText(Bundle bundle) {
        return bundle.getSymbolicName() + " " + bundle.getVersion().toString();
    }
}
