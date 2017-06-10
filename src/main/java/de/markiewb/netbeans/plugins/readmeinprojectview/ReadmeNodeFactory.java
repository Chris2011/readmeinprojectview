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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

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

    private final List<String> keywords = Arrays.asList(
            ".gitlab-ci.yml",
            "readme",
            "authors",
            "changelog",
            "dockerfile",
            "docker-compose.yml",
            "docker-compose.yaml",
            "contributing"
    );

    @Override
    public NodeList createNodes(Project project) {
        File dir = FileUtil.toFile(project.getProjectDirectory());
        FilenameFilter f = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                String lcName = name.toLowerCase();
                for (final String keyword : keywords) {
                    if (lcName.startsWith(keyword + ".") || keyword.equals(lcName)) {
                        return true;
                    }
                }

                return false;
            }
        };
        File[] listFiles = dir.listFiles(f);
        if (null == listFiles || listFiles.length == 0) {
            return NodeFactorySupport.fixedNodeList();
        }

        List<Node> nodes = new ArrayList<Node>(3);
        for (File file : listFiles) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            try {
                DataObject find = DataObject.find(fo);
                if (find != null) {
                    Node cloneNode = find.getNodeDelegate().cloneNode();
                    if (null != cloneNode) {
                        nodes.add(cloneNode);
                    }
                }
            } catch (DataObjectNotFoundException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }

        return NodeFactorySupport.fixedNodeList(nodes.toArray(new Node[nodes.size()]));

    }

}
