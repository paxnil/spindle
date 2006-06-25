package net.sf.spindle.ui.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.common.project.facet.ui.AbstractFacetWizardPage;

public final class TapestryCoreFacetInstallPage extends AbstractFacetWizardPage
{
    private TapestryCoreFacetInstallConfig config;
    private Text urlPatternTextField;

    public TapestryCoreFacetInstallPage()
    {
        super( "formgen.core.facet.install.page" );

        setTitle( "FormGen Core" );
        setDescription( "Configure the FormGen servlet." );
    }

    public void createControl( final Composite parent )
    {
        final Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 1, false ) );

        final Label label = new Label( composite, SWT.NONE );
        label.setLayoutData( gdhfill() );
        label.setText( "URL Pattern:" );

        this.urlPatternTextField = new Text( composite, SWT.BORDER );
        this.urlPatternTextField.setLayoutData( gdhfill() );
        this.urlPatternTextField.setText( this.config.getApplicationName() );

        setControl( composite );
    }

    public void setConfig( final Object config )
    {
        this.config = (TapestryCoreFacetInstallConfig) config;
    }

    public void transferStateToConfig()
    {
        this.config.setApplicationName( this.urlPatternTextField.getText() );
    }

    private static GridData gdhfill()
    {
        return new GridData( GridData.FILL_HORIZONTAL );
    }
}
