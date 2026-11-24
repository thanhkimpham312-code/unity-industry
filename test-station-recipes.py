#!/usr/bin/env python3
"""Compiles the ACTUAL StationCrafting.java with miniature Minecraft type stubs.
Verifies matching rules but does not test the Fabric menu or actual game runtime.
"""
from pathlib import Path
import subprocess, tempfile, re
root=Path(__file__).resolve().parents[1]
source=root/'src/main/java/com/unityindustry/menu/StationCrafting.java'
code=source.read_text()
items=sorted(set(re.findall(r'Items\.([A-Z_]+)',code)))
mods=sorted(set(re.findall(r'ModItems\.([A-Z_]+)',code)))
with tempfile.TemporaryDirectory() as tmp:
 p=Path(tmp)/'src';p.mkdir()
 def make(path,text):
  path=p/path;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text);return str(path)
 paths=[]
 paths.append(make(Path('net/minecraft/world/item/Item.java'), 'package net.minecraft.world.item; public class Item {}'))
 paths.append(make(Path('net/minecraft/world/item/ItemStack.java'), '''package net.minecraft.world.item;
 public class ItemStack {
   public static final ItemStack EMPTY = new ItemStack((Item)null);
   private final Item item;
   public ItemStack(Item item) { this.item=item; }
   public boolean isEmpty(){return item==null || item==Items.AIR;}
   public Item getItem(){return item;}
 }'''))
 paths.append(make(Path('net/minecraft/world/item/crafting/Ingredient.java'), 'package net.minecraft.world.item.crafting; public class Ingredient {}'))
 paths.append(make(Path('net/minecraft/world/Container.java'),'''package net.minecraft.world;
 import net.minecraft.world.item.ItemStack;
 public interface Container { ItemStack getItem(int index); }'''))
 paths.append(make(Path('net/minecraft/world/item/Items.java'),'package net.minecraft.world.item; public class Items {'+''.join('public static final Item '+x+'=new Item();' for x in items)+'}'))
 paths.append(make(Path('com/unityindustry/registry/ModItems.java'),'package com.unityindustry.registry; import net.minecraft.world.item.Item; public class ModItems {'+''.join('public static final Item '+x+'=new Item();' for x in mods)+'}'))
 paths.append(make(Path('com/unityindustry/registry/ModBlocks.java'),'package com.unityindustry.registry; import net.minecraft.world.item.Item; public class ModBlocks { public static final Item RESEARCH_DESK=new Item(), IMPLANT_TABLE=new Item(); }'))
 paths.append(make(Path('com/unityindustry/menu/StationMenu.java'),'package com.unityindustry.menu; public class StationMenu {public enum Station {INDUSTRY, RESEARCH}}'))
 paths.append(make(Path('Test.java'),'''import com.unityindustry.menu.StationCrafting;
import com.unityindustry.menu.StationMenu;
import com.unityindustry.registry.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
public class Test {
 static class C implements Container {
   ItemStack[] a;
   C(int n){a=new ItemStack[n]; for(int i=0;i<n;i++) a[i]=ItemStack.EMPTY;}
   public ItemStack getItem(int i){return a[i];}
   C put(int i,Item it){a[i]=new ItemStack(it);return this;}
 }
 static void expect(Item expected, ItemStack found,String why){
   if (found.isEmpty() || found.getItem()!=expected)throw new AssertionError(why);
 }
 static void none(ItemStack found,String why){
   if (!found.isEmpty())throw new AssertionError(why);
 }
 public static void main(String[] args) {
   Item[] alloy={Items.COPPER_INGOT,Items.IRON_INGOT,Items.GOLD_INGOT};
   int count=0;
   for(int row=0;row<3;row++) for(int a=0;a<3;a++)for(int b=0;b<3;b++){
      if(a==b)continue;
      int c=3-a-b;
      C x=new C(9).put(row*3,alloy[a]).put(row*3+1,alloy[b]).put(row*3+2,alloy[c]);
      expect(ModItems.UNITY_INGOT,StationCrafting.result(StationMenu.Station.INDUSTRY,x),"alloy combination "+row+a+b);count++;
   }
   none(StationCrafting.result(StationMenu.Station.INDUSTRY,new C(9).put(0,Items.GOLD_INGOT)),"should reject unmatched");
   expect(ModItems.TOUGH_STICK,StationCrafting.result(StationMenu.Station.INDUSTRY,
     new C(9).put(4,Items.OBSIDIAN).put(7,Items.OBSIDIAN)),"Tough Stick");
   expect(ModItems.PRIME_CORE,StationCrafting.result(StationMenu.Station.INDUSTRY,
     new C(9).put(1,ModItems.UNITY_INGOT).put(3,ModItems.UNITY_INGOT).put(4,Items.NETHER_STAR)
     .put(5,ModItems.UNITY_INGOT).put(7,ModItems.UNITY_INGOT)),"Prime Core");
   expect(ModBlocks.RESEARCH_DESK,StationCrafting.result(StationMenu.Station.INDUSTRY,
     new C(9).put(0,Items.IRON_INGOT).put(1,Items.BOOK).put(2,Items.IRON_INGOT)
     .put(3,Items.REDSTONE).put(4,Items.OBSERVER).put(5,Items.REDSTONE)
     .put(6,Items.COPPER_INGOT).put(7,Items.GLASS).put(8,Items.COPPER_INGOT)),"Research Desk");
   // All diamond-shaped Unity armor variants are produced only in the industrial station.
   Item U=ModItems.UNITY_INGOT;
   expect(ModItems.UNITY_HELMET, StationCrafting.result(StationMenu.Station.INDUSTRY,
       new C(9).put(0,U).put(1,U).put(2,U).put(3,U).put(5,U)), "helmet top rows");
   expect(ModItems.UNITY_HELMET, StationCrafting.result(StationMenu.Station.INDUSTRY,
       new C(9).put(3,U).put(4,U).put(5,U).put(6,U).put(8,U)), "helmet lower rows");
   expect(ModItems.UNITY_CHESTPLATE, StationCrafting.result(StationMenu.Station.INDUSTRY,
       new C(9).put(0,U).put(2,U).put(3,U).put(4,U).put(5,U).put(6,U).put(7,U).put(8,U)), "chestplate");
   expect(ModItems.UNITY_LEGGINGS, StationCrafting.result(StationMenu.Station.INDUSTRY,
       new C(9).put(0,U).put(1,U).put(2,U).put(3,U).put(5,U).put(6,U).put(8,U)), "leggings");
   expect(ModItems.UNITY_BOOTS, StationCrafting.result(StationMenu.Station.INDUSTRY,
       new C(9).put(0,U).put(2,U).put(3,U).put(5,U)), "boots top rows");
   expect(ModItems.UNITY_BOOTS, StationCrafting.result(StationMenu.Station.INDUSTRY,
       new C(9).put(3,U).put(5,U).put(6,U).put(8,U)), "boots lower rows");
   expect(ModItems.ULTRA_CORE,StationCrafting.result(StationMenu.Station.RESEARCH,
     new C(2).put(0,ModItems.PRIME_CORE).put(1,ModItems.PRIME_CORE)),"Ultra Core");
   Item[][] sigs={
    {Items.MAGMA_CREAM,ModItems.FIRE_CORE}, {Items.ELYTRA,ModItems.FLYING_CORE},
    {Items.ECHO_SHARD,ModItems.ORE_SCANNER_CORE},{Items.NETHERITE_PICKAXE,ModItems.EXCAVATION_CORE},
    {Items.LODESTONE,ModItems.MAGNET_CORE},{Items.CONDUIT,ModItems.PRESSURE_CORE},
    {Items.BONE_MEAL,ModItems.AGRICULTURE_CORE},{Items.BLAST_FURNACE,ModItems.REFINERY_CORE},
    {Items.SHULKER_SHELL,ModItems.STORAGE_CORE},{Items.ANVIL,ModItems.MAINTENANCE_CORE}};
   for(Item[] sig:sigs){
    expect(sig[1],StationCrafting.result(StationMenu.Station.RESEARCH,
      new C(2).put(0,ModItems.PRIME_CORE).put(1,sig[0])),"Research signature");
    expect(sig[1],StationCrafting.result(StationMenu.Station.RESEARCH,
      new C(2).put(0,sig[0]).put(1,ModItems.PRIME_CORE)),"Research swapped signature");
    count+=2;
   }
   System.out.println("PASS: "+count+" alloy permutations/signature cases, plus station recipes");
 }
}'''))
 cmd=['javac','-encoding','UTF-8','-d',str(Path(tmp)/'classes'),*paths,str(source)]
 r=subprocess.run(cmd,capture_output=True,text=True)
 if r.returncode:raise RuntimeError(r.stderr)
 subprocess.run(['java','-cp',str(Path(tmp)/'classes'),'Test'],check=True)
