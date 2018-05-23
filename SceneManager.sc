ThroatSceneManager {
	var	<main,
		window,
		pos,
		gSlots,
		gActiveSlot,
		gIndices;

	*new {|argMain|
		^super.new.init(argMain);
	}

	init {|argMain|
		main = argMain;
		pos = 0;
	}

	generateWindow {


		if(window.notNil, {
			window.front;
		},{
			window = Window.new("SCENE MANAGER", Rect.new(0, 0, 460, 450)).front;
			window.view.background_(Color.black);
		});
		CmdPeriod.doOnce({window.close});
		gSlots = 6.collect({|r|
			var y;
			if(r < 3, {
				y = r * 50;
			},{
				y = r * 50 + 50
			});
			StaticText.new(window, Rect.new(250, y + 50, 200, 50))
				.font_(Font.new("Helvetica Neue Bold", 30))
				.string_("")
				.background_(Color.gray);
		});
		gIndices = 7.collect({|r|
			StaticText.new(window, Rect.new(200, r * 50 + 50, 50, 50))
				.font_(Font.new("Helvetica Neue Bold", 30))
				.string_("")
				.background_(Color.gray)
				.align_(\center);
		});
		gIndices[3].background_(Color.red);
		gActiveSlot = TextField.new(window, Rect.new(250, 200, 200, 50))
			.font_(Font.new("Helvetica Neue Bold", 30))
			.string_("")
			.background_(Color.red)
			.action_({|v|
				this.editName(v.value);
			});
		SCButton.new(window, Rect.new(250, 0, 200, 50))
			.states_([["UP", Color.black, Color.gray]])
			.font_(Font.new("Helvetica Neue Bold", 30))
			.action_({ this.changePos(1.neg) });
		SCButton.new(window, Rect.new(250, 400, 200, 50))
			.states_([["DOWN", Color.black, Color.gray]])
			.font_(Font.new("Helvetica Neue Bold", 30))
			.action_({ this.changePos(1) });
		5.do({|i|
			SCButton.new(window, Rect.new(10, 50 * i + 50, 180, 50))
				.states_([[
					[
						"ADD",
						"RESET",
						"DELETE",
						"MOVE UP",
						"MOVE DOWN"
					][i],
					Color.black,
					Color.gray
				]])
				.font_(Font.new("Helvetica Neue Bold", 20))
				.action_([
					{|v| this.addScene() },
					{|v| this.resetScene() },
					{|v| this.deleteScene() },
					{|v| this.moveScene(1.neg) },
					{|v| this.moveScene(1) },
				][i]);
		});
		this.updateWindow();

	}

	updateWindow {


		gSlots.do({|view,i|
			var	index;
			index = [-3, -2, -1, 1, 2, 3][i];
			if(((pos + index) >= 0) && ((pos + index) < main.sceneBank.size), {
				view.string_(main.sceneBank[index+pos].name);
			},{
				view.string_("");
			});
		});
		gIndices.do({|view,i|
			var	index;
			index = i - 3 + pos;
			if((index >= 0) && (index < main.sceneBank.size),{
				view.string_(index + 1 + "");
			},{
				view.string_("");
			});
		});
		gActiveSlot.string_(main.sceneBank[pos].name);

	}

	changePos {|dir|
		pos = pos + dir;
		pos = pos.clip(0,main.sceneBank.size - 1);
		this.updateWindow();
	}

	addScene {
		this.deactivateScenes();
		main.sceneBank = main.sceneBank.insert(pos, ThroatScene.new(main));
		this.updateSceneActiveStatus();
		this.updateWindow();
	}

	moveScene {|dir|
		this.deactivateScenes();
		main.sceneBank.swap(pos, (pos + dir).wrap(0, main.sceneBank.size));
		pos = (pos + dir).wrap(0, main.sceneBank.size);
		this.updateWindow();
		this.updateSceneActiveStatus();
	}

	deleteScene {
		if(main.sceneBank.size > 1, {
			this.deactivateScenes();
			main.sceneBank.removeAt(pos);
			pos = pos.clip(0, main.sceneBank.size - 1);
			this.updateWindow();
			this.updateSceneActiveStatus();
		});
	}

	resetScene {
		this.deactivateScenes();
		main.sceneBank[pos] = nil;
		main.sceneBank[pos] = ThroatScene.new(main);
		this.updateWindow();
		this.updateSceneActiveStatus();
	}

	editName {|string|
		main.sceneBank[pos].name_(string);
	}

	deactivateScenes {
		main.sceneBank.do(_.deactivate);
	}

	updateSceneActiveStatus {
		main.sceneBank[main.sceneIndex].activate;
	}
}