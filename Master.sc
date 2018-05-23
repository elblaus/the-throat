ThroatMaster {
	var	synth,
		parameters,
		parameterSymbols,
		main,
		window,
		meter,
		responder;

	*new {|... args|
		^super.new.init(*args);
	}

	init {|argMain|
		main = argMain;
		synth = Synth.new(\Throat_Master, target:main.masterGroup);
		parameters = [
			0.0, 1.0, 0.25, 0.75,
			0.5, 0.5, 0.5, 0.5, 0.5,
			0.0, 0.0, 0.1, 0.1, 0.0, 0.0,
			0.0, 0.0, 0.0
		];
		parameterSymbols = [
			\dw, \send, \time, \fb,
			\lo, \mid1, \mid2, \mid3, \hi,
			\compThres, \compRatio, \compAtk, \compRel, \compHi, \compGain,
			\revAmp, \revLen, \revDamp
		];
		this.updateSynth;
	}

	updateSynth {
		parameters.do({|p,i| synth.set(parameterSymbols[i], p) });
	}

	createWindow {
		var	labels,grid;
		grid = 70;
		labels = [
			"DLY DW",
			"DLY SEND",
			"DLY TIME",
			"DLY FB",
			"EQ 300",
			"EQ 600",
			"EQ 1200",
			"EQ 2400",
			"EQ 4800",
			"C THRES",
			"C RATIO",
			"C ATK",
			"C REL",
			"C HPF",
			"C GAIN",
			"R WET",
			"R LEN",
			"R DAMP"
		];
		window = Window.new("MASTER",Rect.new(300, 500, grid * ((labels.size / 2) + 2), 6.5 * grid)).front;
		window.view.background_(Color.black);
		labels.do({|label,i|
			var	row,iMod;
			row = ((2 * i) / labels.size).floor;
			iMod = i % (labels.size / 2);
			ThroatGUI_Knob2.new(window,Rect.new(grid * iMod + grid, grid * 1.5 * row + grid, grid - 15, grid))
				.label_(label)
				.value_(parameters[i])
				.action_({|v|
					synth.set(parameterSymbols[i], v.value);
					parameters[i] = v.value;
				});
		});
		CmdPeriod.doOnce({ window.close });
		meter = nil ! 2;
		responder = nil ! 2;
		2.do({|i|
			meter[i] = LevelIndicator.new(window, Rect.new(grid, [4.5, 5][i] * grid, 8 * grid, 0.5 * grid))
				.style_(1)
				.background_(Color.gray)
				.drawsPeak_(true)
				.numTicks_(11).numMajorTicks_(3);
			responder[i] = OSCresponder(
				Server.default.addr,
				['/m1', '/m2'][i],
				{|time,resp,msg|
					defer {
						meter[i].value = msg[3].ampdb.linlin(-40, 0, 0, 1);
						meter[i].peakLevel = msg[4].ampdb.linlin(-40, 0, 0, 1);
					};
				}
			).add;
			CmdPeriod.doOnce({ responder[i].remove });
		});
		window.onClose_({ responder.do(_.remove) });
		CmdPeriod.doOnce({ window.close });
	}

	generateState {
		^parameters.deepCopy;
	}

	saveState {
		var state;
		CocoaDialog.savePanel({|path|
			state = this.generateState;
			if(ThroatDebug.on, { state.postln });
			state.writeArchive(path);
		},{

		},false);
	}

	loadState {
		var state;
		CocoaDialog.getPaths({|path|
			var	chord;
			state = Object.readArchive(path[0]);
			if(ThroatDebug.on,{ state.postln });
			this.activateState(state);
		},{

		},false);
	}

	loadMainState {|state|
		if(ThroatDebug.on,{ state.postln });
		this.activateState(state);
	}

	activateState {|state|
		parameters = state.deepCopy;
		if(window.notNil,{
			defer { window.close };
		});
		this.updateSynth;
		this.createWindow;
	}
}
