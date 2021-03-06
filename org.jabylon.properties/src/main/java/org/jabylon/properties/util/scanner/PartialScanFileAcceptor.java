/**
 * (C) Copyright 2013 Jabylon (http://www.jabylon.org) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jabylon.properties.util.scanner;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.ProjectLocale;
import org.jabylon.properties.ProjectVersion;
import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.PropertyFile;
import org.jabylon.properties.PropertyFileDescriptor;
import org.jabylon.properties.Resolvable;
import org.jabylon.properties.ScanConfiguration;
import org.jabylon.properties.types.PropertyScanner;
import org.jabylon.properties.util.PropertyResourceUtil;

public class PartialScanFileAcceptor extends AbstractScanFileAcceptor {

    public PartialScanFileAcceptor(ProjectVersion projectVersion, PropertyScanner scanner, ScanConfiguration config) {
        super(projectVersion, scanner, config);
    }

    @Override
    public void newMatch(File file) {

        if(getPropertyScanner().isTemplate(file, getScanConfig().getMasterLocale()))
            newTemplateMatch(file);
        else if(getPropertyScanner().isTranslation(file, getScanConfig()))
            newTranslationMatch(file);


    }

    private void newTranslationMatch(File file) {
        File template = getPropertyScanner().findTemplate(file, getScanConfig());
        //don't do anything if the template doesn't exist yet
        if(template==null || !template.isFile())
            return;

        Locale locale = getPropertyScanner().getLocale(file);
        ProjectLocale projectLocale = getOrCreateProjectLocale(locale);
        URI location = calculateLocation(file);

        //Test if this descriptor is already available
        PropertyFileDescriptor descriptor = (PropertyFileDescriptor) projectLocale.resolveChild(location);
        if(descriptor==null)
        {
            descriptor = createDescriptor(projectLocale, location);
        }

        // load file to initialize statistics;
        PropertyFile propertyFile = descriptor.loadProperties();
        descriptor.setKeys(propertyFile.getProperties().size());
        Resolvable<?, ?> resolvable = getProjectVersion().getTemplate().resolveChild(calculateLocation(template));
        if (resolvable instanceof PropertyFileDescriptor) {
            PropertyFileDescriptor templateDescriptor = (PropertyFileDescriptor) resolvable;
            descriptor.setMaster(templateDescriptor);
        }
        descriptor.updatePercentComplete();
    }

	private void newTemplateMatch(File file) {
		URI location = calculateLocation(file);
		if (getProjectVersion().getTemplate() == null) {
			getProjectVersion().setTemplate(PropertiesFactory.eINSTANCE.createProjectLocale());
			getProjectVersion().getTemplate().setName("template");
			getProjectVersion().getChildren().add(getProjectVersion().getTemplate());
		}

		PropertyFileDescriptor descriptor = (PropertyFileDescriptor) getProjectVersion().getTemplate().resolveChild(location);
		// Test if this descriptor is already available
		boolean exists = descriptor != null;
		if (!exists) {
			descriptor = createDescriptor(getProjectVersion().getTemplate(),location);
			getProjectVersion().getTemplate().getDescriptors().add(descriptor);
		}

		// load file to initialize statistics;
		PropertyFile propertyFile = descriptor.loadProperties();
		descriptor.setKeys(propertyFile.getProperties().size());
		descriptor.updatePercentComplete();

		Locale locale = getPropertyScanner().getLocale(file);
		if (locale != null) {
			descriptor.setVariant(locale);
		}

		Map<Locale, File> translations = getPropertyScanner().findTranslations(file, getScanConfig());
		Set<Entry<Locale, File>> set = translations.entrySet();
		for (Entry<Locale, File> entry : set) {
			ProjectLocale projectLocale = getOrCreateProjectLocale(entry.getKey());
			URI childURI = calculateLocation(entry.getValue());
	        //Test if this child descriptor is already available
	        PropertyFileDescriptor fileDescriptor = (PropertyFileDescriptor) projectLocale.resolveChild(childURI);
	        if(fileDescriptor==null)
	        {
	        	fileDescriptor = createDescriptor(projectLocale, childURI);
	        }
			fileDescriptor.setMaster(descriptor);
		}
		if (!exists) {
			PropertyResourceUtil.addNewTemplateDescriptor(descriptor,getProjectVersion());
		}
	}


    public ProjectLocale getOrCreateProjectLocale(Locale locale) {
        ProjectLocale projectLocale = getProjectVersion().getProjectLocale(locale);
        if (projectLocale == null) {
            projectLocale = PropertiesFactory.eINSTANCE.createProjectLocale();
            projectLocale.setLocale(locale);
            getProjectVersion().getChildren().add(projectLocale);
            PropertyResourceUtil.addNewLocale(projectLocale, getProjectVersion());
        }
        return projectLocale;
    }

}
