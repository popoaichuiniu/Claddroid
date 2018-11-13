package com.popoaichuiniu.jacy;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParserException;

import pxb.android.axml.AxmlVisitor;
import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class MyProcessManifest extends ProcessManifest {

	public MyProcessManifest(String apkPath) throws IOException, XmlPullParserException {
		super(apkPath);
		// TODO Auto-generated constructor stub
	}
	
	public Map<String,AXmlNode> getComponentClasses() {
		// If the application is not enabled, there are no entry points
		if (!isApplicationEnabled())
			return (Map) Collections.emptyMap();
		
		// Collect the components
		Map<String,AXmlNode> components = new HashMap<String,AXmlNode>();
		for (AXmlNode node : this.activities)
			checkAndAddComponent(components, node);
		for (AXmlNode node : this.providers)
			checkAndAddComponent(components, node);
		for (AXmlNode node : this.services)
			checkAndAddComponent(components, node);
		for (AXmlNode node : this.receivers)
			checkAndAddComponent(components, node);
		
		
		
		return components;
	}
	private void checkAndAddComponent(Map<String,AXmlNode> components, AXmlNode node) {
		AXmlAttribute<?> attrEnabled = node.getAttribute("enabled");
		if (attrEnabled == null || !attrEnabled.getValue().equals(Boolean.FALSE)) {
			AXmlAttribute<?> attr = node.getAttribute("name");
			if (attr != null)
				components.put(expandClassName((String) attr.getValue()),node);
			else {
				// This component does not have a name, so this might be obfuscated
				// malware. We apply a heuristic.
				for (Entry<String, AXmlAttribute<?>> a : node.getAttributes().entrySet())
					if (a.getValue().getName().isEmpty()
							&& a.getValue().getType() == AxmlVisitor.TYPE_STRING) {
						String name = (String) a.getValue().getValue();
						if (isValidComponentName(name))
							{
							
								components.put(expandClassName(name),node);
							}
							
					}
			}
		}
	}

	private boolean judgeEA(AXmlNode node) {
		// TODO Auto-generated method stub
		return false;
	}

}
