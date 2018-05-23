ThroatAnalysis {
	var	main,
	    in,
	    window,
	    parameters,
	    responder,
	    buttons,
	    synth,
	    ampGate,
	    pitchGate;

	*new {|... args|
		^super.new.init(*args);
	}

	init {|argMain,inBus|
		main = argMain;
		in = inBus;
		parameters = [0, 1, 0, 1, 0, 1, 0, 1];
		buttons = nil ! 6;
		this.createResponder;
		this.createWindow;
		synth = Synth.new(\Throat_Analysis);
		ampGate = false;
		pitchGate = false;
	}

	createResponder {
		responder = OSCresponderNode(
			Server.default.addr,
			'/tr',
			{
				arg time, responder, msg;
				var	val;
				msg[2].switch(
					0, {
						val = msg[3].linlin(parameters[0],parameters[1], 0, 1);
						main.processData(3, val);
						ampGate = msg[3] > 0.05;
						if(window.isClosed.not, {
							defer {
								buttons[0].value_(val);
							};
						});
					},
					1, {
						val = (((msg[3].cpsmidi - 36) / 48.0)).linlin(parameters[2], parameters[3], 0,1);
						if(ampGate,{
							if(pitchGate,{
								main.processData(4, val);
								if(window.isClosed.not,{
									defer {
										buttons[1].value_(val);
									};
								});
							});
						});
					},
					2, {
						pitchGate = (msg[3] > 0.7);
						if(window.isClosed.not,{
							defer {
								buttons[2].value_(msg[3]);
							};
						});
					},
					3, {
						val = ((msg[3] - 1000) / 8000).linlin(parameters[6], parameters[7], 0, 1);
						if(ampGate,{
							main.processData(5, ((((msg[3] - 1000) / 8000)) - parameters[6]).clip(0, 1) / parameters[7]);
							if(window.isClosed.not,{
								defer {
									buttons[3].value_(val);
								};
							});
						});
					}
				);
			}
		).add;
		CmdPeriod.doOnce({
			responder.remove;
			window.close;
		});
	}

	updateSynth {

	}

	createWindow {
		window = Window.new("ANALYSIS VIS", Rect.new(500, 500, 400, 500)).front;
		window.view.background_(Color.black);
		buttons = 4.collect({|i| Slider.new(window,Rect.new(i * 100, 0, 50, 500)) });
		4.do({|i|
			RangeSlider.new(window,Rect.new(i * 100 + 50, 0, 25, 500))
				.action_({|v|
					parameters[i * 2] = v.lo;
					parameters[i * 2 + 1] = v.hi;
				})
				.lo_(parameters[i*2])
				.hi_(parameters[i*2+1])
				.knobColor_(Color.gray);
		});
		CmdPeriod.doOnce({ window.close });
		window.onClose_({ responder.remove });
		this.createResponder;
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
		if(window.notNil, {
			defer { window.close };
		});
		this.updateSynth;
		this.createWindow;
	}
}
