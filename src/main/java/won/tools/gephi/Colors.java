package won.tools.gephi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Colors {
	public static interface ColorTransformer {
		public Color transform(Color c);
	}

	/**
	 * Returns a ColorTransformer that multiplies the brightness of the color with the specified non-negative factor.
	 * @param amount
	 * @return
	 */
	public static ColorTransformer brightnessTransformer(final float factor) {
		if (factor < 0) throw new IllegalArgumentException("factor must be positive");
		return new ColorTransformer() {
			
			@Override
			public Color transform(Color c) {
				float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(),null);
				return Color.getHSBColor(hsb[0], hsb[1], hsb[2]*factor);
			}
		}; 
	}
	
	public static ColorTransformer hsbTransformer(final float hueDelta, final float saturationFactor, final float brightnessFactor) {
		if (hueDelta < -1 || hueDelta > 1) throw new IllegalArgumentException("hueDelat must be between -1 and 1");
		if (saturationFactor < 0 ) throw new IllegalArgumentException("saturationFactor must be positive");
		if (brightnessFactor < 0 ) throw new IllegalArgumentException("brightnessFactor must be positive");
		return new ColorTransformer() {
			
			@Override
			public Color transform(Color c) {
				float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(),null);
				return Color.getHSBColor(hsb[0]+hueDelta, cap(hsb[1]*saturationFactor), cap(hsb[2]*brightnessFactor));
			}
		}; 
	}

	public static ColorTransformer rgbTransformer(final float redFactor, final float greenFactor, final float blueFactor) {
		if (redFactor < 0 ) throw new IllegalArgumentException("redFactor must be positive");
		if (greenFactor < 0 ) throw new IllegalArgumentException("greenFactor must be positive");
		if (blueFactor < 0 ) throw new IllegalArgumentException("blueFactor must be positive");
		return new ColorTransformer() {
			
			@Override
			public Color transform(Color c) {
				float[] rgb = c.getRGBColorComponents(null);
				return new Color(cap(rgb[0]*redFactor), cap(rgb[1]*greenFactor), cap(rgb[2]* blueFactor));
			}
		};
	}
	
	private static float cap(float value) {
		return (value > 1f) ? 1f : (value < 0) ? 0 : value;
	}
	
	public static class ColorTransformerPipeline implements ColorTransformer {
		private List<ColorTransformer> transformers = new ArrayList<>();
		public void add(ColorTransformer transformer) {
			if (transformer == null) throw new IllegalArgumentException("transformer cannot be null");
			if (transformer == this) throw new IllegalArgumentException("pipeline cannot add itself to its own pipeline");
			this.transformers.add(transformer);
		}
		@Override
		public Color transform(Color c) {
			Color output = c;
			for (ColorTransformer t: this.transformers) {
				output = t.transform(output);
			}
			return output;
		}
	}
	
	public static ColorTransformerBuilder transformerBuilder() {
		return new ColorTransformerBuilder();
	}
	
	public static class ColorTransformerBuilder{
		float slightFactor = 0.9f;
		float strongFactor = 0.3f;
		float smallDelta = 0.01f;
		float bigDelta = 0.1f;
		private ColorTransformerBuilder() {
			
		}
		
		private ColorTransformerPipeline pipeline = new ColorTransformerPipeline();
		public ColorTransformer build(){
			return pipeline;
		}
		public ColorTransformerBuilder changeBrightness(float factor) {
			pipeline.add(hsbTransformer(0f, 1f, factor));
			return this;
		}
		public ColorTransformerBuilder brighter() {
			return changeBrightness(1f/slightFactor);
		}
		public ColorTransformerBuilder muchBrighter() {
			return changeBrightness(1f/strongFactor);
		}
		public ColorTransformerBuilder darker() {
			return changeBrightness(slightFactor);
		}
		public ColorTransformerBuilder muchDarker() {
			return changeBrightness(strongFactor);
		}
		public ColorTransformerBuilder changeSaturation(float factor) {
			pipeline.add(hsbTransformer(0f, factor, 1f));
			return this;
		}
		
		public ColorTransformerBuilder lessSaturation() {
			return changeSaturation(slightFactor);
		}
		public ColorTransformerBuilder muchLessSaturation() {
			return changeSaturation(strongFactor);
		}
		public ColorTransformerBuilder moreSaturation() {
			return changeSaturation(1f/slightFactor);
		}
		public ColorTransformerBuilder muchMoreSaturation() {
			return changeSaturation(1f/strongFactor);
		}
		
		public ColorTransformerBuilder changeHue(float delta) {
			pipeline.add(hsbTransformer(delta, 1f, 1f));
			return this;
		}
		
		public ColorTransformerBuilder increaseHue() {
			return changeHue(smallDelta);
		}

		public ColorTransformerBuilder increaseHueStrongly() {
			return changeHue(bigDelta);
		}
		
		public ColorTransformerBuilder decreaseHue() {
			return changeHue(-smallDelta);
		}

		public ColorTransformerBuilder decreaseHueStrongly() {
			return changeHue(-bigDelta);
		}
		
		public ColorTransformerBuilder changeRed(float factor) {
			pipeline.add(rgbTransformer(factor, 1f, 1f));
			return this;
		}
		public ColorTransformerBuilder moreRed() {
			return changeRed(1f/slightFactor);
		}
		public ColorTransformerBuilder muchMoreRed() {
			return changeRed(1f/strongFactor);
		}
		public ColorTransformerBuilder lessRed() {
			return changeRed(slightFactor);
		}
		public ColorTransformerBuilder muchLessRed() {
			return changeRed(strongFactor);
		}
		
		public ColorTransformerBuilder changeGreen(float factor) {
			pipeline.add(rgbTransformer(1f, factor, 1f));
			return this;
		}
		public ColorTransformerBuilder moreGreen() {
			return changeGreen(1f/slightFactor);
		}
		public ColorTransformerBuilder muchMoreGreen() {
			return changeGreen(1f/strongFactor);
		}
		public ColorTransformerBuilder lessGreen() {
			return changeGreen(slightFactor);
		}
		public ColorTransformerBuilder muchLessGreen() {
			return changeGreen(strongFactor);
		}
		public ColorTransformerBuilder changeBlue(float factor) {
			pipeline.add(rgbTransformer(1f, 1f, factor));
			return this;
		}
		public ColorTransformerBuilder moreBlue() {
			return changeBlue(1f/slightFactor);
		}
		public ColorTransformerBuilder muchMoreBlue() {
			return changeBlue(1f/strongFactor);
		}
		public ColorTransformerBuilder lessBlue() {
			return changeBlue(slightFactor);
		}
		public ColorTransformerBuilder muchLessBlue() {
			return changeBlue(strongFactor);
		}
		
		
	}

}
