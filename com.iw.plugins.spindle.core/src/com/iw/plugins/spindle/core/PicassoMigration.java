package com.iw.plugins.spindle.core;

import org.apache.tapestry.IBinding;

import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 * This class is the temporary landing zone for stuff that will eventually be
 * replaced by some form of Hivemind Support.
 * 
 * @deprecated
 */
public class PicassoMigration {

	public static final String DEFAULT_TEMPLATE_EXTENSION = "html";

	public static final String TEMPLATE_ASSET_NAME = "$template";

	public static final int OGNL_BINDING = 0;

	public static final int MESSAGE_BINDING = 1;

	public static final int LITERAL_BINDING = 2;

	public static final int ASSET_BINDING = 3;

	public static final int BEAN_BINDING = 4;

	public static final int LISTENER_BINDING = 5;

	public static final int COMPONENT_BINDING = 6;

	public static final int HIVEMIND_BINDING = 7;

	public static final String[] BINDING_NAMES = { "ognl", "message", "",
			"asset", "bean", "listener", "component", "hivemind", "" };

	// TODO this is ugly
	public static int getBindingType(String prefix) {

		if (prefix == null)
			return LITERAL_BINDING;

		for (int i = 0; i < BINDING_NAMES.length; i++) {
			if (BINDING_NAMES[i].equals(prefix))
				return i;
		}
		return -1;
	}

	public static interface IBindingValidator {
		public boolean validate(IBinding binding,
				ISourceLocationInfo sourceInfo, IScannerValidator validator);
	}

	public static final int CONTEXT_ASSET = 8;

	public static final int CLASSPATH_ASSET = 9;

	public static final int DEFAULT_ASSET = 10; // external

	public static final String[] ASSET_TYPES = { "context", "classpath", "" };
		
	// TODO this is ugly
	public static int getAssetType(String prefix) {

		if (prefix == null)
			return DEFAULT_ASSET;

		for (int i = 0; i < ASSET_TYPES.length; i++) {
			if (ASSET_TYPES[i].equals(prefix))
				return i;
		}
		return -1;
	}
}
