ThroatGUI_Knob {
	var <view,
	<>value,
	<>morph,
	<>morphIndex,
	<>label,
	<>action,
	clicked, startY;

	*new {|... args|
		^super.new.init(*args);
	}

	init {|parent, rect|
		value = 0.0;
		morph = 0.0;
		morphIndex = 0;
		label = "";
		action = {|value| };
		clicked = false;
		view = UserView.new(parent, rect);
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
		view.refresh();
	}

	setMorph {|v|
		morph = v.clip(value.neg, 1 - value);
		view.refresh;
		this.doAction();
	}

	doAction {
		action.(this);
	}
}

ThroatGUI_Knob2 {
	var <view,
	<>value,
	<>morph,
	<>morphIndex,
	<>label,
	<>action,
	clicked, startY;

	*new {|... args|
		^super.new.init(*args);
	}

	//window,Rect.new(grid * iMod + grid, grid * 1.5 * row + grid, grid - 15, grid)
	init {|parent, rect|
		value = 0.0;
		morph = 0.0;
		morphIndex = 0;
		label = "";
		action = {|value| };
		clicked = false;
		view = UserView.new(parent, rect);
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