package com.schmeedy.relaxng.eclipse.ui.internal.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

public class SchemaAssociationCommandParamValues implements IParameterValues {

	public Map<?, ?> getParameterValues() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("Associate RELAX NG Schema", "new");
		map.put("Change RELAX NG Schema", "change");
		map.put("Remove RELAX NG Schema", "remove");
		return map;
	}

}
