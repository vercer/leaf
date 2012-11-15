package com.vercer.leaf;

import lombok.Getter;

import com.vercer.leaf.exchange.Label;

public class ThreeDeepTestPage extends TestPage2
{
	@Getter
	private Label when = new Label("noon");
}
