package org.mwc.cmap.grideditor.interpolation;

/**
 * Handle linear interpolation of a single floating point (double) variable.
 */
public class LinearInterpolator {

	@SuppressWarnings("unused")
	private final double A, B, a, b, f;

	/**
	 * Create a linear interpolator which maps [a,b] to [A,B]
	 */
	public LinearInterpolator(final double A, final double B, final double a, final double b) {
		this.A = A;
		this.B = B;
		this.a = a;
		this.b = b;
		this.f = (B - A) / (b - a);
	}

	/**
	 * Return the interpolated value corresponding to x.
	 */
	public double interp(final double x) {
		return A + (x - a) * f;
	}
}