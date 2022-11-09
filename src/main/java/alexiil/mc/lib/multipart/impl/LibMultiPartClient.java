/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package alexiil.mc.lib.multipart.impl;

import java.util.function.Consumer;

import alexiil.mc.lib.multipart.impl.client.render.MultipartOutlineRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import alexiil.mc.lib.multipart.impl.client.model.MultipartModel;
import alexiil.mc.lib.multipart.impl.client.render.MultipartBlockEntityRenderer;

public class LibMultiPartClient implements ClientModInitializer {

    public static final ModelIdentifier MODEL_IDENTIFIER
        = new ModelIdentifier(new Identifier(LibMultiPart.NAMESPACE, "container"), "");

    @Override
    public void onInitializeClient() {
        LibMultiPart.isWorldClientPredicate = w -> w != null && w == MinecraftClient.getInstance().world;
        LibMultiPart.partialTickGetter = MinecraftClient.getInstance()::getTickDelta;
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(res -> varProvider());
        ModelLoadingRegistry.INSTANCE.registerModelProvider(LibMultiPartClient::requestModels);
        BlockRenderLayerMap.INSTANCE.putBlock(LibMultiPart.BLOCK, RenderLayer.getCutout());
        BlockEntityRendererRegistry.INSTANCE.register(LibMultiPart.BLOCK_ENTITY, MultipartBlockEntityRenderer::new);
        WorldRenderEvents.BLOCK_OUTLINE.register(MultipartOutlineRenderer.INSTANCE);

        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (be instanceof MultipartBlockEntity multipart) {
                    multipart.onChunkUnload();
                }
            }
        });
    }

    private static ModelVariantProvider varProvider() {
        return new ModelVariantProvider() {

            MultipartModel.Unbaked ubaked = null;

            @Override
            public UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context)
                throws ModelProviderException {

                if (modelId.getNamespace().equals(MODEL_IDENTIFIER.getNamespace())) {
                    if (modelId.getPath().equals(MODEL_IDENTIFIER.getPath())) {
                        return ubaked == null ? (ubaked = new MultipartModel.Unbaked()) : ubaked;
                    }
                }
                return null;
            }
        };
    }

    private static void requestModels(ResourceManager res, Consumer<Identifier> out) {
        out.accept(MODEL_IDENTIFIER);
    }
}
