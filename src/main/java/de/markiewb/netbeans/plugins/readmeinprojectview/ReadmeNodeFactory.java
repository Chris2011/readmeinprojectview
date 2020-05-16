/**
 * Copyright 2013 markiewb
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package de.markiewb.netbeans.plugins.readmeinprojectview;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

@NodeFactory.Registration(projectType = {
    "org-netbeans-modules-ant-freeform",
    "org-netbeans-modules-apisupport-project",
    "org-netbeans-modules-apisupport-project-suite",
    "org-netbeans-modules-apisupport-project-suite-jnlp",
    "org-netbeans-modules-apisupport-project-suite-osgi",
    "org-netbeans-modules-apisupport-project-suite-package",
    "org-netbeans-modules-autoproject",
    "org-netbeans-modules-bpel-project",
    "org-netbeans-modules-j2ee-clientproject",
    "org-netbeans-modules-j2ee-earproject",
    "org-netbeans-modules-j2ee-ejbjarproject",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-javacard-project",
    "org-netbeans-modules-javaee-project",
    "org-netbeans-modules-javafx2-project",
    "org-netbeans-modules-maven",
    "org-netbeans-modules-php-project",
    "org-netbeans-modules-ruby-project",
    "org-netbeans-modules-sql-project",
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-xslt-project"
},
        position = 9000)
public class ReadmeNodeFactory implements NodeFactory {

    /* All considered file names will be converted to lower case before the comparison */
    public static final String DEFAULT_FILENAMES = ""
            + ".gitlab-ci.yml\n"
            + ".gitignore\n"
            + "readme\n"
            + "readme.md\n"
            + "readme.rst\n"
            + "changelog.md\n"
            + "changelog\n"
            + "authors\n"
            + "dockerfile\n"
            + "docker-compose.yml\n"
            + "docker-compose.yaml\n"
            + "contributing\n"
            + "license\n";
    static final String KEY_FILENAMES = "filenames";

    @Override
    public NodeList<?> createNodes(Project project) {
        return new ReadmeNodeList(project);
    }

    private static final class ReadmeNodeList implements NodeList<Node> {

        private final Project project;
        private final ChangeSupport cs;

        ReadmeNodeList(Project project) {
            super();
            this.project = project;
            this.cs = new ChangeSupport(this);
            // Reload node list on property change.
            final Preferences preferences = NbPreferences.forModule(DisplayReadmeFilesPanel.class);
            preferences.addPreferenceChangeListener(new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent event) {
                    if (event.getKey().equals(KEY_FILENAMES)) {
                        cs.fireChange();
                    }
                }
            });
            // Reload node list on project directory content change
            project.getProjectDirectory().addFileChangeListener(new FileChangeAdapter() {
                @Override
                public void fileDeleted(FileEvent fe) {
                    cs.fireChange();
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    cs.fireChange();
                }

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    cs.fireChange();
                }
            });
        }

        @Override
        public Node node(Node key) {
            return key;
        }

        @Override
        public List<Node> keys() {
            // Called only on first open and on change, no cache needed.
            final List<Node> keys = new ArrayList<Node>(0);
            final FileObject directory = project.getProjectDirectory();
            final String[] filenameFilters = getFilenameFilters();
            for (FileObject child : directory.getChildren()) {
                final String lcName = child.getName().toLowerCase();
                for (final String filenameFilter : filenameFilters) {
                    if (matches(lcName, filenameFilter)) {
                        keys.add(getFileNode(child));
                    }
                }
            }
            return keys;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        private static Node getFileNode(FileObject child) {
            try {
                final DataObject find = DataObject.find(child); // Never null
                return find.getNodeDelegate().cloneNode(); // Never null
            } catch (DataObjectNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private static String[] getFilenameFilters() {
        return NbPreferences.forModule(DisplayReadmeFilesPanel.class)
                .get(KEY_FILENAMES, DEFAULT_FILENAMES)
                .toLowerCase()
                .split("\n");
    }

    private static boolean matches(String filename, String filter) {
        if (filter.isEmpty()) {
            return false;
        }
        return filter.equals(filename) || filename.startsWith(filter + ".");
    }
}
