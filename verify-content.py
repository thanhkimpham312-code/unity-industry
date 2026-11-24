#!/usr/bin/env python3
"""Content/asset integrity smoke checks for Unity Industry V2.
Does NOT verify in-game skills, player energy, Mob spawns, or runtime crafting.
"""
from pathlib import Path
import json, struct
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'src/main'
asset=SRC/'resources/assets/unityindustry'
items=['unity_ingot','tough_stick','prime_core','ultra_core',
 'fire_core','flying_core','ore_scanner_core','excavation_core','magnet_core',
 'pressure_core','agriculture_core','refinery_core','storage_core',
 'maintenance_core','unity_helmet','unity_chestplate','unity_leggings','unity_boots']
for name in items:
 png=asset/'textures/item'/f'{name}.png'
 assert png.exists(),png
 b=png.read_bytes()
 assert b[:8]==b'\x89PNG\r\n\x1a\n',png
 w,h=struct.unpack('>II',b[16:24]);assert w==h and w in (16,32,64,128), (name,w,h)
 for sub in ('items','models/item'):
  j=asset/sub/f'{name}.json';assert j.exists(),j
  json.loads(j.read_text())
for name in ['industry_crafting_table','research_desk','implant_table','reverse_implant_table']:
 for face in ('side','top','bottom'):
  png=asset/'textures/block'/f'{name}_{face}.png'
  assert png.exists(),png
 for section in ('models/block','blockstates','items'):
  j=asset/section/f'{name}.json'
  assert j.exists(),j
  json.loads(j.read_text())
for name in ('industry','research'):
 png=asset/'textures/gui'/f'{name}.png'
 b=png.read_bytes();assert struct.unpack('>II',b[16:24])==(256,256)
# Both the inventory sprites and the actual equipped-model UV atlases must exist.
for layer in ('humanoid', 'humanoid_leggings'):
 png=asset/'textures/entity/equipment'/layer/'unity.png'
 b=png.read_bytes()
 assert b[:8]==b'\x89PNG\r\n\x1a\n',png
 assert struct.unpack('>II',b[16:24]) in ((64,32),(128,64)),(png,'expected 64x32 or scaled 128x64 UV atlas')
# Regression guard for the already-seen 1.21.11 Mojang mappings compiler error.
screen=(SRC/'java/com/unityindustry/client/StationScreen.java').read_text()
assert 'import net.minecraft.client.renderer.RenderPipelines;' in screen
assert 'import net.minecraft.client.gui.RenderPipelines;' not in screen
assert screen.count('import net.minecraft.client.renderer.RenderPipelines;') == 1
for f in (SRC/'resources').rglob('*.json'):
 json.loads(f.read_text(encoding='utf-8'))
# Guard against repeating 0.1: those 2 working stations must actually have source handlers.
for f in ['block/StationBlock.java','block/WideStationBlock.java','menu/StationMenu.java', 'menu/StationCrafting.java', 'menu/ImplantMenu.java', 'gameplay/UnityEnergy.java', 'gameplay/ChestplateCores.java', 'gameplay/UnityPowers.java', 'registry/CoreComponents.java', 'network/UnityActionPayload.java']:
 assert (SRC/'java/com/unityindustry'/f).exists(),f
code=(SRC/'java/com/unityindustry/menu/StationCrafting.java').read_text()
for marker in ['ModItems.UNITY_INGOT','ModItems.ULTRA_CORE',
               'Items.COPPER_INGOT','Items.NETHER_STAR','Items.ELYTRA','ModItems.UNITY_CHESTPLATE']:
 assert marker in code,marker
assert not (SRC/'resources/data/unityindustry/recipe/unity_ingot.json').exists(), 'Do not leak Industry-only alloy recipe into Vanilla'
print('PASS: all item/block PNGs, both armor UV atlases, Java source presence, JSON and station recipes')

# Real 3D geometry: two physical block halves, not a preview image or a cube.
for name in ('research_desk','implant_table','reverse_implant_table'):
    states=json.loads((asset/'blockstates'/f'{name}.json').read_text())['variants']
    assert len(states)==8, (name,'four facings by two halves')
    for half in ('left','right'):
        model=asset/'models/block'/f'{name}_{half}.json'
        obj=json.loads(model.read_text()); elements=obj.get('elements',[])
        assert len(elements)>=8,(model,'not actual 3D elements')
        assert max(v['to'][1] for v in elements)>16,(model,'no raised features')
# Dynamic slots must be derived from installed core list, not fixed slot=2.
keys=(SRC/'java/com/unityindustry/client/UnityKeys.java').read_text()
powers=(SRC/'java/com/unityindustry/gameplay/UnityPowers.java').read_text()
assert 'SkillCatalog.active' in keys and 'SkillCatalog.at' in powers
assert 'FLIGHT_COOLDOWN_UNTIL' not in powers
assert (ROOT/'tools/CoreArmorAssetGenerator.java').exists()
print('PASS: raised 3D models, 2-part state mapping, dynamic skill slots and no flight cooldown')
