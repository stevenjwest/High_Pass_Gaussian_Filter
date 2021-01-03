package io.github.stevenjwest;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.GaussianBlur3D;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class High_Pass_Gaussian_Filter implements PlugIn {
	
	ImagePlus imp;
	
	private static double xsigma=2, ysigma=2, zsigma=2;

	/**
	 * 
	 */
	@Override
	public void run(String arg) {
		
		ImagePlus imp = IJ.getImage();
		
		if (imp.isComposite() && imp.getNChannels()==imp.getStackSize()) {
			IJ.error("High-Pass Gaussian Filter", "Composite color images not supported");
			return;
		}
		
		GenericDialog gd = new GenericDialog("High Pass 3D Gaussian Blur");
		gd.addNumericField("X sigma:", xsigma, 1);
		gd.addNumericField("Y sigma:", ysigma, 1);
		gd.addNumericField("Z sigma:", zsigma, 1);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		xsigma = gd.getNextNumber();
		ysigma = gd.getNextNumber();
		zsigma = gd.getNextNumber();
		
		// duplicate the image:
		// ImagePlus imp2 = imp.duplicate();
		ImagePlus imp2 = new ImagePlus("", imp.getStack().duplicate() );
		
		// set channels, slices, frames:
		imp2.setDimensions(imp.getNChannels(), imp.getNSlices(), imp.getNFrames() );
		
		imp.startTiming();
		
		// use the imported blur into this class - which comments out the updateAndDraw() method on imp!
		blur(imp, xsigma, ysigma, zsigma);
		
		//imp2.setTitle("imp2");
		//imp2.show();
		
		// now subtract the blurred image from the original image:
		subtractStack(imp2, imp);
		
		// set the stack from imp2 to imp:
		imp.setStack( imp2.getImageStack() );
		
		// NOW updateAndDraw the original imp:
		imp.updateAndDraw();
		imp2 = null;
		
		IJ.showTime(imp, imp.getStartTime(), "", imp.getStackSize() );
		
	}
	
	public static void blur(ImagePlus imp, double sigmaX, double sigmaY, double sigmaZ) {
		imp.deleteRoi();
		ImageStack stack = imp.getStack();
		if (sigmaX>0.0 || sigmaY>0.0) {
			GaussianBlur gb = new GaussianBlur();
			int channels = stack.getProcessor(1).getNChannels();
			//IJ.showMessage("Number of channels: "+channels);
			gb.setNPasses(channels*imp.getStackSize());
			for (int i=1; i<=imp.getStackSize(); i++) {
				ImageProcessor ip = stack.getProcessor(i);
				double accuracy = (imp.getBitDepth()==8||imp.getBitDepth()==24)?0.002:0.0002;
				gb.blurGaussian(ip, sigmaX, sigmaY, accuracy);
			}
		}
		if (sigmaZ>0.0) {
			if (imp.isHyperStack())
				blurHyperStackZ(imp, sigmaZ);
			else
				blurZ(stack, sigmaZ);
			// imp.updateAndDraw();  DO NOT UPDATE AND DRAW!
		}
	}

	private static void blurZ(ImageStack stack, double sigmaZ) {
		GaussianBlur gb = new GaussianBlur();
		double accuracy = (stack.getBitDepth()==8||stack.getBitDepth()==24)?0.002:0.0002;
		int w=stack.getWidth(), h=stack.getHeight(), d=stack.getSize();
		float[] zpixels = null;
		FloatProcessor fp =null;
		IJ.showStatus("Z blurring");
		gb.showProgress(false);
		int channels = stack.getProcessor(1).getNChannels();
		for (int y=0; y<h; y++) {
			IJ.showProgress(y, h-1);
			for (int channel=0; channel<channels; channel++) {
				zpixels = stack.getVoxels(0, y, 0, w, 1, d, zpixels, channel);
				if (fp==null)
					fp = new FloatProcessor(w, d, zpixels);
				//if (y==h/2) new ImagePlus("before-"+h/2, fp.duplicate()).show();
				gb.blur1Direction(fp, sigmaZ, accuracy, false, 0);
				stack.setVoxels(0, y, 0, w, 1, d, zpixels, channel);
			}
		}
		IJ.showStatus("");
	}

	private static void blurHyperStackZ(ImagePlus imp, double zsigma) {
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int timePoints = imp.getNFrames();
		int nVolumes = channels*timePoints;
		for (int c=1; c<=channels; c++) {
			if (slices==1) {
				ImageStack stack = getVolume(imp, c, 1);
				blurZ(stack, zsigma);
			} else {
				for (int t=1; t<=timePoints; t++) {
					ImageStack stack = getVolume(imp, c, t);
					blurZ(stack, zsigma);
					//new ImagePlus("stack-"+c+"-"+t, stack).show();
				}
			}
		}
	}

	private static ImageStack getVolume(ImagePlus imp, int c, int t) {
		ImageStack stack1 = imp.getStack();
		ImageStack stack2 = new ImageStack(imp.getWidth(), imp.getHeight());
		if (imp.getNSlices()==1) {
			for (t=1; t<=imp.getNFrames(); t++) {
				int n = imp.getStackIndex(c, 1, t);
				stack2.addSlice(stack1.getProcessor(n));
			}
		} else {
			for (int z=1; z<=imp.getNSlices(); z++) {
				int n = imp.getStackIndex(c, z, t);
				stack2.addSlice(stack1.getProcessor(n));
			}
		}
		return stack2;
	}
	
	/**
	 * Subtract each ImageProcessor data in imp2 from the ImageProcessor data in imp,
	 * for each ImageProcessor in the stack.
	 * <p>
	 * Assumes imp and imp2 stacks are the same size in X Y and Z, and have the same number of
	 * channels and slices.
	 * <p>
	 * This method directly alters imp.
	 * 
	 * @param imp
	 * @param imp2
	 */
	public static void subtractStack(ImagePlus imp, ImagePlus imp2) {
		
		if(imp.getStackSize() != imp2.getStackSize() || imp.getWidth() != imp2.getWidth() ||
				imp.getHeight() != imp2.getHeight() || imp.getNChannels() != imp2.getNChannels() ||
				imp.getNFrames() != imp2.getNFrames() || imp.getNSlices() != imp2.getNSlices() ) {
			IJ.error("3D High-Pass Gaussian Blur", "The blurred image and original image do not match");
			return;
		}
		
		ImageCalculator ic = new ImageCalculator();
		imp = ic.run("subtract stack", imp, imp2);
		
	}

}
