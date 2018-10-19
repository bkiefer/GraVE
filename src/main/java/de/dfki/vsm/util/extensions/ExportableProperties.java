package de.dfki.vsm.util.extensions;

import java.util.Map;

import de.dfki.vsm.util.extensions.value.ProjectValueProperty;

/**
 * Created by alvaro on 4/20/17.
 */
public interface ExportableProperties {

  Map<ProjectProperty, ProjectValueProperty> getExportableProperties();

  Map<ProjectProperty, ProjectValueProperty> getExportableAgentProperties();
}
