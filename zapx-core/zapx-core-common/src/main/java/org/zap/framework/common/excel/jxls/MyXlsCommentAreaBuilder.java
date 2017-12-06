package org.zap.framework.common.excel.jxls;


import org.jxls.area.Area;
import org.jxls.area.CommandData;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xls.AreaCommand;
import org.jxls.command.*;
import org.jxls.common.AreaRef;
import org.jxls.common.CellData;
import org.jxls.common.CellRef;
import org.jxls.transform.Transformer;
import org.jxls.util.Util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Core MyXlsCommentAreaBuilder
 * @author sunqx
 * Date: 9/16/15
 * 测试用
 */
public class MyXlsCommentAreaBuilder implements AreaBuilder {
	
    public static final String COMMAND_PREFIX = "jx:";
    private static final String ATTR_PREFIX = "(";
    private static final String ATTR_SUFFIX = ")";
    private static final String ATTR_REGEX = "\\s*\\w+\\s*=\\s*([\"|'])(?:(?!\\1).)*\\1";
    private static final Pattern ATTR_REGEX_PATTERN = Pattern.compile(ATTR_REGEX);
    private static final String AREAS_ATTR_REGEX = "areas\\s*=\\[[^]]*]";
    private static final Pattern AREAS_ATTR_REGEX_PATTERN = Pattern.compile(AREAS_ATTR_REGEX);
    
    private static Map<String, Class> commandMap = new HashMap<String, Class>();
    private static final String LAST_CELL_ATTR_NAME = "lastCell";

    static{
        commandMap.put("each", EachCommand.class);
        commandMap.put("if", IfCommand.class);
        commandMap.put("area", AreaCommand.class);
        commandMap.put("image", ImageCommand.class);
        commandMap.put("grid", GridCommand.class);
    }

    Transformer transformer;
    private boolean clearTemplateCells = true;

    public MyXlsCommentAreaBuilder() {
    }

    public MyXlsCommentAreaBuilder(Transformer transformer) {
        this.transformer = transformer;
    }

    public MyXlsCommentAreaBuilder(Transformer transformer, boolean clearTemplateCells) {
        this(transformer);
        this.clearTemplateCells = clearTemplateCells;
    }

    @Override
    public Transformer getTransformer() {
        return transformer;
    }

    @Override
    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public static void addCommandMapping(String commandName, Class clazz){
        commandMap.put(commandName, clazz);
    }

    /**
     * Builds a list of {@link org.jxls.area.XlsArea} objects defined by top level AreaCommand markup ("jx:area")
     * containing a tree of all nested commands
     * @return
     */
    public List<Area> build() {
        List<Area> userAreas = new ArrayList<Area>();
        List<CellData> commentedCells = transformer.getCommentedCells();
        List<CommandData> allCommands = new ArrayList<CommandData>();
        List<Area> allAreas = new ArrayList<Area>();
        for (CellData cellData : commentedCells) {
            String comment = cellData.getCellComment();
            List<CommandData> commandDatas = buildCommands(cellData, comment);
            for (CommandData commandData : commandDatas) {
                if( commandData.getCommand() instanceof  AreaCommand ){
                    MyXlsArea userArea = new MyXlsArea(commandData.getAreaRef(), transformer);
                    allAreas.add(userArea);
                    userAreas.add( userArea );
                }else{
                    List<Area> areas = commandData.getCommand().getAreaList();
                    allAreas.addAll(areas);
                    allCommands.add( commandData );
                }
            }
        }
        for (int i = 0; i < allCommands.size(); i++) {
            CommandData commandData = allCommands.get(i);
            AreaRef commandAreaRef = commandData.getAreaRef();
            List<Area> commandAreas = commandData.getCommand().getAreaList();
            Area minArea = null;
            List<Area> minAreas = new ArrayList<Area>();
            for (Area area : allAreas) {
                if( commandAreas.contains( area ) || !area.getAreaRef().contains(commandAreaRef)) continue;
                boolean belongsToNextCommand = false;
                for (int j = i + 1; j < allCommands.size(); j++) {
                    CommandData nextCommand = allCommands.get(j);
                    if(nextCommand.getCommand().getAreaList().contains( area )){
                        belongsToNextCommand = true;
                        break;
                    }
                }
                if( belongsToNextCommand || (minArea != null && !minArea.getAreaRef().contains(area.getAreaRef())) ) continue;
                if( minArea != null && minArea.equals( area ) ){
                    minAreas.add( area );
                }else{
                    minArea = area;
                    minAreas.clear();
                    minAreas.add( minArea );
                }
            }
            for (Area area : minAreas) {
                area.addCommand( commandData.getAreaRef(), commandData.getCommand() );
            }
        }
        if( clearTemplateCells ){
            for(Area area: userAreas){
                ((MyXlsArea)area).clearCells();
            }
        }
        return userAreas;
    }

    private List<CommandData> buildCommands(CellData cellData, String text) {
        String[] commentLines = text.split("\\n");
        List<CommandData> commandDatas = new ArrayList<CommandData>();
        for (String commentLine : commentLines) {
            String line = commentLine.trim();
            if (isCommandString(line)) {
                int nameEndIndex = line.indexOf(ATTR_PREFIX, COMMAND_PREFIX.length());
                if (nameEndIndex < 0) {
                    String errMsg = "Failed to parse command line [" + line + "]. Expected '" + ATTR_PREFIX + "' symbol.";
                    throw new IllegalStateException(errMsg);
                }
                String commandName = line.substring(COMMAND_PREFIX.length(), nameEndIndex).trim();
                Map<String, String> attrMap = buildAttrMap(line, nameEndIndex);
                CommandData commandData = createCommandData(cellData, commandName, attrMap);
                if (commandData != null) {
                    commandDatas.add(commandData);
                    List<Area> areas = buildAreas(cellData, line);
                    for (Area area : areas) {
                        commandData.getCommand().addArea( area );
                    }
                    if( areas.isEmpty() ){
                        Area area = new MyXlsArea(commandData.getAreaRef(), transformer);
                        commandData.getCommand().addArea( area );
                    }
                }
            }
        }
        return commandDatas;
    }

    public static boolean isCommandString(String str){
        return str.startsWith(COMMAND_PREFIX);
    }

    private List<Area> buildAreas(CellData cellData, String commandLine) {
        List<Area> areas = new ArrayList<Area>();
        Matcher areasAttrMatcher = AREAS_ATTR_REGEX_PATTERN.matcher(commandLine);
        if( areasAttrMatcher.find() ){
            String areasAttr = areasAttrMatcher.group();
            List<AreaRef> areaRefs = extractAreaRefs(cellData, areasAttr);
            for (AreaRef areaRef : areaRefs) {
                Area area = new MyXlsArea(areaRef, transformer);
                areas.add(area);
            }
        }
        return areas;
    }

    private List<AreaRef> extractAreaRefs(CellData cellData, String areasAttr) {
        List<AreaRef> areaRefs = new ArrayList<AreaRef>();
        Matcher areaRefMatcher = Util.regexAreaRefPattern.matcher(areasAttr);
        while( areaRefMatcher.find() ){
            String areaRefName = areaRefMatcher.group();
            AreaRef areaRef = new AreaRef(areaRefName);
            if( areaRef.getSheetName() == null || areaRef.getSheetName().trim().length() == 0){
                areaRef.getFirstCellRef().setSheetName( cellData.getSheetName() );
            }
            areaRefs.add(areaRef);
        }
        return areaRefs;  
    }

    private Map<String, String> buildAttrMap(String commandLine, int nameEndIndex) {
        int paramsEndIndex = commandLine.lastIndexOf(ATTR_SUFFIX);
        if(paramsEndIndex < 0 ){
            String errMsg = "Failed to parse command line [" + commandLine + "]. Expected '" + ATTR_SUFFIX + "' symbol.";
            throw new IllegalArgumentException(errMsg);
        }
        String attrString = commandLine.substring(nameEndIndex + 1, paramsEndIndex).trim();
        return parseCommandAttributes(attrString);
    }

    private CommandData createCommandData(CellData cellData, String commandName, Map<String, String> attrMap) {
        Class clazz = commandMap.get(commandName);
        if( clazz == null ){
            return null;
        }
        try {
            Command command = (Command) clazz.newInstance();
            for (Map.Entry<String, String> attr : attrMap.entrySet()) {
                if( !attr.getKey().equals(LAST_CELL_ATTR_NAME) ){
                    Util.setObjectProperty(command, attr.getKey(), attr.getValue(), true);
                }
            }
            String lastCellRef = attrMap.get(LAST_CELL_ATTR_NAME);
            if( lastCellRef == null ){
                return null;
            }
            CellRef lastCell = new CellRef(lastCellRef);
            if( lastCell.getSheetName() == null || lastCell.getSheetName().trim().length() == 0 ){
                lastCell.setSheetName( cellData.getSheetName() );
            }
            return new CommandData(new AreaRef(cellData.getCellRef(), lastCell),  command);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, String> parseCommandAttributes(String attrString) {
        Map<String,String> attrMap = new LinkedHashMap<String, String>();
        Matcher attrMatcher = ATTR_REGEX_PATTERN.matcher(attrString);
        while(attrMatcher.find()){
            String attrData = attrMatcher.group();
            int attrNameEndIndex = attrData.indexOf("=");
            String attrName = attrData.substring(0, attrNameEndIndex).trim();
            String attrValuePart = attrData.substring(attrNameEndIndex + 1).trim();
            String attrValue = attrValuePart.substring(1, attrValuePart.length() - 1);
            attrMap.put(attrName, attrValue);
        }
        return attrMap;
    }

}