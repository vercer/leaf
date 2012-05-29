package com.vercer.jet;

import com.vercer.jet.transform.Decorator;
import com.vercer.jet.transform.Label;

import lombok.Getter;

public class MyDecorator extends Decorator
{
	@Getter
	Label label = new Label("there!");
}
