NNNT_Knob {
	var <view,
	<value,
	<>morph,
	<>morphIndex,
	<>label,
	<>action,
	clicked, startY;

	*new {|... args|
		^super.new.init(*args);
	}

	init {|a_value = 0.0, a_morph = 0.0, a_morphIndex = 0, a_label = "LABEL"|
		value = a_value;
		morph = a_morph;
		morphIndex = a_morphIndex;
		label = a_label;
		action = {|value| };
		clicked = false;
		view = UserView.new;
		view.background = Color.black;
		view.drawFunc = {
			var font, stringBounds;

			Pen.scale(1, 1.neg);
			Pen.translate(0, view.bounds.height.neg);
			Pen.fillColor = Color.new(0.9, 0.9, 0.8);
			Pen.addAnnularWedge(
				(view.bounds.width / 2) @ (view.bounds.width / 2),
				(view.bounds.width / 2) * 0.8,
				(view.bounds.width / 2) * 0.85,
				(1.25 * pi),
				(1.5.neg * pi)
			);
			Pen.fill;

			Pen.fillColor = [
				Color.new(1, 0.5, 0),
				Color.new(0, 0.5, 0.5),
				Color.new(0.5, 0, 0.5),
				Color.new(0.5, 0.5, 0),
				Color.new(1, 0, 0),
				Color.new(0, 1, 0),
				Color.new(0, 0, 1),
				Color.new(0, 1, 1),
				Color.new(1, 0, 1),
				Color.new(1, 1, 0),
			].at(morphIndex);
			Pen.addAnnularWedge(
				(view.bounds.width / 2) @ (view.bounds.width / 2),
				(view.bounds.width / 2) * 0.9,
				(view.bounds.width / 2) * 0.75,
				(value * 1.5.neg * pi) + (1.25 * pi),
				(morph.clip(value.neg, (1 - value)) * 1.5.neg * pi)
			);
			Pen.fill;

			Pen.fillColor = Color.new(0.9, 0.9, 0.8);
			Pen.addAnnularWedge(
				(view.bounds.width / 2) @ (view.bounds.width / 2),
				(view.bounds.width / 2) * 0.6,
				(view.bounds.width / 2),
				(1.25 * pi) + (value * 1.5.neg * pi) - 0.075,
				0.15
			);
			Pen.fill;

			Pen.scale(1, 1.neg);
			Pen.translate(0, view.bounds.height.neg);
			font = Font.new("Helvetica Neue", 11);
			Pen.font = font;
			stringBounds = GUI.stringBounds(label, font);
			Pen.stringAtPoint(label, 0 @ 0);
			Pen.fill;
		};
		view.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
			var newY;
			if(clicked.not, {
				clicked = true;
				startY = y;
			});
			([256, 0].includes(modifiers)).if({
				newY = 1 - y.linlin(0, view.bounds.height, 0, 1.0);
				if(newY != value,{ this.valueAction(newY) });
				//newY = 1 - (y - startY); //.linlin(0, view.bounds.height, -1.0, 1.0);
			});
			([131072].includes(modifiers)).if({
			newY = 1 - y.linlin(0, view.bounds.height, 0, 2.0);
				if(newY != value,{ this.setMorph(newY) });
			});
		};
		view.mouseUpAction = {
			clicked = false;
		};
		view.mouseMoveAction = view.mouseDownAction;
		view.keyDownAction = {|view, char, modifiers, unicode,keycode|
			if(char == $1, {
				morphIndex = 0;
				this.doAction;
				view.refresh;
			});
			if(char == $2, {
				morphIndex = 1;
				this.doAction;
				view.refresh;
			});
			if(char == $3, {
				morphIndex = 2;
				this.doAction;
				view.refresh;
			});
			if(char == $4, {
				morphIndex = 3;
				this.doAction;
				view.refresh;
			});
			if(char == $5, {
				morphIndex = 4;
				this.doAction;
				view.refresh;
			});
			if(char == $6, {
				morphIndex = 5;
				this.doAction;
				view.refresh;
			});
			if(char == $7, {
				morphIndex = 6;
				this.doAction;
				view.refresh;
			});
			if(char == $8, {
				morphIndex = 7;
				this.doAction;
				view.refresh;
			});
			if(char == $9, {
				morphIndex = 8;
				this.doAction;
				view.refresh;
			});
			if(char == $0, {
				morphIndex = 9;
				this.doAction;
				view.refresh;
			});
		}
	}

	valueAction {|v|
		value = v;
		view.refresh;
		this.doAction();
	}

	setValue {|v|
		value = v;
		view.refresh;
	}

	setMorph {|v|
		morph = v.clip(value.neg, 1 - value);
		view.refresh;
		this.doAction();
	}

	doAction {
		action.(value);
	}
}

/*
ThroatGUI_Knob : SCUserView {

	var <value,
		<>morph,
		<>morphIndex,
		<>label;

	*viewClass { ^SCUserView }

	init {|argParent, argBounds|
		super.init(argParent, argBounds);
		value = 0.0;
		morph = 0.0;
		morphIndex = 0;
		label = "";
		this.focusColor_(Color.new(0, 0, 0, 0));
		this.drawFunc = { this.draw };
	}

	draw{
		var	font, stringBounds;
		Pen.scale(1, 1.neg);
		Pen.translate(0, this.bounds.height.neg);
		Pen.fillColor = Color.new(0.9, 0.9, 0.8);
		Pen.addAnnularWedge(
			(this.bounds.width / 2) @ (this.bounds.width / 2),
			(this.bounds.width / 2) * 0.8,
			(this.bounds.width / 2) * 0.85,
			(1.25 * pi),
			(1.5.neg * pi)
		);
		Pen.fill;
		Pen.fillColor = [
			Color.new(1, 0.5, 0),
			Color.new(0, 0.5, 0.5),
			Color.new(0.5, 0, 0.5),
			Color.new(0.5, 0.5, 0),
			Color.new(1, 0, 0),
			Color.new(0, 1, 0),
			Color.new(0, 0, 1),
			Color.new(0, 1, 1),
			Color.new(1, 0, 1),
			Color.new(1, 1, 0),
		].at(morphIndex);
		Pen.addAnnularWedge(
			(this.bounds.width / 2) @ (this.bounds.width / 2),
			(this.bounds.width / 2) * 0.9,
			(this.bounds.width / 2) * 0.75,
			(value * 1.5.neg * pi) + (1.25 * pi),
			((morph).clip(value.neg, (1 - value)) * 1.5.neg * pi)
		);
		Pen.fill;
		Pen.fillColor = Color.new(0.9, 0.9, 0.8);
		Pen.addAnnularWedge(
			(this.bounds.width / 2) @ (this.bounds.width / 2),
			(this.bounds.width / 2) * 0.6,
			(this.bounds.width / 2),
			(1.25 * pi) + (value * 1.5.neg * pi) - 0.075,
			0.15
		);
		Pen.fill;
		Pen.scale(1, 1.neg);
		Pen.translate(0, this.bounds.height.neg);
		font = Font.new("Helvetica Neue Bold", 11);
		SCPen.font = font;
		stringBounds = GUI.stringBounds(label, font);
		SCPen.stringAtPoint(label, 0 @ 0);
		Pen.fill;
	}

	valueAction_{|v|
		value = v;
		this.refresh;
		this.doAction;
	}

	value_{|v|
		value = v;
		this.refresh;
	}

	setMorph{|v|
		morph = v.clip(value.neg, 1 - value);
		this.refresh;
		this.doAction;
	}

	readMouse {
		arg x, y, modifiers, buttonNumber, clickCount;
		var newY, xq;
		([256, 0].includes(modifiers)).if({
			newY = 1 - y.linlin(0, this.bounds.height, 0, 1.0);
			if(newY != value,{ this.valueAction_(newY) });
		});
		([131330].includes(modifiers)).if({
			newY = 1 - y.linlin(0, this.bounds.height, 0, 2.0);
			if(newY != value,{ this.setMorph(newY) });
		});
	}

	mouseDown{
		arg x, y, modifiers, buttonNumber, clickCount;
		mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount);
		this.readMouse(x, y, modifiers, buttonNumber, clickCount);
	}

	mouseMove{
		arg x, y, modifiers, buttonNumber, clickCount;
		mouseMoveAction.value(this, x, y, modifiers, buttonNumber, clickCount);
		this.readMouse(x, y, modifiers, buttonNumber, clickCount);
	}

	defaultKeyDownAction {
		arg char, modifiers, unicode,keycode;
		if(char == $1, {
			morphIndex = 0;
			this.doAction;
			^this.refresh;
		});
		if(char == $2, {
			morphIndex = 1;
			this.doAction;
			^this.refresh;
		});
		if(char == $3, {
			morphIndex = 2;
			this.doAction;
			^this.refresh;
		});
		if(char == $4, {
			morphIndex = 3;
			this.doAction;
			^this.refresh;
		});
		if(char == $5, {
			morphIndex = 4;
			this.doAction;
			^this.refresh;
		});
		if(char == $6, {
			morphIndex = 5;
			this.doAction;
			^this.refresh;
		});
		if(char == $7, {
			morphIndex = 6;
			this.doAction;
			^this.refresh;
		});
		if(char == $8, {
			morphIndex = 7;
			this.doAction;
			^this.refresh;
		});
		if(char == $9, {
			morphIndex = 8;
			this.doAction;
			^this.refresh;
		});
		if(char == $0, {
			morphIndex = 9;
			this.doAction;
			^this.refresh;
		});
		^nil;
	}
}

ThroatGUI_Knob2 : SCUserView {
	var <value,
		<>label;

	*viewClass { ^SCUserView }

	init {|argParent, argBounds|
		super.init(argParent, argBounds);
		value = 0.0;
		label = "";
		this.focusColor_(Color.new(0, 0, 0, 0));
		this.drawFunc = { this.draw };
	}

	draw {
		var font, stringBounds;
		Pen.scale(1, 1.neg);
		Pen.translate(0, this.bounds.height.neg);
		Pen.fillColor = Color.new(0.9, 0.9, 0.8);
		Pen.addAnnularWedge(
			(this.bounds.width / 2) @ (this.bounds.width / 2),
			(this.bounds.width / 2) * 0.8,
			(this.bounds.width / 2) * 0.85,
			(1.25 * pi),
			(1.5.neg * pi)
		);
		Pen.fill;
		Pen.fillColor = Color.new(0.9, 0.9, 0.8);
		Pen.addAnnularWedge(
			(this.bounds.width / 2) @ (this.bounds.width / 2),
			(this.bounds.width / 2) * 0.6,
			(this.bounds.width / 2),
			(1.25 * pi) + (value * 1.5.neg * pi) - 0.075,
			0.15
		);
		Pen.fill;
		Pen.scale(1, 1.neg);
		Pen.translate(0, this.bounds.height.neg);
		font = Font.new("Helvetica Neue Bold", 11);
		SCPen.font = font;
		stringBounds = GUI.stringBounds(label, font);
		SCPen.stringAtPoint(label, 0 @ 0);
		Pen.fill;
	}

	valueAction_{|v|
		value = v;
		this.refresh;
		this.doAction;
	}

	value_{|v|
		value = v;
		this.refresh;
	}

	readMouse {
		arg x, y, modifiers, buttonNumber, clickCount;
		var newY, xq;
		([256, 0].includes(modifiers)).if({
			newY = 1 - y.linlin(0, this.bounds.height, 0, 1.0);
			if(newY != value,{ this.valueAction_(newY) });
		});
	}

	mouseDown{
		arg x, y, modifiers, buttonNumber, clickCount;
		mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount);
		this.readMouse(x, y, modifiers, buttonNumber, clickCount);
	}

	mouseMove{
		arg x, y, modifiers, buttonNumber, clickCount;
		mouseMoveAction.value(this, x, y, modifiers, buttonNumber, clickCount);
		this.readMouse(x, y, modifiers, buttonNumber, clickCount);
	}

	defaultKeyDownAction {
		arg char, modifiers, unicode, keycode;
		^nil;
	}
}

*/