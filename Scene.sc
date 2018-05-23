ThroatScene {
	var	parameters,
		<>name,
		<>comment,
		synth,
		window,
		<modules,
		<panelsView,
		<synthSymbols,
		<synthParameterSymbols,
		<synthParameterStrings,
		source,
		master,
		main,
		sourceGroup,
		masterGroup,
		fxGroup,
		keyboards,
		<chordMemory,
		<>chordMemoryPosition,
		<chordMemoryLength,
		chordView,
		<>choirSynth,

		keys,
		popups,
		chordNr;

	*new {|... args|
		^super.new.init(*args);
	}

	init {|argMain|
		synthSymbols = [
			\Throat_ModDelay,
			\Throat_MagAbove,
			\Throat_MagBelow,
			\Throat_HarmonicDistortion,
			\Throat_PitchShift,
			\Throat_BinShift,
			\Throat_AmpMod,
			\Throat_Excess,
			\Throat_3VoiceGrain,
			\Throat_OctaveSynth,
			\Throat_Choir,
			\Throat_Empty,
			\Throat_Gain,
			\Throat_LPF,
			\Throat_HPF,
			\Throat_Delay,
			\Throat_Reverb,
			\Throat_Sampler,
			\Throat_ChordConvolution
		];
		synthParameterSymbols = [
			[\dw, \freq, \rDepth, \pDepth, \noise, \drive],
			[\dw, \thres, \gcomp],
			[\dw, \thres, \gcomp],
			[\dw, \gain1, \gain2, \gain3, \gain4, \gain5, \gain7],
			[\dw, \shift, \pDisp, \tDisp],
			[\dw, \pitch, \spread],
			[\dw, \freqP, \width, \freqS, \filter],
			[\dw, \thres, \gComp],
			[\dw, \f1, \f2, \f3, \overlap, \rad, \lpf, \fNoise, \len],
			[\dw, \f1, \f2, \f3, \f1f, \f2f, \f3f, \argRes, \argTone, \argThres],
			[\dw, \tDisp, \pDisp, \argThres, \oct, \atk, \rel],
			[],
			[\gain],
			[\dw, \freq, \res],
			[\dw, \freq, \res],
			[\dw, \time, \fb],
			[\dw, \size, \damp],
			[\dw, \oct, \semi, \cent, \len, \pos, \dir, \lpFreq, \hpFreq],
			[\dw, \tone]
		];
		synthParameterStrings = [
			["D/W", "Freq", "RDepth", "PDepth", "Noise", "Drive"],
			["D/W", "Thres", "GComp"],
			["D/W", "Thres", "GComp"],
			["D/W", "1Harm", "2Harm", "3Harm", "4Harm", "5Harm", "7harm"],
			["D/W", "Shift", "PDisp", "TDisp"],
			["D/W", "Pitch", "Spread"],
			["D/W", "PFrq", "PWidth", "SFrq", "FFrq"],
			["D/W", "Thres", "GComp"],
			["D/W", "P1", "P2", "P3", "Dens", "TDisp", "LPF", "PDisp", "Len"],
			["D/W", "P1", "P2", "P3", "Fine1", "Fine2", "Fine3", "Res", "Tone", "Thres"],
			["D/W", "TDisp", "PDisp", "Thres", "Oct", "Atk", "Rel"],
			[],
			["Gain"],
			["DW", "Freq", "Res"],
			["DW", "Freq", "Res"],
			["DW", "Time", "FB"],
			["DW", "Size", "Damp"],
			["DW", "Oct", "Semi", "Cent", "Len", "Pos", "Dir", "LPF", "HPF"],
			["DW","Tone"]
		];
		keyboards = nil!24!5;
		chordMemory = [];
		chordMemory = Array.fill3D(100, 5, 25, { 0 });
		chordMemoryPosition = 0;
		chordMemoryLength = 3;
		main = argMain;
		this.initModules(nil);
		this.updateExecutionOrder;
		name = "Scene";
		comment = "";

		keys = nil ! 5 ! 24;
		popups = nil ! 5;
	}

	readChordsFromFile {
		var file, notes, nrOfChords, chords, error;

		Dialog.openPanel({|path|
			error = { "SOMETHING WENT TERRIBLY WRONG".postln };
			file = SimpleMIDIFile.read(path.standardizePath);
			notes = file.noteOnEvents;

			if(notes.size % 5 == 0, {
				nrOfChords = floor(notes.size / 5);
				}, {
					error.value();
			});
			nrOfChords = (notes.size / 5);
			chords = [0, 0, 0, 0, 0] ! nrOfChords;

			notes.do({|n, i|
				var index;
				index = floor(i / 5);
				chords[index][i % 5] = n[4];
			});
			//chords.postln;
			chordMemory = Array.fill3D(100, 5, 25, { 0 });
			chordMemoryPosition = 0;
			chordMemoryLength = nrOfChords;
			chords.do({|notes, index|
				notes.do({|note, part|
					var dif, octave;
					if((note >= 48) && (note < 72), {
						dif = 48;
						octave = 0;
					});
					if(note < 48, {
						dif = 36;
						octave = -12;
					});
					if(note < 36, {
						dif = 24;
						octave = -24;
					});
					if(note >= 72, {
						dif = 60;
						octave = 12;
					});
					if(note >= 84, {
						dif = 72;
						octave = 24;
					});



					chordMemory[index][part][note - dif] = 1;
					chordMemory[index][part][24] = octave;
				});
			});

		}, {

		}, false);
	}

	recieveMorph {|index,morph|
		modules.do(_.setMorph(index, morph));
	}

	sample {
		modules.do({|m,i|
			if(m.id == 17) {
				m.synth.set(\t_sample, 1);
			};
		});
	}

	clearSample {
		modules.do({|m,i|
			if(m.id == 17) {
				m.synth.set(\t_clear,1);
			};
		});
	}


	activate {
		modules.do(_.activate);
	}

	deactivate {
		modules.do(_.deactivate);
	}

	initModules {|data|
		if(data != nil, {
			modules.do(_.cleanUp);
			modules = nil;
			modules = 10.collect({|i|
				var	index, moduleData, morphData;
				index = data[i][0];
				moduleData = data[i][1].deepCopy;
				morphData = data[i][2];
				ThroatModule.new(this, index, moduleData, morphData);
			});
		},{
			modules = 10.collect({|i| ThroatModule.new(this, 11, nil, nil)});
		});
	}

	createWindow {
		var	inputView,outView;
		window = Window.new(this.name, Rect.new(20, 50, 1300, 760)).front;
		window.view.background_(Color.black);
		window.view.keyDownAction_({
			arg view, char, modifiers, unicode, keycode;
			if(keycode == 123, {
				chordMemoryPosition = chordMemoryPosition - 1;
				if(chordMemoryPosition < 0,{
					chordMemoryPosition = chordMemoryLength;
				});
			});
			if(keycode == 124, {
				chordMemoryPosition = chordMemoryPosition + 1;
				if(chordMemoryPosition > chordMemoryLength,{
					chordMemoryPosition = 0;
				});
			});
			if(keycode == 126, {
				chordMemoryLength = chordMemoryLength + 1;
				if(chordMemoryLength > 99, { chordMemoryLength = 99 });
			});

			if(keycode == 125, {
				chordMemoryLength = (chordMemoryLength - 1) % 100;
				if(chordMemoryLength < 0, { chordMemoryLength=0 });
			});
			keycode.postln;
			if(keycode == 31, {
				this.readChordsFromFile();
				this.updateChordView;
				this.updateChoirSynth;
			});
			if(ThroatDebug.on, {
				("chordMemoryPosition:" + chordMemoryPosition).postln;
				("chordMemoryLength:" + chordMemoryLength).postln;
				(chordMemory[chordMemoryPosition]).postln;
			});
			if((keycode >= 123) && (keycode <= 126),{
				this.updateChordView;
				this.updateChoirSynth;
			});

		});
		Font.antiAliasing_(true).smoothing_(true);
		StaticText.new(window, Rect.new(20, 10, 800, 30))
			.background_(Color.new(1, 1, 1, 0.0))
			.font_(Font.new("Helvetica Neue Bold", 25))
			.string_("The Throat III")
			.stringColor_(Color.new(1, 1, 1, 1));
		StaticText.new(window,Rect.new(980, 10, 300, 30))
			.background_(Color.new(1, 1, 1, 0.0))
			.font_(Font.new("Helvetica Neue Bold", 25))
			.string_("Version 4.0")
			.stringColor_(Color.new(1, 1, 1, 0.5))
			.align_(\right);
		this.updatePanelsView;

		this.createChordView;
		//this.updateChordView;

		CmdPeriod.doOnce({ window.close });
		^window;
	}

	createKnob {
		arg parent,
		    action,
		    label,
		    value,
		    mIndex,
		    m;
		ThroatGUI_Knob.new(parent, (parent.bounds.height - 20) @ parent.bounds.height)
			.action_(action)
			.value_(value)
			.label_(label)
			.morphIndex_(mIndex)
			.morph_(m);
	}

	changeModule {|module,type|
		var	index, sentinel;
		sentinel = false;
		if(synthSymbols[type] == \Throat_Choir, {
			if(ThroatDebug.on, { "symbol = Choir".postln });
			modules.do({|m,i|
				if(m.id == type,{
					sentinel = true;
				});
			});
		});
		if(sentinel.not,{
			"sentinel.not: ".post;
			sentinel.not.postln;
			modules.do({|m,i| if(m == module, {index = i}) });
			modules[index].cleanUp;
			modules[index] = ThroatModule.new(this,type,nil);
			this.updateExecutionOrder;
			this.updatePanelsView;
		});
	}

	swapModules {|module,dir|
		var	temp,index,swapIndex;
		modules.do({|m,i| if(m == module, { index = i }) });
		swapIndex = (index + dir) % modules.size;
		temp = modules[swapIndex];
		modules[swapIndex] = modules[index];
		modules[index] = temp;
		this.updateExecutionOrder;
		this.updatePanelsView;
	}

	updateExecutionOrder {
		modules.do({|m,i|
			m.synth.moveToTail(main.fxGroup);
		});
	}

	updatePanelsView {
		panelsView = CompositeView.new(window, Rect.new(20, 40, 900, 711))
			.background_(Color.new(0.5, 0.75, 1));
		panelsView.decorator = FlowLayout.new(panelsView.bounds, 1 @ 1, 1 @ 1);
		modules.do({|m,index| m.panel });
	}

	updateChoirSynth {
		var f1,f2,f3,f4,f5;
		if(chordMemory[chordMemoryPosition][0].includes(1), { f1=chordMemory[chordMemoryPosition][0].indexOf(1) }, { f1=0 });
		if(chordMemory[chordMemoryPosition][1].includes(1), { f2=chordMemory[chordMemoryPosition][1].indexOf(1) }, { f2=0 });
		if(chordMemory[chordMemoryPosition][2].includes(1), { f3=chordMemory[chordMemoryPosition][2].indexOf(1) }, { f3=0 });
		if(chordMemory[chordMemoryPosition][3].includes(1), { f4=chordMemory[chordMemoryPosition][3].indexOf(1) }, { f4=0 });
		if(chordMemory[chordMemoryPosition][4].includes(1), { f5=chordMemory[chordMemoryPosition][4].indexOf(1) }, { f5=0 });
		if(ThroatDebug.on,{
			(48 + f1 + chordMemory[chordMemoryPosition][0][24]).midicps.postln;
			(48 + f2 + chordMemory[chordMemoryPosition][1][24]).midicps.postln;
			(48 + f3 + chordMemory[chordMemoryPosition][2][24]).midicps.postln;
			(48 + f4 + chordMemory[chordMemoryPosition][3][24]).midicps.postln;
			(48 + f5 + chordMemory[chordMemoryPosition][4][24]).midicps.postln;
		});
		choirSynth.set(
			\f1, (48 + f1 + chordMemory[chordMemoryPosition][0][24]).midicps,
			\f2, (48 + f2 + chordMemory[chordMemoryPosition][1][24]).midicps,
			\f3, (48 + f3 + chordMemory[chordMemoryPosition][2][24]).midicps,
			\f4, (48 + f4 + chordMemory[chordMemoryPosition][3][24]).midicps,
			\f5, (48 + f5 + chordMemory[chordMemoryPosition][4][24]).midicps
		);
		modules.do({|m,i|
			m.id.postln;
			if(m.id == 18) {
				m.synth.set(
					\f1, (48 + f1).midicps,
					\f2, (48 + f2).midicps,
					\f3, (48 + f3).midicps,
					\f4, (48 + f4).midicps,
					\f5, (48 + f5).midicps
				);
			};
		});
	}

	generatePanelsView {
		this.updatePanelsView;
	}

	generateChordView {
		this.updateChordView;
	}

	updateChordView {
		if(window.notNil and: { window.isClosed.not }, {

			5.do({|i|
				24.do({|j|
					keyboards[i][j].value_(chordMemory[chordMemoryPosition][i][j]);
				});
				popups[i].value = (chordMemory[chordMemoryPosition][i][24] / 12) + 2;

			});

			chordNr.string_("CHORD" + (chordMemoryPosition + 1) + "/" + (chordMemoryLength + 1));
		});
	}

	createChordView {
		if(window.notNil and: { window.isClosed.not }, {
			chordView = CompositeView.new(window, Rect.new(930, 40, 24 * 12 + 14 + 50, 357))
				.background_(Color.new(0.5, 0.75, 1));
			5.do({|i|
				keys = CompositeView.new(chordView, Rect.new(7, i * 45 + 5, 24 * 12, 40));
				keys.background_(Color.new(0.5, 0.5, 0.5));
				24.do({|j|
					var states;
					if([false, true, false, true, false, false, true, false, true, false, true, false].wrapAt(j), {
						states = [
							["", Color.black, Color.black],
							["", Color.black, Color.new(0.5, 0, 0, 1.0)]
						];
					},{
						states = [
							["", Color.black, Color.white],
							["", Color.black, Color.new(1,0.5,0.5,1.0)]
						];
					});
					keyboards[i][j] = Button.new(keys, Rect.new(j * 12, 0, 11, 40))
							.states_(states)
							.canFocus_(false)
							.action_({|b|
								this.updateChordMemory(i, j);
								this.updateChoirSynth;
							})
							.value_(chordMemory[chordMemoryPosition][i][j]);
				});


				// -----------------------------------------------------------------------------------------------
				popups[i] = PopUpMenu.new(chordView, Rect.new(24 * 12 + 5, i * 45 + 5, 40, 40))
					.items_([" -2", " -1", "0", " +1", " +2"])
					.action_({|m|
						chordMemory[chordMemoryPosition][i][24] = [-24, -12, 0, 12, 24][m.value];
						this.updateChoirSynth;
					})
					.value_(
						(chordMemory[chordMemoryPosition][i][24] / 12) + 2;
					)
					.canFocus_(false);


				// -----------------------------------------------------------------------------------------------

			});
			chordNr = StaticText.new(chordView, Rect.new(7, 230, 24 * 12 + 14, 60))
				.string_("CHORD" + (chordMemoryPosition + 1) + "/" + (chordMemoryLength + 1))
				.align_(\center)
				.font_(Font.new("Helvetica Neue Bold", 11))
				.background_(Color.new(0, 0, 0, 0))
				.stringColor_(Color.new(0, 0, 0, 1));
		});
	}

	updateChordMemory {|keyboard,pos|
		24.do({|k|
			if(k != pos,{
				keyboards[keyboard][k].value_(0);
				chordMemory[chordMemoryPosition][keyboard][k] = 0;
			});
		});
		chordMemory[chordMemoryPosition][keyboard][pos] = 1;
	}

	generateState {
		var state;
		state = modules.collect(_.createArchive);
		state = state ++ ([[chordMemory, chordMemoryPosition, chordMemoryLength]]);
		state = state ++ [name];
		^state;
	}

	loadMainState {|state|
		this.activateState(state);
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
			if(ThroatDebug.on, { state.postln });
			this.activateState(state);
		},{

		},false);
	}

	activateState {|state|
		var	chord;
		name = state.removeAt(state.size - 1);
		chord = state.removeAt(state.size - 1);
		chordMemory = chord[0].deepCopy;
		chordMemoryPosition = chord[1];
		if(chord.size > 2, { chordMemoryLength = chord[2] });

		this.initModules(state);
		this.updateExecutionOrder;
		if(window.notNil and: { window.isClosed.not }, {
			this.updatePanelsView;
			this.updateChordView;
		});
		this.updateChoirSynth;
	}
}
