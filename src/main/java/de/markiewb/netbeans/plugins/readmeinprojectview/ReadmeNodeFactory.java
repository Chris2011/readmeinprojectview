/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.markiewb.netbeans.plugins.readmeinprojectview;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
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
    "org-netbeans-modules-ant-freeform", "org-netbeans-modules-apisupport-project", "org-netbeans-modules-apisupport-project-suite", "org-netbeans-modules-apisupport-project-suite-jnlp", "org-netbeans-modules-apisupport-project-suite-osgi", "org-netbeans-modules-apisupport-project-suite-package", "org-netbeans-modules-autoproject", "org-netbeans-modules-j2ee-clientproject", "org-netbeans-modules-j2ee-earproject", "org-netbeans-modules-j2ee-ejbjarproject", "org-netbeans-modules-java-j2seproject", "org-netbeans-modules-maven", "org-netbeans-modules-web-project"
},
        position = 9000)
public class ReadmeNodeFactory implements NodeFactory {

    @Override
    public NodeList createNodes(Project project) {
        File dir = FileUtil.toFile(project.getProjectDirectory());
        FilenameFilter f = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().startsWith("readme.");
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
