ThroatModule {
	var	<synth,
		<>main,
		<>id,
		<data,
		<dataMorph,
		<morph;

	*new {|... args|
		^super.new.init(*args)
	}

	init {|argMain, argId, argData, argDataMorph|
		this.main = argMain;
		this.id = argId;

		synth = Synth.new(main.synthSymbols[id]);
		if(main.synthSymbols[id] == \Throat_Choir, { main.choirSynth = synth });
		(argData.notNil).if({
			data = argData.deepCopy;
			this.applyDataToSynth();
		},{
			data = 0.0 ! main.synthParameterSymbols[this.id].size;
		});

		(argDataMorph.notNil).if({
			dataMorph = argDataMorph.deepCopy;
		},{
			dataMorph = [0,0.0] ! main.synthParameterSymbols[this.id].size;
		});
		morph = 0.0!10;
		if(ThroatDebug.on,{ (" @ThroatModule.init   i:" + id + "   mdata:" + data).postln });
	}

	cleanUp {
		synth.free;
		synth = nil;
	}

	activate {
		synth.run(true);
	}

	deactivate {
		synth.run(false);
	}

	applyDataToSynth {
		data.do({|v,i|
			synth.set(main.synthParameterSymbols[this.id][i], v);
		});
	}

	panel {
		var panel,swapView,popView;
		panel = CompositeView.new(
			main.panelsView,
			(main.panelsView.bounds.width - 2) @ ((((main.panelsView.bounds.height) / main.modules.size) - 1).floor)
		).background_(Color.new(0,0,0,1.0));
		panel.decorator = FlowLayout.new(panel.bounds, 5 @ 0, 0 @ 0);
		UserView.new(panel, 10 @ (panel.bounds.height - 2)).background_(Color.new(0, 0, 0, 1));
		popView = CompositeView.new(panel, (panel.bounds.height * 4) @ (panel.bounds.height - 1));
		PopUpMenu.new(popView, Rect.new(1, 1, panel.bounds.height * 4 - 2, panel.bounds.height - 3))
			.items_(main.synthSymbols.collect(_.asString))
			.value_(this.id)
			.action_({|v|
				if(ThroatDebug.on,{
					v.value.postln;
					this.postln;
				});
				main.changeModule(this, v.value);
			});
		StaticText.new(popView, Rect.new(0, 0, panel.bounds.height * 4, panel.bounds.height - 1 - 10))
			.stringColor_(Color.new(0.5, 0.75, 1))
			.background_(Color.new(0, 0, 0))
			.string_(main.synthSymbols[this.id].asString.split($_)[1])
			.font_(Font.new("Helvetica Neue Bold", 25));
		UserView.new(panel,1 @ (panel.bounds.height)).background_(Color.new(0.5, 0.75, 1));
		swapView = CompositeView.new(panel, 70 @ (panel.bounds.height - 2)).background_(Color.new(0.5, 0.75, 1));
		StaticText.new(swapView,Rect.new(0, 0, swapView.bounds.width, (swapView.bounds.height / 2) - 1))
			.enabled_(true)
			.mouseDownAction_({ main.swapModules(this, -1) })
			.string_("UP")
			.align_(\center)
			.font_(Font.new("Helvetica Neue Bold", 11))
			.stringColor_(Color.new(0.5, 0.5, 0.5, 1))
			.background_(Color.new(0, 0, 0));
		StaticText.new(swapView,Rect.new(0, swapView.bounds.height / 2, swapView.bounds.width, swapView.bounds.height / 2)).enabled_(true)
			.mouseDownAction_({ main.swapModules(this, 1) })
			.string_("DOWN")
			.align_(\center)
			.font_(Font.new("Helvetica Neue Bold", 11))
			.stringColor_(Color.new(0.5, 0.5, 0.5, 1))
			.background_(Color.new(0, 0, 0));
		UserView.new(panel,1 @ (panel.bounds.height)).background_(Color.new(0.5, 0.75, 1));
		UserView.new(panel,10 @ (panel.bounds.height - 2)).background_(Color.new(0, 0, 0, 1));
		main.synthParameterSymbols[this.id].do({|symbol,i|
			main.createKnob(
				panel,
				{|k|
					data[i] = k.value;
					dataMorph[i] = [k.morphIndex,k.morph];
					this.updateSynth(i);
				},
				main.synthParameterStrings[this.id][i],
				data[i],
				dataMorph[i][0],
				dataMorph[i][1]
				);
		});
		^panel;
	}

	updateSynth {|index=nil|
		if(index.notNil,{
			synth.set(
				main.synthParameterSymbols[this.id][index],
				(data[index] + (dataMorph[index][1] * morph[dataMorph[index][0]])).clip(0, 1)
			);
		},{
			main.synthParameterSymbols[this.id].do({|symbol,i|
				synth.set(
					symbol,
					(data[i] + (dataMorph[i][1] * morph[dataMorph[i][0]])).clip(0,1)
				);
			});
		});
	}

	setMorph {|index,value|
		morph[index] = value;
		this.updateSynth;
	}

	createArchive {
		^([id, data, dataMorph]);
	}

	readArchive {|inData|
		data.size.do({|i|
			data[i] = inData[i]
		});
	}
}
