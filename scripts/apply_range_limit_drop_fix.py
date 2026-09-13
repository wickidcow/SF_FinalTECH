from pathlib import Path

setup_path = Path("src/main/java/io/taraxacum/finaltech/setup/SetupUtil.java")
ticker_path = Path("src/main/java/io/taraxacum/finaltech/util/BlockTickerUtil.java")

setup = setup_path.read_text(encoding="utf-8")
old_warning = '''                            if (dropSelf) {
                                FinalTechChanged.logger().warning("Be careful if you have the Slimefun addon 'BedrockTechnology' installed and set drop-self to true.");
                                FinalTechChanged.logger().warning("There is a duplication bug, and we may fix it in next version");
                            }
'''
if old_warning not in setup:
    raise SystemExit("Expected BedrockTechnology warning block was not found")
setup = setup.replace(old_warning, "", 1)
setup_path.write_text(setup, encoding="utf-8")

ticker = ticker_path.read_text(encoding="utf-8")
old_drop = '''                                if (canBreak) {
                                    BlockStorage.clearBlockInfo(block);
                                    block.setType(Material.AIR);
                                    if (item instanceof MachineProcessHolder machineProcessHolder) {
                                        machineProcessHolder.getMachineProcessor().endOperation(block);
                                    }
                                    if (dropSelf && item.getId().equals(BlockStorage.getLocationInfo(block.getLocation(), ConstantTableUtil.CONFIG_ID))) {
                                        block.getLocation().getWorld().dropItem(block.getLocation(), ItemStackUtil.cloneItem(item.getItem(), 1));
                                    }
'''
new_drop = '''                                if (canBreak) {
                                    String storedItemId = BlockStorage.getLocationInfo(location, ConstantTableUtil.CONFIG_ID);
                                    boolean shouldDropSelf = dropSelf && item.getId().equals(storedItemId);

                                    if (item instanceof MachineProcessHolder machineProcessHolder) {
                                        machineProcessHolder.getMachineProcessor().endOperation(block);
                                    }
                                    BlockStorage.clearBlockInfo(block);
                                    block.setType(Material.AIR);

                                    if (shouldDropSelf) {
                                        world.dropItem(location, ItemStackUtil.cloneItem(item.getItem(), 1));
                                    }
'''
if old_drop not in ticker:
    raise SystemExit("Expected legacy range-limit drop block was not found")
ticker = ticker.replace(old_drop, new_drop, 1)
ticker_path.write_text(ticker, encoding="utf-8")

print("Applied FinalTECH range-limit drop-self fix and removed stale warning spam.")
