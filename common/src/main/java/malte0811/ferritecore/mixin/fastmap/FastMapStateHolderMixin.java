package malte0811.ferritecore.mixin.fastmap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import malte0811.ferritecore.ducks.FastMapStateHolder;
import malte0811.ferritecore.fastmap.FastMap;
import malte0811.ferritecore.impl.StateHolderImpl;
import net.minecraft.state.Property;
import net.minecraft.state.StateHolder;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = StateHolder.class, priority = 900)
public abstract class FastMapStateHolderMixin<O, S> implements FastMapStateHolder<S> {
    @Mutable
    @Shadow
    @Final
    private ImmutableMap<Property<?>, Comparable<?>> properties;

    @Shadow
    private Table<Property<?>, Comparable<?>, S> field_235894_e_;
    private int globalTableIndex;
    private FastMap<S> globalTable;

    @Redirect(
            method = "with",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Table;get(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            )
    )
    public Object getNeighborFromFastMap(Table<?, ?, ?> ignore, Object rowKey, Object columnKey) {
        return this.globalTable.withUnsafe(this.globalTableIndex, (Property<?>) rowKey, columnKey);
    }

    /**
     * @reason This Mixin completely replaces the data structures initialized by this method, as the original ones waste
     * a lot of memory
     * @author malte0811
     */
    @Overwrite
    public void func_235899_a_(Map<Map<Property<?>, Comparable<?>>, S> states) {
        StateHolderImpl.populateNeighbors(states, this);
    }

    @Inject(method = "func_235899_a_", at = @At("RETURN"), cancellable = true)
    public void func_235899_a_Return(Map<Map<Property<?>, Comparable<?>>, S> states, CallbackInfo ci) {
        ci.cancel();//Hack: Cancel before Hydrogens code runs
    }

    @Override
    public FastMap<S> getStateMap() {
        return globalTable;
    }

    @Override
    public int getStateIndex() {
        return globalTableIndex;
    }

    @Override
    public ImmutableMap<Property<?>, Comparable<?>> getVanillaPropertyMap() {
        return properties;
    }

    @Override
    public void replacePropertyMap(ImmutableMap<Property<?>, Comparable<?>> newMap) {
        properties = newMap;
    }

    @Override
    public void setStateMap(FastMap<S> newValue) {
        globalTable = newValue;
    }

    @Override
    public void setStateIndex(int newValue) {
        globalTableIndex = newValue;
    }
}
