package com.iw.plugins.spindle.ui;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;

/**
 * @author administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DTDLableDecorator implements ILabelDecorator {

	/**
	 * Constructor for DTDLableDecorator.
	 */
	public DTDLableDecorator() {
		super();
	}

	private ITapestryModel getTapestryModel(Object element) {
		IStorage resource = null;
		try {
			resource = (IStorage) element;
		} catch (Exception e) {
			if (element instanceof IAdaptable) {
				resource =
					(IStorage) ((IAdaptable) element).getAdapter(
						IStorage.class);
			}
		}
		if (resource != null) {
			return TapestryPlugin.getTapestryModelManager().getModel(resource);
		}
		return null;
	}

	private String getDTDString(ITapestryModel model) {
		if (!model.isLoaded()) {
			return "undetermined";
		}
		String DTDversion = null;
		if (model.getClass() == TapestryComponentModel.class) {
			PluginComponentSpecification spec =
				((TapestryComponentModel) model).getComponentSpecification();
			DTDversion = spec.getDTDVersion();
		} else if (model.getClass() == TapestryApplicationModel.class) {
			PluginApplicationSpecification spec =
				((TapestryApplicationModel) model).getApplicationSpec();
			DTDversion = spec.getDTDVersion();
		}
		if (DTDversion == null) {
			return "?";
		}
		return DTDversion;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(Image, Object)
	 * we return null here as we are not decorating images (yet)
	 */
	public Image decorateImage(Image image, Object element) {
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(String, Object)
	 */
	public String decorateText(String text, Object element) {
		ITapestryModel model = getTapestryModel(element);
		if (model != null) {
			return text + " (DTD " + getDTDString(model) +")";
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
	 * don't really know what to do with this yet.
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 * we are not decorating images yet so this is not needed.
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return getTapestryModel(element) != null;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}
