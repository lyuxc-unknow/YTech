package com.yanny.ytech.configuration.renderer;

import com.yanny.ytech.configuration.Utils;
import com.yanny.ytech.configuration.entity.WoollyMammothEntity;
import com.yanny.ytech.configuration.model.WoollyMammothModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class WoollyMammothRenderer extends MobRenderer<WoollyMammothEntity, WoollyMammothModel> {
    public WoollyMammothRenderer(@NotNull EntityRendererProvider.Context context) {
        super(context, new WoollyMammothModel(context.bakeLayer(WoollyMammothModel.LAYER_LOCATION)), 0.7f);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull WoollyMammothEntity entity) {
        return Utils.modLoc("textures/entity/woolly_mammoth.png");
    }
}
