// ThroatMain.new()

ThroatDebug { *on { ^false } }

ThroatMain  {
	var	<sceneIndex,
		<>sceneBank,
		<sceneManager,
		<>sceneWindow,
		<inputGroup,
		<masterGroup,
		<fxGroup,
		<sensors,
		<input,
		<analysis,
		<master,
		window,
		<sensorsActive,
		<pS1,
		<pS2,
		nextChordThres,
		nextChordAllowed,
		nextSceneThres,
		nextSceneAllowed,
		sampleAllowed,
		clearAllowed,
		structure,
		structPos,
		structVisWindow,
		structVisWidgets;

	*new {|... args|
		^super.new.init(*args);
	}

	init {|sa|
		sa = true;
		"__________________".postln;
		Server.default.options.memSize_(2**19);
		Server.default.recHeaderFormat_("aiff");
		Server.default.recSampleFormat_("int24");
		Server.default.options.blockSize_(16);
		Server.default.waitForBoot({
			this.compileSynthDefs;
			Server.default.sync;
			sensorsActive = sa;
			pS1 = false;
			pS2 = false;
			sceneManager = ThroatSceneManager.new(this);
			fork {
				// HERE BE DRAGONS!!!!!!!!!!!!!!!!!!!
				0.75.yield;
				sensors = ThroatSensors.new(this);
			};
			nextChordThres = 0.5;
			nextChordAllowed = true;
			nextSceneThres = 0.5;
			nextSceneAllowed = true;
			sampleAllowed = true;
			clearAllowed = true;
			inputGroup = Group.new(Server.default);
			fxGroup = Group.after(inputGroup);
			masterGroup = Group.after(fxGroup);
			input = ThroatInput.new(this, 0);
			master = ThroatMaster.new(this);
			analysis = ThroatAnalysis.new(this, 20);
			this.initScenes;
			sceneIndex = 0;
		});
		View.globalKeyDownAction_({|v, c, m, u, k|
			k.postln;
			if(k == 12,{
				sceneBank[sceneIndex].sample;
			});
			if(k == 13,{
				sceneBank[sceneIndex].clearSample;
			});
		});
		CmdPeriod.doOnce({
				View.globalKeyDownAction_(nil);
		});
		this.createWindow;
	}

	updateFlow {|dir|
		var chr, target, stringArray;
		if(structVisWindow.notNil, {
			stringArray = 3.collect({|i|
				var result,
				    index = sceneIndex+[-1, 0, 1] @ i;
				if((index >= 0) && (index < sceneBank.size), {
					result = sceneBank[index].name;
				},{
					result = "";
				});
				result;
			});
			defer {
				if(structVisWindow.isClosed.not,{
					structVisWidgets[0].string_(stringArray[0]);
					structVisWidgets[1].string_(stringArray[1]);
					structVisWidgets[2].string_(stringArray[2]);
				});
			};
		});
	}

	genStructVis {
		var stringArray,updateFlow;
		stringArray = 3.collect({|i|
			var result,
			    index = sceneIndex + [-1, 0, 1] @ i;
			if((index >= 0) && (index < sceneBank.size),{
				result = sceneBank[index].name;
			},{
				result = "";
			});
			result;
		});
		structVisWindow = Window.new("STRUCT VIS", Rect.new(0, 0, 1200, 700)).front;
		structVisWindow.view.background_(Color.black);
		structVisWindow.view.keyDownAction_({|view, char, modifiers, unicode, keycode|
			if(keycode == 124,{
				this.switchScene(1);
				this.updateFlow.(1);
			});
			if(keycode == 123,{
				this.switchScene(1.neg);
				this.updateFlow.(1.neg);
			});
			structVisWindow.front;
		});
		structVisWidgets = nil ! 3;
		structVisWidgets[0] = StaticText.new(structVisWindow, Rect.new(0, 0, 250, 700));
		structVisWidgets[2] = StaticText.new(structVisWindow, Rect.new(750, 0, 250, 700));
		structVisWidgets[1] = StaticText.new(structVisWindow, Rect.new(250, 0, 500, 700));
		structVisWidgets.do({|w, i|
			w.background_(Color.new(0, 0, 0, 0));
			w.stringColor_([Color.new(0.25, 0.25, 1.0), Color.new(0.1, 1, 0.1), Color.new(1, 1, 0.25)].at(i));
			w.align_(\centered);
			w.string_(stringArray.at(i));
			w.font_([Font.new("Helvetica Neue", 70), Font.new("Helvetica Neue", 100)].wrapAt(i));
		});
		CmdPeriod.doOnce({structVisWindow.close});
	}

	createWindow {
		var	menu, sceneSkin;
		window = Window.new("Prototyp 3", Rect.new(100, 100, 350, 800)).front;
		window.view.background_(Color.black);
		menu = View.new(window, Rect.new(10, 50, 280, 400));
		7.do({|i|
			Button.new(menu,Rect.new(50, i * 50, 280, 40))
				.states_([[
					[
						"SENSORS",
						"INPUT",
						"ANALYSIS",
						"MASTER",
						"SCENE",
						"SCENE MANAGER",
						"SCENE COVER FLOW"
					].at(i),
					Color.black,
					Color.gray
				]])
				.font_(Font.new("Helvetica Neue Bold", 20))
				.action_(
					[
						{ sensors.createWindow },
						{ input.createWindow },
						{ analysis.createWindow },
						{ master.createWindow },
						{
							if(sceneWindow.notNil,{
								sceneWindow.close;
							});
							sceneWindow = sceneBank[sceneIndex].createWindow;
						},
						{ sceneManager.generateWindow },
						{ this.genStructVis }
					].at(i)
				);
			if(i < 5, {
				Button.new(menu, Rect.new(0, i * 50, 40, 20))
					.states_([[
						"S",
						Color.black,
						Color.gray
					]])
					.font_(Font.new("Helvetica Neue Bold", 12))
					.action_(
						[
							{ sensors.saveState },
							{ input.saveState },
							{ analysis.saveState },
							{ master.saveState },
							{ sceneBank[sceneIndex].saveState }
						].at(i)
					);
				Button.new(menu,Rect.new(0, i * 50 + 20, 40, 20))
					.states_([[
						"L",
						Color.black,
						Color.gray
					]])
					.font_(Font.new("Helvetica Neue Bold", 12))
					.action_(
						[
							{ sensors.loadState },
							{ input.loadState },
							{ analysis.loadState },
							{ master.loadState },
							{ sceneBank[sceneIndex].loadState }
						].at(i)
					);
			});
		});
		Button.new(window, Rect.new(10, 400, 330, 40))
			.states_([[
				"SAVE ALL",
				Color.black,
				Color.gray
			]])
			.font_(Font.new("Helvetica Neue Bold", 20))
			.action_({ this.saveAll });

		Button.new(window,Rect.new(10,450,330,40))
			.states_([[
				"LOAD ALL",
				Color.black,
				Color.gray
			]])
			.font_(Font.new("Helvetica Neue Bold", 20))
			.action_({ this.loadAll });
		CmdPeriod.doOnce({ window.close });
	}

	nextChord {
		sceneBank[sceneIndex].chordMemoryPosition = sceneBank[sceneIndex].chordMemoryPosition + 1;
		if(sceneBank[sceneIndex].chordMemoryPosition > sceneBank[sceneIndex].chordMemoryLength,{
			sceneBank[sceneIndex].chordMemoryPosition = 0;
		});
		defer { sceneBank[sceneIndex].updateChordView };
		sceneBank[sceneIndex].updateChoirSynth;
	}

	compileSynthDefs {
		ThroatSynthDefs.compile();
		ThroatBuffers.initialize();
	}

	switchScene {|dir|
		defer {
			sceneBank[sceneIndex].deactivate;
			sceneIndex = (sceneIndex + dir).clip(0, sceneBank.size - 1);
			if(ThroatDebug.on,{
				(
					"SCENE #" ++
					sceneIndex ++
					"\t\t" ++
					sceneBank[sceneIndex].name ++
					"\t\t" ++
					sceneBank[sceneIndex].comment
				).postln;
			});
			sceneBank[sceneIndex].activate;
			this.updateFlow();
			if(sceneWindow.notNil,{
				if(sceneWindow.isClosed.not,{
					sceneWindow.close;
					sceneWindow = sceneBank[sceneIndex].createWindow;
				});
			});
		};
	}

	processData {|index, data|
		var deadZone = 0.2;

		if(index == 0, {
			if(data > (nextChordThres + deadZone), {
				if(nextChordAllowed,{
					this.nextChord;
					nextChordAllowed = false;
				});
			});
			if(data < (nextChordThres - deadZone), { nextChordAllowed = true });
		});

		if(index == 1, {
			if(data > (nextSceneThres + deadZone), {
				if(nextSceneAllowed, {
					this.switchScene(1);
					nextSceneAllowed = false;
				});
			});
			if(data < (nextSceneThres - deadZone), { nextSceneAllowed = true });
		});
/*
		if(index==1,{ pS1 = (data > 0.5) });
		if(index==2,{ pS2 = (data > 0.5) });
		if(pS2 && pS2,{
			if(clearAllowed, {
				clearAllowed = false;
				sceneBank[sceneIndex].clearSample;
			});
		});
		if((pS2 && pS2).not, {
			clearAllowed = true;
		});
*/
		if(index > 1, {
			sceneBank[sceneIndex].recieveMorph(index, data);
		});
	}

	initScenes {
		sceneBank = [
			ThroatScene.new(this),
			ThroatScene.new(this).deactivate,
			ThroatScene.new(this).deactivate,
			ThroatScene.new(this).deactivate,
			ThroatScene.new(this).deactivate,
			ThroatScene.new(this).deactivate
		];
	}

	saveAll {
		var state;
		Dialog.savePanel({|path|
			state = [];
			if(ThroatDebug.on, { "Saving sensors...".postln });
			state = (state ++ [sensors.generateState]).deepCopy;
			if(ThroatDebug.on, { "Saving input...".postln });
			state = (state ++ [input.generateState]).deepCopy;
			if(ThroatDebug.on, { "Saving analysis...".postln });
			state = (state ++ [analysis.generateState]).deepCopy;
			if(ThroatDebug.on, { "Saving master...".postln });
			state = (state ++ [master.generateState]).deepCopy;
			if(ThroatDebug.on, { "Saving scenes...".postln });
			state = (state ++ [sceneBank.collect(_.generateState())]).deepCopy;
			if(ThroatDebug.on,{ state.postln});
			state.writeArchive(path);
		},{

		},false);
	}

	loadAll {
		var state;
		Dialog.openPanel({|path|
			var	chord,scenes;
			state = Object.readArchive(path);
			//sensors.loadMainState(state[0]);
			//input.loadMainState(state[1]);
			//analysis.loadMainState(state[2]);
			//master.loadMainState(state[3]);
			scenes = state[4];
			"SCENES".postln;
			sceneBank.do(_.deactivate);
			sceneBank = scenes.size.collect({ ThroatScene.new(this).deactivate });
			sceneBank.do({|s,i| s.loadMainState(scenes[i]) });
		},{

		},false);
	}
}
