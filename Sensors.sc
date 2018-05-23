ThroatSensors {
	var	arduino,
		main,
		routine,
		delta,
		data,
		bounds,
		dummyWindow,
		window,
		parameters,
		sliders,
		responder;

	*new {|... args|
		^super.new.init(*args);
	}

	init {|argMain|
		main = argMain;
		parameters = [nil];
		delta = 0.05;
		data = 0.0 ! 10;
		bounds = [0, 4000] ! 10;
		if(main.sensorsActive, {
			responder = OSCresponderNode(nil, '/data', {|t, r, msg|
				this.recieveData(msg[(1..9)]);
			}).add;
			CmdPeriod.doOnce({ responder.remove });

			//arduino = ArduinoSMS("/dev/tty.usbmodem1d11", 115200);
			//arduino.action_({|... msg| this.recieveData(msg) });
			//this.activate;
			this.createWindow;
		},{
			this.dummyMode(true)
		});
	}

	dummyMode {
		arg bool = true;
		defer {
			if(bool, {
				this.deactivate;
				if(dummyWindow.notNil, {
					dummyWindow.front;
				},{
					dummyWindow = Window.new("SENSOR DUMMY", Rect.new(100, 100, 500, 500)).front;
					dummyWindow.view.background_(Color.new(1, 0.5, 0.0));
					CmdPeriod.doOnce({ dummyWindow.close });
					10.do({|i|
						Slider.new(dummyWindow, Rect.new(i * 50, 0, 50, 500))
							.action_({|v| main.processData(i, v.value) })
							.canFocus_(false);
					});
				});
			},{
				this.activate;
			});
		};
	}

	calibrate {
		arg time = 10;
		fork {
			(time * 100).do({
				data.do({|d,i|
					if(d < bounds[i][0], { bounds[i][0] = d });
					if(d > bounds[i][1], { bounds[i][1] = d });
				});
				(time / (100 * time)).yield;
			});
		};
	}

	recieveData {|msgIn|

		8.do({|i|
			data[i] = msgIn[i].linlin(bounds[i][0], bounds[i][1], 0.0, 1.0);
		});
		//data[1] = 1.0 - data[1];
		defer {
			main.processData(0, data[0]);
			main.processData(1, data[1]);
			main.processData(2, data[2]);
			main.processData(3, data[3]);
			main.processData(4, data[4]);
			main.processData(5, data[5]);
			main.processData(6, data[6]);
			main.processData(7, data[7]);
		};
		defer {
			if(window.notNil and: { sliders.notNil }, {
				if(window.isClosed.not, {
						8.do({|i| sliders[i].value_(data[i])});
				});
			});
		};

		/*if((msg.includes(nil).not) && (msg.size == 7), {
			data.do({|d,i|
				if(msg[i + 1].notNil,{
					data[i] = msg[i + 1].linlin(bounds[i][0], bounds[i][1], 0.0, 1.0);
				});
			});
			data[1] = 1.0 - data[1];
			defer {
				main.processData(1, data[0]);
				main.processData(0, data[1]);
				main.processData(2, data[2]);
			};
			defer {
				if(window.notNil and: { sliders.notNil }, {
					if(window.isClosed.not, {
							3.do({|i| sliders[i].value_(data[[1, 0, 2][i]])});
					});
				});
			};
		});*/
	}

	activate {
		routine = fork {
			loop {
				arduino.send($r, $a);
				delta.yield;
			};
		};
	}

	deactivate {
		routine !? routine.stop;
	}

	close {
		arduino !? arduino.close;
	}

	createWindow {
		defer {
			if(window.notNil and: {window.isClosed.not},{
				window.front;
			}, {
				window = Window.new("SENSORS", Rect.new(100, 100, 800, 500)).front;
				window.view.background_(Color.new(1, 0.5, 0.0));
				CmdPeriod.doOnce({ window.close });
				sliders = nil;
				sliders = 10.collect({|i|
					Slider.new(window, Rect.new(i * 80, 0, 40, 500))
						.canFocus_(false);
				});
				if(main.sensorsActive.not, {
					sliders.do({|s,i|
						s.action_({ |v| main.processData(i,v.value) });
					});
				});
				10.do({|i|
					Slider.new(window,Rect.new(i * 80 + 40, 0, 20, 500))
						.canFocus_(false)
						.value_(bounds[i][0] / 4000.0)
						.action_({|s|
							bounds[i][0] = s.value * 4000;
						});
					Slider.new(window,Rect.new(i * 80 + 60, 0, 20, 500))
						.canFocus_(false)
						.value_(bounds[i][1] / 4000.0)
						.action_({|s|
							bounds[i][1] = s.value * 4000;
						});
				});
			});
		};
	}

	generateState {
		^bounds.deepCopy;
	}

	saveState {
		var state;
		CocoaDialog.savePanel({|path|
			state = this.generateState;
			if(ThroatDebug.on,{ state.postln});
			state.writeArchive(path);
		},{

		}, false);
	}

	loadState {
		var state;
		CocoaDialog.getPaths({|path|
			var	chord;
			state = Object.readArchive(path[0]);
			if(ThroatDebug.on,{ state.postln });
			bounds = state.deepCopy;
			if(window.notNil, {
				defer {
					window.close
				};
			});
			this.createWindow;
		},{

		},false);
	}

	loadMainState {|state|

	}
}
