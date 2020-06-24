package de.dfki.vsm.model.sceneflow.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import de.dfki.vsm.Preferences;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.model.sceneflow.glue.command.definition.FunctionDefinition;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;

/**
 * @author Gregor Mehlmann
 */
public final class SceneFlow extends SuperNode {

    protected String mXMLNameSpace = new String();
    protected String mXMLSchemeInstance = new String();
    protected String mXMLSchemeLocation = new String();
    protected String mPackageName = new String();
    protected String mContextClass = new String();
    protected String mContextCode = new String();
    protected ArrayList<String> mClassPathList = new ArrayList<String>();
    protected HashMap<String, FunctionDefinition> mUserCmdDefMap = new HashMap<String, FunctionDefinition>();
    protected String mModifDate = new String();

    public SceneFlow() {
    }

    public String getContextCode() {
        return mContextCode;
    }

    public void setContextCode(String initContext) {
        mContextCode = initContext;
    }

    public String getContextClass() {
        return mContextClass;
    }

    public void setContextClass(String value) {
        mContextClass = value;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String value) {
        mPackageName = value;
    }

    public ArrayList<String> getClassPathList() {
        return mClassPathList;
    }

    public void setClassPathList(ArrayList<String> classPath) {
        mClassPathList = classPath;
    }

    public ArrayList<String> getCopyOfClassPathList() {
        ArrayList<String> copy = new ArrayList<String>();

        for (String str : mClassPathList) {
            copy.add(str);
        }

        return copy;
    }

    public HashMap<String, FunctionDefinition> getUsrCmdDefMap() {
        return mUserCmdDefMap;
    }

    public void setUsrCmdDefMap(HashMap<String, FunctionDefinition> value) {
        mUserCmdDefMap = value;
    }

    public void putUsrCmdDef(String key, FunctionDefinition value) {
        mUserCmdDefMap.put(key, value);
    }

    public FunctionDefinition getUsrCmdDef(String key) {
        return mUserCmdDefMap.get(key);
    }

    public FunctionDefinition removeUsrCmdDef(String key) {
        return mUserCmdDefMap.remove(key);
    }

    public FunctionDefinition getUserCommandDefinitionAt(String key) {
        return mUserCmdDefMap.get(key);
    }

    public void setUserCommandDefinitionAt(String key, FunctionDefinition value) {
        mUserCmdDefMap.put(key, value);
    }

    protected void writeCommands(IOSIndentWriter out) throws XMLWriteError {
      if (Command.convertToVOnDA) {
        out.println("<Commands>").push();
        Command.writeListXML(out,
            mUserCmdDefMap.values().stream()
            .filter((FunctionDefinition f) -> f.isActive())
            .collect(Collectors.toList()));
        Command.writeListXML(out, mTypeDefList);
        Command.writeListXML(out, mVarDefList);
        Command.writeListXML(out, mCmdList);
        out.pop().println("</Commands>");
      } else {
        super.writeCommands(out);
      }
    }

    protected void writeFieldsXML(IOSIndentWriter out) throws XMLWriteError {
      super.writeFieldsXML(out);

      if (! Command.convertToVOnDA && !mUserCmdDefMap.isEmpty()) {
        out.println("<UserCommands>").push();
        Command.writeListXML(out, mUserCmdDefMap.values().stream()
            .filter((FunctionDefinition f) -> f.isActive())
            .collect(Collectors.toList()));
        out.pop().println("</UserCommands>");
      }

      if (! Command.convertToVOnDA) {
        out.println("<ClassPath>").push();

        for (int i = 0; i < mClassPathList.size(); i++) {
          out.println("<ClassPathElement>").push();
          out.println(mClassPathList.get(i));
          out.pop().println("</ClassPathElement>");
        }

        out.pop().println("</ClassPath>");
        out.print("<InitContext>");
        out.print(mContextCode);
        out.println("</InitContext>");
      }
    }

    @Override
    public void writeXML(IOSIndentWriter out) throws XMLWriteError {
        String start = "";

        for (String id : mStartNodeMap.keySet()) {
            start += id + ";";
        }

        //out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<SceneFlow "
                + "id=\"" + mNodeId + "\" "
                + "name=\"" + mNodeName + "\" "
                + "comment=\"" + mComment + "\" hideLocalVar=\"" + mHideLocalVarBadge + "\" hideGlobalVar=\"" + mHideGlobalVarBadge + "\" "
                + "modifDate=\"" + Preferences.sDATE_FORMAT.format(new Date()) + "\" " + "start=\""
                + start + "\" "
                // + "context=\""+(context.equals("") ? "java.lang.Object" : context)+"\" "
                + "context=\"" + mContextClass + "\" " + "package=\"" + mPackageName + "\" "
                // + "scenefile=\"" + mSceneFileName + "\" "
                // + "sceneinfo=\"" + mSceneInfoFileName + "\" "
                + (Command.convertToVOnDA ? "" : "xmlns=\"" + Preferences.getProperty("xmlns") + "\" ") // JAXB chokes on namespace
                + "xmlns:xsi=\"" + Preferences.getProperty("xmlns_xsi")
                + "\" " + "xsi:schemaLocation=\"" + Preferences.getProperty("xmlns") + " "
                + Preferences.getProperty("xsi_schemeLocation") + "\">").push();

        writeFieldsXML(out);

        out.pop().print("</SceneFlow>");
    }

    @Override
    public void parseXML(Element element) throws XMLParseError {
        super.parseXML(element);

        mContextClass = element.getAttribute("context");
        mPackageName = element.getAttribute("package");
        mXMLSchemeLocation = element.getAttribute("xsi:schemaLocation");
        mXMLNameSpace = element.getAttribute("xmlns");
        mXMLSchemeInstance = element.getAttribute("xmlns:xsi");
        mModifDate = element.getAttribute("modifDate");

        XMLParseAction.processChildNodes(element, new XMLParseAction() {
            public void run(Element element) throws XMLParseError {
                String tag = element.getTagName();

                if (tag.equals("UserCommands")) {
                    XMLParseAction.processChildNodes(element, "UserCommand", new XMLParseAction() {
                        public void run(Element element) throws XMLParseError {
                            FunctionDefinition def = new FunctionDefinition();

                            def.parseXML(element);
                            mUserCmdDefMap.put(def.getName(), def);
                        }
                    });
                } else if (tag.equals("ClassPath")) {
                    XMLParseAction.processChildNodes(element, "ClassPathElement", new XMLParseAction() {
                        public void run(Element element) throws XMLParseError {
                            mClassPathList.add(element.getTextContent().trim());
                        }
                    });
                } else if (tag.equals("InitContext")) {
                    mContextCode = element.getTextContent().trim();
                } else {
                  /* TODO: THIS WOULD ONLY WORK IF THE SUPERCLASSES REMOVED
                   * THE ELEMENTS THEY CAN HANDLE, but it's not reliable anyway
                    throw new XMLParseError(null,
                            "Cannot parse the element with the tag \"" + tag
                            + "\" into a sceneflow child!");*/
                }
            }
        });
    }

   
}
