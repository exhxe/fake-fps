package com.fakefps;

import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

/** OptiLikeのビデオ設定から、このMODの設定画面を開くための入口(OptiLikeが無くても害はありません)。 */
public class OptiLikeBridge implements Function<Object, Object> {
	@Override
	public Object apply(Object parent) {
		return new FakeFpsScreen(parent instanceof Screen ? (Screen) parent : null);
	}
}
