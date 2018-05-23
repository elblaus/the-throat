ThroatBuffers {
	*initialize {
		Buffer.alloc(Server.default, 512, 1, {|buf| buf.chebyMsg([0,1]) }, bufnum:12);
		Buffer.alloc(Server.default, 512, 1, {|buf| buf.chebyMsg([0,0,1]) }, bufnum:13);
		Buffer.alloc(Server.default, 512, 1, {|buf| buf.chebyMsg([0,0,0,1]) }, bufnum:14);
		Buffer.alloc(Server.default, 512, 1, {|buf| buf.chebyMsg([0,0,0,0,1]) }, bufnum:15);
		Buffer.alloc(Server.default, 512, 1, {|buf| buf.chebyMsg([0,0,0,0,0,0,1]) }, bufnum:17);
		Buffer.alloc(Server.default, 1024, 1, bufnum:2);
		Buffer.alloc(Server.default, 1024, 1, bufnum:1);
		Buffer.read(Server.default, "~/Desktop/MathsZ2040.aif".standardizePath, bufnum:0);
	}
}

ThroatSynthDefs {
	*compile {
		SynthDef(\Throat_Input,{
			arg	in=0,
				inGain=0.0,
				testGain=0.0,
				fileGain=0.0,
				out=20,
				lo=0.5,
				mid1=0.5,
				mid2=0.5,
				mid3=0.5,
				hi=0.5,
				compThres=0.0,
				compRatio=0.0,
				compAtk=0.1,
				compRel=0.1,
				compHi=0.0,
				compGain=0.0;

			var	chain,imp,delimp;

			lo = LinLin.kr(lo,0,1,-9,9);
			mid1 = LinLin.kr(mid1,0,1,-9,9);
			mid2 = LinLin.kr(mid2,0,1,-9,9);
			mid3 = LinLin.kr(mid3,0,1,-9,9);
			hi = LinLin.kr(hi,0,1,-9,9);

			compThres = LinExp.kr(compThres,0,1,1,0.01);
			compRatio = LinExp.kr(compRatio,0,1,1,10).reciprocal;
			compAtk = LinExp.kr(compAtk,0,1,0.01,0.1);
			compRel = LinExp.kr(compRel,0,1,0.05,0.5);
			compHi = LinExp.kr(compHi,0,1,10,1000);
			compGain = LinExp.kr(compGain,0,1,1,10);

			inGain = LinExp.kr(inGain,0,1,1,10)-1;
			testGain = LinExp.kr(testGain,0,1,1,10)-1;
			fileGain = LinExp.kr(fileGain,0,1,1,10)-1;

			chain = (inGain*SoundIn.ar([in])) + (testGain*SinOsc.ar(800)) + (fileGain*PlayBuf.ar(1,0,1,loop:1));
			chain = BLowShelf.ar(chain,300,0.5,lo);
			chain = BPeakEQ.ar(chain,600,0.25,mid1);
			chain = BPeakEQ.ar(chain,1200,0.25,mid2);
			chain = BPeakEQ.ar(chain,2400,0.25,mid3);
			chain = BHiShelf.ar(chain,4800,0.25,hi);
			chain = compGain*Compander.ar(chain,HPF.ar(chain,compHi),compThres,1,compRatio,compAtk,compRel);
			chain = Limiter.ar(chain,0.95);

			imp = Impulse.kr(15);
			delimp = Delay1.kr(imp);
			SendReply.kr(imp, '/input', [Amplitude.kr(chain).lag(0,1), K2A.ar(Peak.ar(chain, delimp).lag(0, 3))]);

			Out.ar(out,chain);
		}).send(Server.default);


		SynthDef(\Throat_Analysis,{
			arg	inBus = 20;
			var	f,
				in,
				amplitude,
				freq,
				hasFreq,
				centroid,
				impulse;

			//in = SoundIn.ar([0]);
			in = InFeedback.ar(inBus);
			//in = PinkNoise.ar;

			amplitude = Amplitude.ar(in);
			# freq, hasFreq = Tartini.kr(in);
			f = FFT(LocalBuf.new(2048,1),in);
			centroid = SpecCentroid.kr(f);

			amplitude = LagUD.ar(amplitude,0.1,0.3);
			centroid = LagUD.kr(centroid,0.1,0.2);
			freq = LagUD.kr(freq,0.2,0.3);
			hasFreq = LagUD.kr(hasFreq,0.3,0.1);

			impulse = Impulse.kr(20);
			SendTrig.kr(impulse,0,amplitude,0.5);
			SendTrig.kr(impulse,1,freq,0.5);
			SendTrig.kr(impulse,2,hasFreq,0.5);
			SendTrig.kr(impulse,3,centroid,0.5);
		}).send(Server.default);


		SynthDef(\Throat_Master,{
			arg in=20,
				lo=0.5,
				mid1=0.5,
				mid2=0.5,
				mid3=0.5,
				hi=0.5,
				compThres=0.0,
				compRatio=0.0,
				compAtk=0.1,
				compRel=0.1,
				compHi=0.0,
				compGain=0.0,
				dw=0.0,
				send=0.0,
				time=0.0,
				fb=0.0,
				revAmp=0.0,
				revDamp=0.0,
				revLen=0.0;

			var	chain,dry,delay,imp,delimp;

			lo = LinLin.kr(lo,0,1,-9,9);
			mid1 = LinLin.kr(mid1,0,1,-9,9);
			mid2 = LinLin.kr(mid2,0,1,-9,9);
			mid3 = LinLin.kr(mid3,0,1,-9,9);
			hi = LinLin.kr(hi,0,1,-9,9);
			time = LinExp.kr(time,0,1,0.025,9.9);
			send = LinExp.kr(send,0,1,1,2)-1;
			fb = LinExp.kr(fb,0,1,1,2)-1;
			compThres = LinExp.kr(compThres,0,1,1,0.01);
			compRatio = LinExp.kr(compRatio,0,1,1,10).reciprocal;
			compAtk = LinExp.kr(compAtk,0,1,0.01,0.1);
			compRel = LinExp.kr(compRel,0,1,0.05,0.5);
			compHi = LinExp.kr(compHi,0,1,10,1000);
			compGain = LinExp.kr(compGain,0,1,1,10);
			revAmp = LinExp.kr(revAmp,0,1,0.001,2);
			revDamp = LinExp.kr(revDamp,0,1,0.01,1);
			revLen = LinExp.kr(revLen,0,1,0.5,20);

			chain = In.ar(in,1);

			imp = Impulse.kr(15);
			delimp = Delay1.kr(imp);
			SendReply.kr(imp, '/m1', [Amplitude.kr(chain).lag(0,1), K2A.ar(Peak.ar(chain, delimp).lag(0, 3))]);

			dry = chain;
			delay = LocalIn.ar(1).clip(-2,2);
			LocalOut.ar( DelayC.ar((dry*send)+(delay*fb),10,time) );
			chain = (dw*delay) + ((1-dw)*dry);

			chain = BLowShelf.ar(chain,300,0.5,lo);
			chain = BPeakEQ.ar(chain,600,0.25,mid1);
			chain = BPeakEQ.ar(chain,1200,0.25,mid2);
			chain = BPeakEQ.ar(chain,2400,0.25,mid3);
			chain = BHiShelf.ar(chain,4800,0.25,hi);

			chain = compGain*Compander.ar(chain,HPF.ar(chain,compHi),compThres,1,compRatio,compAtk,compRel);

			chain = chain + (revAmp*GVerb.ar(chain,130,revLen,revDamp,drylevel:0));

			chain = Limiter.ar(chain,0.95);
			chain = chain.clip(-1,1);

			SendReply.kr(imp, '/m2', [Amplitude.kr(chain).lag(0,1), K2A.ar(Peak.ar(chain, delimp).lag(0, 3))]);

			Out.ar(0,chain!2);

		}).send(Server.default);


		SynthDef(\Throat_ChordConvolution,{
			arg	f1 = 200,
				f2 = 250,
				f3 = 350,
				f4 = 500,
				f5 = 750,
				tone = 0.0,
				dw = 0,
				frameSize = 0;

			var	chord, in, dry, output, wet;

			f1 = Lag.kr(f1, 2);
			f2 = Lag.kr(f2, 2);
			f3 = Lag.kr(f3, 2);
			f4 = Lag.kr(f4, 2);
			f5 = Lag.kr(f5, 2);
			tone = Lag.kr(tone, 2);
			dw = Lag.kr(dw, 2);

			dry = In.ar(20, 1);

			chord = VarSaw.ar([f1, f2, f3, f4, f5], 0, tone * 0.5);
			chord = 0.1 * Mix.ar(chord);

			wet = Convolution.ar(dry, chord, 2048) + Convolution.ar(dry, chord, 256);
			output = ((1 - dw) * dry) + (dw * wet * 0.5);

			ReplaceOut.ar(20, output);
		}).send(Server.default);


		SynthDef(\Throat_HarmonicDistortion,{

			arg	dw=0.0,
				gain1=0.0,
				gain2=0.0,
				gain3=0.0,
				gain4=0.0,
				gain5=0.0,
				gain7=0.0;

			var	chain, wet, dry;

			gain1 = Lag.kr(LinExp.kr(gain1,0,1,1,2)-1,2);
			gain2 = Lag.kr(LinExp.kr(gain2,0,1,1,2)-1,2);
			gain3 = Lag.kr(LinExp.kr(gain3,0,1,1,2)-1,2);
			gain4 = Lag.kr(LinExp.kr(gain4,0,1,1,2)-1,2);
			gain5 = Lag.kr(LinExp.kr(gain5,0,1,1,2)-1,2);
			gain7 = Lag.kr(LinExp.kr(gain7,0,1,1,2)-1,2);
			dw = Lag.kr(dw, 2);

			dry = In.ar(20,1);
			wet = dry;
			wet = Mix.ar([
				wet*K2A.ar(gain1),
				Shaper.ar(12,wet,gain2),
				Shaper.ar(13,wet,gain3),
				Shaper.ar(14,wet,gain4),
				Shaper.ar(15,wet,gain5),
				Shaper.ar(17,wet,gain7)]);
			wet = LPF.ar(wet,11125);
			wet = LeakDC.ar(wet);
			chain = (dw*wet) + ((1-dw)*dry);

			ReplaceOut.ar(20,chain);

		}).send(Server.default);


		SynthDef(\Throat_ModDelay,{

			arg	dw = 0.0,
				freq = 0.0,
				noise = 0.0,
				drive = 0.0,
				pDepth = 0.0,
				rDepth = 0.0;

			var	dry, wet, chain;

			freq = Lag.kr(LinExp.kr(freq,0,1,1,1000),1);
			pDepth = Lag.kr(LinExp.kr(pDepth,0,1,1,2)-1,1);
			rDepth = Lag.kr(LinExp.kr(rDepth,0,1,1,2)-1,1);
			noise = Lag.kr(LinExp.kr(noise,0,1,1,10)-1,1);
			drive = Lag.kr(LinExp.kr(drive,0,1,0.9,100),1);
			dw = Lag.kr(dw, 2);

			dry = In.ar(20,1);
			wet = DelayC.ar(dry,0.1,SinOsc.kr(freq,0,0.01*pDepth,0.01*pDepth)+LFDNoise3.kr(freq,0.01*rDepth,0.01*rDepth));
			wet = wet + (PinkNoise.ar(noise)*wet);
			wet = (drive*wet).tanh;
			chain = (dw*wet) + ((1-dw)*dry);

			ReplaceOut.ar(20,chain);

		}).send(Server.default);


		SynthDef(\Throat_MagAbove,{

			arg	dw=0.0,
				thres=0.0,
				gcomp=0.0;

			var	chain, input, output;

			dw = Lag.kr(dw, 2);
			thres = Lag.kr(thres, 2);
			gcomp = Lag.kr(gcomp, 2);

			thres = LinExp.kr(thres,0,1,1,100)-1;

			input = In.ar(20,1);
			chain = FFT(LocalBuf(1024), input);
			chain = PV_MagAbove(chain, thres);
			chain = IFFT(chain);
			chain = chain + (chain*(gcomp*thres*0.01));

			ReplaceOut.ar(20,((1-dw)*input) + (dw*chain));

		}).send(Server.default);


		SynthDef(\Throat_MagBelow,{

			arg	dw=0.0,
				thres=0.0,
				gcomp=0.0;

			var	chain, input;

			dw = Lag.kr(dw, 2);
			thres = Lag.kr(dw, 2);
			gcomp = Lag.kr(gcomp, 2);

			thres = LinExp.kr(thres,0,1,1,100)-1;

			input = In.ar(20,1);
			chain = FFT(LocalBuf.new(1024),input);
			chain = PV_MagBelow(chain, thres);
			chain = IFFT(chain);
			chain = chain + (chain*(gcomp*10*(1-(thres*0.01))));

			ReplaceOut.ar(20,((1-dw)*input) + (dw*chain));

		}).send(Server.default);


		SynthDef(\Throat_PitchShift,{

			arg	dw=0.0,
				shift=0.0,
				pDisp=0.0,
				tDisp=0.0;

			var	chain, input;

			dw = Lag.kr(dw, 2);
			shift = Lag.kr(LinExp.kr(shift,0,1,0.25,4).round(0.25),2);
			pDisp = Lag.kr(LinExp.kr(pDisp,0,1,1.0,2.0)-1,2);
			tDisp = Lag.kr(LinExp.kr(tDisp,0,1,1.0,2.0)-1,2);

			input = In.ar(20,1);
			chain = PitchShift.ar(input,0.1,shift,pDisp,tDisp)
				+PitchShift.ar(input,0.1,shift,pDisp,tDisp)
				+PitchShift.ar(input,0.1,shift,pDisp,tDisp)
				+PitchShift.ar(input,0.1,shift,pDisp,tDisp);

			ReplaceOut.ar(20,((1-dw)*input) + (dw*chain));

		}).send(Server.default);


		SynthDef(\Throat_BinShift,{

			arg	dw=0.0,
				shift=0.0,
				spread=0.0;

			var	chain, input;

			shift = Lag.kr(shift, 2);
			spread = Lag.kr(spread, 2);
			dw = Lag.kr(dw, 2);

			shift = LinExp.kr(shift,0,1,0.25,4).round(0.25);
			spread = LinLin.kr(spread,0,1,-128,128);

			input = In.ar(20,1);
			chain = FFT(LocalBuf(2048),input);
			chain = PV_BinShift(chain, Lag.kr(shift,0.5), Lag.kr(spread,0.5));
			chain = IFFT(chain);
			ReplaceOut.ar(20,((1-dw)*input) + (dw*chain));

		}).send(Server.default);


		SynthDef(\Throat_AmpMod,{

			arg	dw=0.0,
				freqP=0.0,
				width=0.0,
				freqS=0.0,
				filter=0.0;

			var	chain, mod;

			freqP = Lag.kr(LinExp.kr(freqP,0.0,1.0,1,3000)-0.9, 2);
			freqS = Lag.kr(LinExp.kr(freqS,0.0,1.0,1,4000)-0.9, 2);
			filter = Lag.kr(LinExp.kr(filter,0,1,2,10), 2);
			dw = Lag.kr(dw, 2);

			chain = In.ar(20,1);
			mod = LPF.ar(LFPulse.ar(freqP,0,0.01+(Lag.kr(width*0.49,1))),freqP) + SinOsc.ar(freqS);
			chain = LPF.ar(dw*chain*mod,Lag.kr(filter*freqP+500,1)) + ((1-dw)*chain);

			ReplaceOut.ar(20,chain);

		}).send(Server.default);


		SynthDef(\Throat_Excess,{

			arg	dw=0.0,
				thres=0.0,
				gComp=0.0;
			var	dry, wet, output;

			thres = Lag.kr(LinExp.kr(thres,0,1,1,3)-1, 2);
			gComp = Lag.kr(gComp, 2);
			dw = Lag.kr(dw, 2);

			dry = In.ar(20,1);
			wet = dry.excess(thres);
			wet = wet + (wet*gComp*thres);
			output = ((1-dw)*dry)+(dw*wet);

			ReplaceOut.ar(20,output);

		}).send(Server.default);


		SynthDef(\Throat_Empty,{

			var	dry;

			dry = In.ar(20,1);
			ReplaceOut.ar(20,dry);

		}).send(Server.default);


		SynthDef(\Throat_3VoiceGrain,{

			arg	dw=0.0,
				f1=0.25,
				f2=0.5,
				f3=0.75,
				overlap=1.0,
				rand=0.0,
				lpf=0.5,
				fNoise=0.0,
				len=0.5;

			var	dry, wet, output,
				freq, dens, b,
				source, chain, recPhase,
				readPhase;

			f1 = Lag.kr(LinLin.kr(f1,0,1,-24,24),2);
			f2 = Lag.kr(LinLin.kr(f2,0,1,-24,24),2);
			f3 = Lag.kr(LinLin.kr(f3,0,1,-24,24),2);
			dens = Lag.kr(LinExp.kr(overlap,0,1,1,4), 2);
			rand = Lag.kr(LinExp.kr(rand,0,1,0.0001,1), 2);
			lpf = Lag.kr(LinExp.kr(lpf,0,1,1000,10000), 2);
			fNoise = Lag.kr(LinExp.kr(fNoise,0,1,0.0001,0.5), 2);
			len = Lag.kr(LinExp.kr(len,0,1,0.01,1.0), 2);
			dw = Lag.kr(dw, 2);

			b = LocalBuf.new(44100*10,1);

			dry = InFeedback.ar(20,1);
			recPhase = Phasor.ar(0,1,0,BufFrames.kr(b));
			readPhase = (recPhase-(len*44100)).wrap(0,BufFrames.kr(b)) / BufFrames.kr(b);
			BufWr.ar(dry,b,recPhase,1);
			wet =
				LPF.ar(Warp1.ar(1,b,PinkNoise.kr(0.0,readPhase),PinkNoise.kr(fNoise,f1.midiratio),len,-1,dens,rand,4),lpf)+
				LPF.ar(Warp1.ar(1,b,PinkNoise.kr(0.0,readPhase),PinkNoise.kr(fNoise,f2.midiratio),len,-1,dens,rand,4),lpf)+
				LPF.ar(Warp1.ar(1,b,PinkNoise.kr(0.0,readPhase),PinkNoise.kr(fNoise,f3.midiratio),len,-1,dens,rand,4),lpf);
			output = ((1-dw)*dry)+(dw*wet);
			ReplaceOut.ar(20,output);

		}).send(Server.default);


		SynthDef(\Throat_OctaveSynth,{

			arg	dw=0.0,
				f1=0.0,
				f2=0.0,
				f3=0.0,
				f1f=0.0,
				f2f=0.0,
				f3f=0.0,
				argRes=0.0,
				argTone=0.0,
				argThres=0.0;

			var	filterFreq, freq, hasFreq,
				in, chain, gate,
				tone, res, dry,
				wet, output;

			f1f = Lag.kr(LinLin.kr(f1f,0,1,0.0,0.5), 2);
			f2f = Lag.kr(LinLin.kr(f1f,0,1,0.0,0.5), 2);
			f3f = Lag.kr(LinLin.kr(f1f,0,1,0.0,0.5), 2);
			f1 = Lag.kr((2**(LinLin.kr(f1,0,1,-2,2))) + f1f, 2);
			f2 = Lag.kr((2**(LinLin.kr(f2,0,1,-2,2))) + f2f, 2);
			f3 = Lag.kr((2**(LinLin.kr(f3,0,1,-2,2))) + f3f, 2);
			dw = Lag.kr(dw, 2);
			argRes = Lag.kr(argRes, 2);
			argTone = Lag.kr(argTone, 2);
			argThres = Lag.kr(argThres, 2);
			dw = Lag.kr(dw, 2);

			dry = InFeedback.ar(20,1);
			#freq, hasFreq = Tartini.kr(2*dry);
			tone = 0.25 + (Amplitude.kr(2*dry)*LinExp.kr(argTone,0,1,0.75,25));
			res = 1.0 - LinExp.kr(argRes,0,1,0.1,0.95);
			freq = Lag.kr(freq,0.01);
			gate = Lag.ar(K2A.ar(hasFreq>LinExp.kr(argThres,0,1,0.1,0.99)),0.1);
			freq = Lag.kr(freq,0.01);
			filterFreq = Lag.kr((freq*tone).max(50).min(12000),0.25);
			wet = gate*(
				RLPF.ar(Pulse.ar(f1*freq),(f1*filterFreq).max(50).min(12000),res)+
				RLPF.ar(Pulse.ar(f2*freq),(f2*filterFreq).max(50).min(12000),res)+
				RLPF.ar(Pulse.ar(f3*freq),(f3*filterFreq).max(50).min(12000),res)
				);
			output = ((1-dw)*dry)+(dw*wet);
			ReplaceOut.ar(20,output);

		}).send(Server.default);


		SynthDef(\Throat_Choir,{

			arg	dw = 0.0,
				argThres = 0.99,
				f1 = 220,
				f2 = 220,
				f3 = 220,
				f4 = 220,
				f5 = 220,
				pDisp = 0.01,
				tDisp = 0.5,
				oct = 0.5,
				thres = 0.0,
				source = 0.0,
				atk = 0.5,
				rel = 0.5;

			var	freq, hasFreq, in,
				gate, gate2, output, dry,
				wet, src, fft, nred;

			dw = Lag.kr(dw, 2);
			f1 = Lag.kr(f1, 0.1);
			f2 = Lag.kr(f2, 0.1);
			f3 = Lag.kr(f3, 0.1);
			f4 = Lag.kr(f4, 0.1);
			f5 = Lag.kr(f5, 0.1); //1
			pDisp = Lag.kr(pDisp.linexp(0, 1, 1, 1.5), 2) - 1;
			tDisp = Lag.kr(tDisp, 2);
			oct = Lag.kr(oct, 2);

			atk = atk.linexp(0, 1, 0.1, 10);
			rel = rel.linexp(0, 1, 0.1, 10);

			argThres = Lag.kr(LinExp.kr(argThres, 0, 1, 0.6, 1.0), 2);
			dry = In.ar(20, 1);
			src = ((1 - source) * dry) + (source * In.ar(21, 1));

			#freq, hasFreq = Tartini.kr(src, 0.93, 1024);
			gate = LagUD.ar(K2A.ar(hasFreq > argThres), atk, rel);

			oct = 2**(LinLin.kr(oct, 0, 1, -1, 1).round(1));
			freq = Gate.kr(freq, hasFreq - argThres - 0.01);
			//freq = freq.clip(60, 760);
			freq = freq.clip(60, 1300);
			freq = Lag.kr(freq, 0.01); // 2.0

			f1 = f1 * LagUD.kr(oct / freq, 0.01, 0.01);
			f2 = f2 * LagUD.kr(oct / freq, 0.01, 0.01);
			f3 = f3 * LagUD.kr(oct / freq, 0.01, 0.01);
			f4 = f4 * LagUD.kr(oct / freq, 0.01, 0.01);
			f5 = f5 * LagUD.kr(oct / freq, 0.01, 0.01);
			gate2 = Lag.ar(Amplitude.ar(dry).clip(0, 0.2), 0.01) * 4;
			wet = gate2 * gate * Mix.ar( PitchShift.ar(dry, 0.125, [f1, f2, f3, f4, f5].min(4.0).max(0.125), pDisp, tDisp) );
			output = ((1 - dw) * dry) + (dw * wet);
			ReplaceOut.ar(20, output);

		}).send(Server.default);


		SynthDef(\Throat_Gain,{

			arg	gain=0.0;

			var	in, dry, output, wet;

			dry = In.ar(20,1);
			output = dry*Lag.kr(gain.linexp(0,1,0.001,2),2);
			ReplaceOut.ar(20,output);

		}).send(Server.default);


		SynthDef(\Throat_LPF,{

			arg	dw=0.0,freq=0.0,res=0.0;

			var	in, dry, output, wet;

			dry = In.ar(20,1);
			wet = RLPF.ar(dry,Lag.kr(freq.linexp(0,1,50,8000),2),Lag.kr(1.0-res.linexp(0,1,0.01,1),2));
			output = ((1-dw)*dry)+(dw*wet);
			ReplaceOut.ar(20,output);

		}).send(Server.default);


		SynthDef(\Throat_HPF,{

			arg	dw=0.0,freq=0.0,res=0.0;

			var	in, dry, output, wet;
			dry = In.ar(20,1);
			wet = RHPF.ar(dry,Lag.kr(freq.linexp(0,1,50,8000),2),Lag.kr(1.0-res.linexp(0,1,0.01,1),2));
			output = ((1-dw)*dry)+(dw*wet);
			ReplaceOut.ar(20,output);

		}).send(Server.default);


		SynthDef(\Throat_Delay,{

			arg	dw=0.0,fb=0,time=0;

			var	in, dry, output, wet;

			dry = In.ar(20,1);
			wet = LocalIn.ar(1);
			LocalOut.ar((Lag.kr(fb.linexp(0,1,0.01,2),2))*DelayC.ar(dry+wet,4,Lag.kr(time.linexp(0,1,0.01,4))));
			output = ((1-dw)*dry)+(dw*wet);
			ReplaceOut.ar(20,output);

		}).send(Server.default);


		SynthDef(\Throat_Reverb,{

			arg	dw=0.0,size=0.0,damp=0.0;

			var	in, dry, output, wet;

			dry = In.ar(20,1);
			wet = FreeVerb.ar(dry,1.0,Lag.kr(size.linexp(0,1,0.01,1),2),Lag.kr(damp.linexp(0,1,0.01,1),2));
			output = ((1-dw)*dry)+(dw*wet);
			ReplaceOut.ar(20,output);

		}).send(Server.default);


		SynthDef(\Throat_Sampler,{

			arg	dw=0.0,
				oct=0.0,semi=0.0,cent=0.0,
				len=0.0,pos=0.0,dir=0.0,
				lpFreq=0.0,hpFreq=0.0,
				t_sample=0,t_clear=0;

			var	in, dry, output, wet, slot, b1, b2, seq, bLen, trigger, sel;

			bLen = 8;
			oct = 2 ** (oct.linlin(0, 1, -2, 2));
			semi = semi.linlin(0, 1, -12 ,12).round(1).midiratio;
			cent = semi.linlin(0, 1, -1, 1).round(0.01).midiratio;
			lpFreq = Lag.kr(lpFreq.linexp(0, 1, 50, 10000), 2);
			hpFreq = Lag.kr(hpFreq.linexp(0, 1, 20, 4000), 2);
			dir = Lag.kr(dir.linlin(0, 1, 0, 0.5), 1);

			len = Lag.kr(len.linexp(0, 1, 0.001, 1), 4);
			pos = Lag.kr(pos, 2);

			b1 = LocalBuf.new(44100 * bLen, 1);
			b2 = LocalBuf.new(44100 * bLen, 1);

			dry = In.ar(20, 1);

			seq = Dseq.new([0, 1], inf);
			slot = Demand.kr(t_clear, 0, seq);

			//InputRec
			RecordBuf.ar(dry,Select.kr(slot, [b1, b2]), loop: 0, trigger:t_sample);
			//ClearRec
			RecordBuf.ar(DC.ar(0), Select.kr(slot, [b2, b1]), loop:1, trigger:t_sample);

			trigger = Impulse.kr((bLen * len).reciprocal);

			sel = (TRand.kr(0, 0.5, trigger) + (dir * 0.5)).round(1);

			wet = PlayBuf.ar(
				1,
				Select.kr(slot, [b1, b2]),
				(oct * semi * cent) * Select.kr(sel, [-1, 1]),
				trigger,
				44100 * bLen * pos,
				1
			);

			wet = LPF.ar(wet, lpFreq);
			wet = HPF.ar(wet, hpFreq);

			output = ((1 - dw) * dry) + (dw * wet);
			ReplaceOut.ar(20, output);

		}).send(Server.default);
	}
}