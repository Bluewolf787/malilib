package malilib.config.option;

import malilib.util.MathUtils;
import malilib.util.position.Vec2d;

public class Vec2dConfig extends BaseDualValueConfig<Vec2d>
{
    protected final double minValue;
    protected final double maxValue;

    public Vec2dConfig(String name, Vec2d defaultValue)
    {
        this(name, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public Vec2dConfig(String name, double defaultX, double defaultY)
    {
        this(name, new Vec2d(defaultX, defaultY), Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public Vec2dConfig(String name, double defaultX, double defaultY, double minValue, double maxValue)
    {
        this(name, new Vec2d(defaultX, defaultY), minValue, maxValue);
    }

    public Vec2dConfig(String name, Vec2d defaultValue, double minValue, double maxValue)
    {
        super(name, defaultValue, name);

        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public boolean setValue(Vec2d newValue)
    {
        newValue = this.getClampedValue(newValue);
        return super.setValue(newValue);
    }

    public double getMinValue()
    {
        return this.minValue;
    }

    public double getMaxValue()
    {
        return this.maxValue;
    }

    protected Vec2d getClampedValue(Vec2d value)
    {
        double x = MathUtils.clamp(value.x, this.minValue, this.maxValue);
        double y = MathUtils.clamp(value.y, this.minValue, this.maxValue);

        return new Vec2d(x, y);
    }
}
