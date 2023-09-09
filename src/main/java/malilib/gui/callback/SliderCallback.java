package malilib.gui.callback;

public interface SliderCallback
{
    /**
     * Maximum number of values/steps the underlying data can have.
     * Return Integer.MAX_VALUE for unlimited/non-specified, like double data type ranges.
     */
    int getMaxSteps();

    /**
     * Returns the relative value within the min - max range,
     * so relativeValue = (value - minValue) / (maxValue - minValue)
     */
    double getRelativeValue();

    /**
     * Sets the value from the provided relative value (0.0 ... 1.0)
     */
    void setRelativeValue(double relativeValue);
}
