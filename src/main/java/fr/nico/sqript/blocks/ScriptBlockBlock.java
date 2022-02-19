package fr.nico.sqript.blocks;

import fr.nico.sqript.ScriptManager;
import fr.nico.sqript.compiling.ScriptException;
import fr.nico.sqript.compiling.ScriptToken;
import fr.nico.sqript.forge.SqriptForge;
import fr.nico.sqript.meta.Block;
import fr.nico.sqript.meta.Feature;
import fr.nico.sqript.structures.Side;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

@Block(
        feature = @Feature(name = "Block",
                description = "Define a Minecraft block that will be added to the game.",
                examples = "block my_block:",
                regex = "^block .*",
                side = Side.BOTH),

        fields = {
                @Feature(name = "name"),
                @Feature(name = "texture"),
                @Feature(name = "creative tab"),
                @Feature(name = "harvest level"),
                @Feature(name = "drop"),
                @Feature(name = "variants"),
                @Feature(name = "hardness"),
        },
        reloadable = false
)
public class ScriptBlockBlock extends ScriptBlock {
    String variant = null;


    public ScriptBlockBlock(ScriptToken head) throws ScriptException {
        super(head);
    }

    @Override
    protected void load() throws Exception {
        String registryName = getHead().getText().replaceFirst("block\\s+(.*)", "$1").replaceAll(":","").replaceAll(" ","_").trim();
        String texture = "";
        CreativeTabs tab = CreativeTabs.MISC;
        String displayName;
        String drop = null;
        int harvestLevel = 1;
        float hardness = 6f;
        Material material = Material.ROCK;
        net.minecraft.block.Block e;

        if(!fieldDefined("name"))
            throw new ScriptException.ScriptMissingFieldException(this.getLine(),"item","name");
        if(!fieldDefined("texture") && !fieldDefined("model"))
            throw new ScriptException.ScriptMissingFieldException(this.getLine(),"item","texture or model");

        displayName = getSubBlock("name").getRawContent();

        if(fieldDefined("texture"))
            texture = getSubBlock("texture").getRawContent().replaceAll("\\.png","");
        if(fieldDefined("creative tab"))
            tab = loadTabFromName(getSubBlock("creative tab").getRawContent());
        if(fieldDefined("harvest level"))
            harvestLevel = Integer.parseInt(getSubBlock("harvest level").getRawContent());
        if(fieldDefined("drop"))
            drop = getSubBlock("drop").getRawContent();
        if(fieldDefined("hardness"))
            hardness = Float.parseFloat(getSubBlock("hardness").getRawContent());
        if(fieldDefined("variants")) {
            variant = getSubBlock("variants").getRawContent();
        }

        //System.out.println("tgggggggggggggggggggggggggggggggggggggg");
        //System.out.println("ITEM TYPE FIELD IS NOT DEFINED");
        fr.nico.sqript.forge.common.ScriptBlock.ScriptBlock block = new fr.nico.sqript.forge.common.ScriptBlock.ScriptBlock(material,drop,harvestLevel);
        block.setRegistryName(this.getHead().getScriptInstance().getName(),registryName);
        texture = this.getHead().getScriptInstance().getName()+":"+texture;
        block.setUnlocalizedName(registryName);
        block.setHardness(hardness);
        block.setCreativeTab(tab);

        SqriptForge.blocks.add(block);

        createBlock(registryName, texture);
        createBlockState(registryName,texture);
        createItemBlockModel(registryName,texture);


        Item itemBlock = new ItemBlock(block){
            @Override
            public String getItemStackDisplayName(ItemStack stack) {
                return displayName;
            }
        }.setUnlocalizedName(block.getUnlocalizedName()).setRegistryName(block.getRegistryName());
        SqriptForge.items.add(itemBlock);
        if(registryName.equalsIgnoreCase("granite_1")) {
            variant = "stair";
        }
        if(variant !=null) {
            if (variant.contains("stair")) {
                e = new fr.nico.sqript.forge.common.ScriptBlock.ScriptBlockStair(block.getDefaultState(), material);
                e.setRegistryName(this.getHead().getScriptInstance().getName(), registryName + "_stair");
                texture = this.getHead().getScriptInstance().getName() + ":" + texture;
                e.setHardness(hardness);
                e.setCreativeTab(tab);

                //System.out.println("Max stack size of item is : "+maxStackSize);
                SqriptForge.blocks.add(e);

                createBlock(registryName , texture);
                createBlockState(registryName + "_stair", texture);
                createItemBlockModel(registryName + "_stair", texture);
                Item tt = new ItemBlock(e) {
                    @Override
                    public String getItemStackDisplayName(ItemStack stack) {
                        return displayName + " Stair";
                    }
                }.setUnlocalizedName(e.getUnlocalizedName() + "_stair").setRegistryName(e.getRegistryName());
                SqriptForge.items.add(tt);
            }
        }

    }

    private void createBlock(String registryName, String texture) throws Exception {
        //Creating the model/blockstate file if the model file was not specified
        //System.out.println("Creating model file for item : "+registryName);
        if(!fieldDefined("model")){
            //File scriptFile
            File mainFolder = new File(ScriptManager.scriptDir,this.getHead().getScriptInstance().getName());
            if(!mainFolder.exists())
                mainFolder.mkdir();

            File itemModelsFolder = new File(mainFolder,"/models/block");
            if(!itemModelsFolder.exists())
                itemModelsFolder.mkdirs();
            //Il faut créer le dossier et l'enregistrer à la main
            if(variant==null) {
                String parent = "minecraft:block/cube_all";
                File jsonFile = new File(itemModelsFolder, registryName + ".json");
                String content = ("{" + "\n"
                        + "  'parent': '" + parent + "'," + "\n"
                        + "  'textures': {" + "\n"
                        + "      'all': '" + texture + "'" + "\n"
                        + "  }" + "\n"
                        + "}").replaceAll("'", "\"");
                createFile(jsonFile, content);
            } else{
                String parent = "minecraft:block/stairs";
                File jsonFile = new File(itemModelsFolder, registryName + ".json");
                String content = ("{" + "\n"
                        + "  'parent': '" + parent + "'," + "\n"
                        + "  'textures': {" + "\n"
                        + "      'bottom': '" +this.getHead().getScriptInstance().getName()+":"+registryName+ "'," + "\n"
                        + "      'top': '" +this.getHead().getScriptInstance().getName()+":"+registryName+ "'," + "\n"
                        + "      'side': '" +this.getHead().getScriptInstance().getName()+":"+registryName+ "'" + "\n"
                        + "  }" + "\n"
                        + "}").replaceAll("'", "\"");
                createFile(jsonFile, content);
            }
        }
    }

    private void createBlockState(String registryName, String texture) throws Exception {
        //Creating the model/blockstate file if the model file was not specified
        //System.out.println("Creating model file for item : "+registryName);
        if(!fieldDefined("model")){
            //File scriptFile
            File mainFolder = new File(ScriptManager.scriptDir,this.getHead().getScriptInstance().getName());
            if(!mainFolder.exists())
                mainFolder.mkdir();

            File itemModelsFolder = new File(mainFolder,"/blockstates/");
            if(!itemModelsFolder.exists())
                itemModelsFolder.mkdirs();
            //Il faut créer le dossier et l'enregistrer à la main
            File jsonFile = new File(itemModelsFolder,registryName+".json");
            String content = ("{"+"\n"
                    +"    \"variants\": {\n"
                    +"        \"normal\": [\n"
                    +"            { \"model\": \""+this.getHead().getScriptInstance().getName()+":"+registryName+"\" }\n"
                    +"        ],\n"
                    +"        \"facing=east,half=bottom,shape=straight\":  { \"model\": \""+registryName+"\" },\n"
                    +"        \"facing=west,half=bottom,shape=straight\":  { \"model\": \""+registryName+"\", \"y\": 180, \"uvlock\": true },\n"
                    +"        \"facing=south,half=bottom,shape=straight\": { \"model\": \""+registryName+"\", \"y\": 90, \"uvlock\": true },\n"
                    +"        \"facing=north,half=bottom,shape=straight\": { \"model\": \""+registryName+"\", \"y\": 270, \"uvlock\": true },\n"
                    +"        \"facing=east,half=bottom,shape=outer_right\":  { \"model\": \""+registryName+"\" },\n"
                    +"        \"facing=west,half=bottom,shape=outer_right\":  { \"model\": \""+registryName+"\", \"y\": 180, \"uvlock\": true },\n"
                    +"        \"facing=south,half=bottom,shape=outer_right\": { \"model\": \""+registryName+"\", \"y\": 90, \"uvlock\": true },\n"
                    +"        \"facing=north,half=bottom,shape=outer_right\": { \"model\": \""+registryName+"\", \"y\": 270, \"uvlock\": true },\n"
                    +"        \"facing=east,half=bottom,shape=outer_left\":  { \"model\": \""+registryName+"\", \"y\": 270, \"uvlock\": true },\n"
                    +"        \"facing=west,half=bottom,shape=outer_left\":  { \"model\": \""+registryName+"\", \"y\": 90, \"uvlock\": true },\n"
                    +"        \"facing=south,half=bottom,shape=outer_left\": { \"model\": \""+registryName+"\" },\n"
                    +"        \"facing=north,half=bottom,shape=outer_left\": { \"model\": \""+registryName+"\", \"y\": 180, \"uvlock\": true },\n"
                    +"        \"facing=east,half=bottom,shape=inner_right\":  { \"model\": \""+registryName+"\" },\n"
                    +"        \"facing=west,half=bottom,shape=inner_right\":  { \"model\": \""+registryName+"\", \"y\": 180, \"uvlock\": true },\n"
                    +"        \"facing=south,half=bottom,shape=inner_right\": { \"model\": \""+registryName+"\", \"y\": 90, \"uvlock\": true },\n"
                    +"        \"facing=north,half=bottom,shape=inner_right\": { \"model\": \""+registryName+"\", \"y\": 270, \"uvlock\": true },\n"
                    +"        \"facing=east,half=bottom,shape=inner_left\":  { \"model\": \""+registryName+"\", \"y\": 270, \"uvlock\": true },\n"
                    +"        \"facing=west,half=bottom,shape=inner_left\":  { \"model\": \""+registryName+"\", \"y\": 90, \"uvlock\": true },\n"
                    +"        \"facing=south,half=bottom,shape=inner_left\": { \"model\": \""+registryName+"\" },\n"
                    +"        \"facing=north,half=bottom,shape=inner_left\": { \"model\": \""+registryName+"\", \"y\": 180, \"uvlock\": true },\n"
                    +"        \"facing=east,half=top,shape=straight\":  { \"model\": \""+registryName+"\", \"x\": 180, \"uvlock\": true },\n"
                    +"        \"facing=west,half=top,shape=straight\":  { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 180, \"uvlock\": true },\n"
                    +"        \"facing=south,half=top,shape=straight\": { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 90, \"uvlock\": true },\n"
                    +"        \"facing=north,half=top,shape=straight\": { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 270, \"uvlock\": true },\n"
                    +"        \"facing=east,half=top,shape=outer_right\":  { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 90, \"uvlock\": true },\n"
                    +"        \"facing=west,half=top,shape=outer_right\":  { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 270, \"uvlock\": true },\n"
                    +"        \"facing=south,half=top,shape=outer_right\": { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 180, \"uvlock\": true },\n"
                    +"        \"facing=north,half=top,shape=outer_right\": { \"model\": \""+registryName+"\", \"x\": 180, \"uvlock\": true },\n"
                    +"        \"facing=east,half=top,shape=outer_left\":  { \"model\": \""+registryName+"\", \"x\": 180, \"uvlock\": true },\n"
                    +"        \"facing=west,half=top,shape=outer_left\":  { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 180, \"uvlock\": true },\n"
                    +"        \"facing=south,half=top,shape=outer_left\": { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 90, \"uvlock\": true },\n"
                    +"        \"facing=north,half=top,shape=outer_left\": { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 270, \"uvlock\": true },\n"
                    +"        \"facing=east,half=top,shape=inner_right\":  { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 90, \"uvlock\": true },\n"
                    +"        \"facing=west,half=top,shape=inner_right\":  { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 270, \"uvlock\": true },\n"
                    +"        \"facing=south,half=top,shape=inner_right\": { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 180, \"uvlock\": true },\n"
                    +"        \"facing=north,half=top,shape=inner_right\": { \"model\": \""+registryName+"\", \"x\": 180, \"uvlock\": true },\n"
                    +"        \"facing=east,half=top,shape=inner_left\":  { \"model\": \""+registryName+"\", \"x\": 180, \"uvlock\": true },\n"
                    +"        \"facing=west,half=top,shape=inner_left\":  { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 180, \"uvlock\": true },\n"
                    +"        \"facing=south,half=top,shape=inner_left\": { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 90, \"uvlock\": true },\n"
                    +"        \"facing=north,half=top,shape=inner_left\": { \"model\": \""+registryName+"\", \"x\": 180, \"y\": 270, \"uvlock\": true }\n"
                    +"    }\n"
                    +"}"
            ).replaceAll("'","\"");
            createFile(jsonFile,content);
        }
    }


    private void createItemBlockModel(String registryName, String texture) throws Exception {
        //Creating the model/blockstate file if the model file was not specified
        //System.out.println("Creating model file for item : "+registryName);
        if(!fieldDefined("model")){
            //File scriptFile
            File mainFolder = new File(ScriptManager.scriptDir,this.getHead().getScriptInstance().getName());
            if(!mainFolder.exists())
                mainFolder.mkdir();

            File itemModelsFolder = new File(mainFolder,"/models/item");
            if(!itemModelsFolder.exists())
                itemModelsFolder.mkdirs();
            //Il faut créer le dossier et l'enregistrer à la main
            File jsonFile = new File(itemModelsFolder,registryName+".json");
            String content = ("{"+"\n"
                    +"  'parent': '"+this.getHead().getScriptInstance().getName()+":block/"+registryName+"'\n"
                    +"}").replaceAll("'","\"");
            createFile(jsonFile,content);
        }
    }

    private CreativeTabs loadTabFromName(String creative_tab) {
        if(creative_tab.equalsIgnoreCase("building blocks") || creative_tab.equalsIgnoreCase("blocks"))
            return CreativeTabs.BUILDING_BLOCKS;
        for(CreativeTabs tab : CreativeTabs.CREATIVE_TAB_ARRAY){
            if(tab.getTabLabel().replaceAll("_"," ").equalsIgnoreCase(creative_tab) || creative_tab.startsWith(tab.getTabLabel()))
                return tab;
        }
        //System.out.println("Returning null for tab");
        return null;
    }


    private void createFile(File file, String contents) throws Exception {

        if (file.exists()) {
            return;
        }
        //System.out.println("Creating file : "+file.getAbsolutePath());
        file.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(contents);
        out.close();
    }



}







