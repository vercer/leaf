package com.vercer.leaf;

import com.vercer.leaf.exchange.Decorator;
import com.vercer.leaf.exchange.Label;

import lombok.Getter;

public class MyDecorator extends Decorator
{
	@Getter
	Label label = new Label("there!");
}
