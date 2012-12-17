package com.vercer.leaf;

import lombok.Getter;

import com.vercer.leaf.exchange.Decorator;
import com.vercer.leaf.exchange.Label;

public class MyDecorator extends Decorator
{
	@Getter
	Label label = new Label("there!");
}
