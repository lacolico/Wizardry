package electroblob.wizardry.misc;

import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.oredict.OreDictionary;

/** Custom version of {@link MerchantRecipeList} which allows wildcard recipes (i.e. trades which accept items with any
 * damage value). Function is otherwise identical. For some reason this feature was removed in 1.11.
 * @author Electroblob
 * @since Wizardry 4.1 */
@SuppressWarnings("serial")
public class WildcardTradeList extends MerchantRecipeList {

	public WildcardTradeList(){
		super();
	}
	
	public WildcardTradeList(NBTTagCompound tag){
		super(tag);
	}

	@Override
    public MerchantRecipe canRecipeBeUsed(ItemStack offer1, ItemStack offer2, int index){
		
        if(index > 0 && index < this.size()){
        	
            MerchantRecipe merchantrecipe1 = (MerchantRecipe)this.get(index);
            return !this.areItemStacksExactlyEqual(offer1, merchantrecipe1.getItemToBuy()) || (!offer2.isEmpty() || merchantrecipe1.hasSecondItemToBuy()) && (!merchantrecipe1.hasSecondItemToBuy() || !this.areItemStacksExactlyEqual(offer2, merchantrecipe1.getSecondItemToBuy())) || offer1.getCount() < merchantrecipe1.getItemToBuy().getCount() || merchantrecipe1.hasSecondItemToBuy() && offer2.getCount() < merchantrecipe1.getSecondItemToBuy().getCount() ? null : merchantrecipe1;
        
        }else{
        	
            for(int i = 0; i < this.size(); ++i){
            	
                MerchantRecipe merchantrecipe = (MerchantRecipe)this.get(i);

                if (this.areItemStacksExactlyEqual(offer1, merchantrecipe.getItemToBuy()) && offer1.getCount() >= merchantrecipe.getItemToBuy().getCount() && (!merchantrecipe.hasSecondItemToBuy() && offer2.isEmpty() || merchantrecipe.hasSecondItemToBuy() && this.areItemStacksExactlyEqual(offer2, merchantrecipe.getSecondItemToBuy()) && offer2.getCount() >= merchantrecipe.getSecondItemToBuy().getCount())){
                    return merchantrecipe;
                }
            }
            
            return null;
        }
    }

    private boolean areItemStacksExactlyEqual(ItemStack stack1, ItemStack stack2){
    	// Added to allow wildcards
    	if((stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE || stack2.getItemDamage() == OreDictionary.WILDCARD_VALUE)
    			// Can't use ItemStack.areItemsEqualIgnoreDurability because that only works for items with durability, not subtypes.
    			&& stack1.getItem() == stack2.getItem()) return true;
    	
        return ItemStack.areItemsEqual(stack1, stack2) && (!stack2.hasTagCompound() || stack1.hasTagCompound() && NBTUtil.areNBTEquals(stack2.getTagCompound(), stack1.getTagCompound(), false));
    }

	@Override
	public void writeToBuf(PacketBuffer buffer){

		buffer.writeByte((byte)(this.size() & 255));

		// Trick the client into thinking this is a normal item
		for(MerchantRecipe merchantrecipe : this){

			ItemStack itemToBuy = merchantrecipe.getItemToBuy();
			if(itemToBuy.getMetadata() == OreDictionary.WILDCARD_VALUE) itemToBuy = WizardryUtilities.copyWithMeta(itemToBuy, 0);
			buffer.writeItemStack(itemToBuy);

			ItemStack itemToSell = merchantrecipe.getItemToSell();
			if(itemToSell.getMetadata() == OreDictionary.WILDCARD_VALUE) itemToSell = WizardryUtilities.copyWithMeta(itemToSell, 0);
			buffer.writeItemStack(itemToSell);

			ItemStack secondItemToBuy = merchantrecipe.getSecondItemToBuy();
			buffer.writeBoolean(!secondItemToBuy.isEmpty());

			if(!secondItemToBuy.isEmpty()){
				if(secondItemToBuy.getMetadata() == OreDictionary.WILDCARD_VALUE) secondItemToBuy = WizardryUtilities.copyWithMeta(secondItemToBuy, 0);
				buffer.writeItemStack(secondItemToBuy);
			}

			buffer.writeBoolean(merchantrecipe.isRecipeDisabled());
			buffer.writeInt(merchantrecipe.getToolUses());
			buffer.writeInt(merchantrecipe.getMaxTradeUses());
		}
	}
	
}
