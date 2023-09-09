package malilib.gui.callback;

import javax.annotation.Nullable;

import malilib.listener.EventListener;
import malilib.render.text.StyledTextLine;
import malilib.util.data.RangedFloatStorage;

public class FloatSliderCallback implements SteppedSliderCallback, SliderCallbackWithText
{
    protected final RangedFloatStorage storage;
    @Nullable protected final EventListener changeListener;
    protected double stepSize = 0.0009765625F; // 1 / 1024
    protected int maxSteps = Integer.MAX_VALUE;
    protected StyledTextLine displayText;

    public FloatSliderCallback(RangedFloatStorage storage, @Nullable EventListener changeListener)
    {
        this.storage = storage;
        this.changeListener = changeListener;
        this.updateDisplayText();
    }

    @Override
    public double getRelativeValue()
    {
        float minValue = this.storage.getMinFloatValue();
        float maxValue = this.storage.getMaxFloatValue();
        return (this.storage.getFloatValue() - minValue) / (maxValue - minValue);
    }

    @Override
    public void setRelativeValue(double relativeValue)
    {
        float minValue = this.storage.getMinFloatValue();
        float maxValue = this.storage.getMaxFloatValue();
        double relValue = relativeValue * (maxValue - minValue);
        double value = relValue + minValue;
        double step = this.stepSize;

        if (step > 0)
        {
            value = value - ((value + step) % step);
        }

        this.storage.setFloatValue((float) value);
        this.updateDisplayText();

        if (this.changeListener != null)
        {
            this.changeListener.onEvent();
        }
    }

    @Override
    public double getStepSize()
    {
        return this.stepSize;
    }

    @Override
    public void setStepSize(double step)
    {
        this.stepSize = step;
    }

    @Override
    public int getMaxSteps()
    {
        return this.maxSteps;
    }

    public void setMaxSteps(int maxSteps)
    {
        this.maxSteps = maxSteps;
    }

    @Override
    public void updateDisplayText()
    {
        this.displayText = StyledTextLine.translateFirstLine("malilib.label.config.slider_value.float",
                                                             String.format("%.4f", this.storage.getFloatValue()));
    }

    @Override
    public StyledTextLine getDisplayText()
    {
        return this.displayText;
    }
}
